package com.dianping.cat.report.task.metric;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.advanced.MetricConfigManager;
import com.dianping.cat.consumer.core.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.baseline.BaselineConfig;
import com.dianping.cat.report.baseline.BaselineConfigManager;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class MetricAlert implements Task, LogEnabled {

	@Inject
	protected MetricConfigManager m_metricConfigManager;

	@Inject
	protected ProductLineConfigManager m_productLineConfigManager;

	@Inject
	protected BaselineService m_baselineService;

	@Inject
	protected BaselineConfigManager m_baselineConfigManager;

	@Inject
	protected ServerConfigManager m_manager;

	@Inject
	protected ReportService m_reportService;

	@Inject(type = ModelService.class, value = "metric")
	protected ModelService<MetricReport> m_service;

	@Inject
	protected MetricPointParser m_parser;

	private Logger m_logger;

	private static final String METRIC = "metric";

	private static final long TEN_SECONDS = 10 * 1000;

	private static final long DURATION = 1 * TimeUtil.ONE_MINUTE;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void run() {
		boolean active = true;
		while (active) {
			Transaction t = Cat.newTransaction("MetricAlert", "Redo");
			long current = System.currentTimeMillis();
			try {
				Date date = new Date(System.currentTimeMillis() - DURATION - TEN_SECONDS);

				metricAlert(date);
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);

			} finally {
				t.complete();
			}
			long duration = System.currentTimeMillis() - current;

			try {
				if (duration < DURATION) {
					Thread.sleep(DURATION - duration);
				}
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	protected void metricAlert(Date date) {
		long current = date.getTime() / 1000 / 60;
		int minute = (int) (current % (60));
		Set<String> productLines = m_productLineConfigManager.queryProductLines().keySet();

		for (String productLine : productLines) {
			MetricReport report = queryMetricReport(productLine);
			for (MetricItem item : report.getMetricItems().values()) {
				MetricItemConfig metricConfig = m_metricConfigManager.queryMetricItemConfig(item.getId());
				if (metricConfig == null) {
					continue;
				}
				List<AlertInfo> alerts = new ArrayList<AlertInfo>();

				if (metricConfig.isShowCount()) {
					alerts.addAll(buildAlertInfo(date, productLine, MetricType.COUNT, item, minute));
				}
				if (metricConfig.isShowAvg()) {
					alerts.addAll(buildAlertInfo(date, productLine, MetricType.AVG, item, minute));
				}
				if (metricConfig.isShowSum()) {
					alerts.addAll(buildAlertInfo(date, productLine, MetricType.SUM, item, minute));
				}

				if (alerts.size() > 0) {
					for (AlertInfo alert : alerts) {
						m_logger.info(alert.toString());
					}
				}
			}
		}
	}

	private class AlertInfo {
		Date date;

		MetricType metricType;

		String productLine;

		String metricId;

		public AlertInfo(Date date, MetricType metricType, String productLine, String metricId) {
			super();
			this.date = date;
			this.metricType = metricType;
			this.productLine = productLine;
			this.metricId = metricId;
		}

		@Override
		public String toString() {
			return "AlertInfo [date=" + date + ", metricType=" + metricType + ", productLine=" + productLine
			      + ", metricId=" + metricId + "]";
		}

	}

	private List<AlertInfo> buildAlertInfo(Date date, String productLine, MetricType type, MetricItem item, int minute) {
		List<AlertInfo> result = new ArrayList<AlertInfo>();
		String id = item.getId();
		String baseLineKey = id + ":" + type;
		double[] baseline = m_baselineService.queryHourlyBaseline(METRIC, baseLineKey, date);
		if (baseline != null) {
			double[] realDatas = m_parser.buildHourlyData(item, type);
			if (realDatas == null) {
				return result;
			}
			BaselineConfig baselineConfig = m_baselineConfigManager.queryBaseLineConfig(baseLineKey);
			List<Integer> minutes = checkData(baseline, realDatas, minute, baselineConfig);
			for (int resultMinuteInHour : minutes) {
				long current = date.getTime() / TimeUtil.ONE_MINUTE;
				long resultMinute = current - minute + resultMinuteInHour;
				Date resultDate = new Date(resultMinute * TimeUtil.ONE_MINUTE);
				AlertInfo info = new AlertInfo(resultDate, type, productLine, item.getId());
				result.add(info);
			}
		}
		return result;
	}

	private MetricReport queryMetricReport(String product) {
		ModelRequest request = new ModelRequest(product, ModelPeriod.CURRENT.getStartTime());
		if (m_service.isEligable(request)) {
			ModelResponse<MetricReport> response = m_service.invoke(request);
			MetricReport report = response.getModel();
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable metric service registered for " + request + "!");
		}
	}

	private List<Integer> checkData(double[] baseline, double[] datas, int minute, BaselineConfig config) {
		List<Integer> result = new ArrayList<Integer>();
		double minValue = config.getMinValue();
		double lowerLimit = config.getLowerLimit();
		double upperLimit = config.getUpperLimit();
		for (int i = 0; i <= minute; i++) {
			if (baseline[i] < 0) {
				continue;
			} else if (baseline[i] == 0) {
				if (datas[i] >= minValue) {
					result.add(i);
				}
			} else {
				double percent = datas[i] / baseline[i];
				if (datas[i] >= minValue && (percent < lowerLimit || percent > upperLimit)) {
					result.add(i);
				}
			}
		}
		return result;
	}

	@Override
	public String getName() {
		return "MetricAlertRedo";
	}

	@Override
	public void shutdown() {

	}

}
