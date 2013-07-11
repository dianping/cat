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
import com.dianping.cat.consumer.advanced.ProductLineConfigManager;
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

	private static final int DURATION_IN_MINUTE = 1;

	private static final long DURATION = DURATION_IN_MINUTE * TimeUtil.ONE_MINUTE;

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

	private List<Integer> check(double[] realDatas, double[] baseLineDatas, BaselineConfig config) {
		int size = realDatas.length;
		for (int i = 0; i <= size; i++) {

		}

		return null;
	}

	protected void metricAlert(Date date) {
		long current = date.getTime() / 1000 / 60;
		int minute = (int) (current % (60));
		Set<String> productLines = m_productLineConfigManager.queryProductLines().keySet();

		for (String productLine : productLines) {
			MetricReport report = queryMetricReport(productLine);

			for (MetricItem item : report.getMetricItems().values()) {
				String key = item.getId();
				MetricItemConfig metricConfig = m_metricConfigManager.queryMetricItemConfig(key);
				List<Integer> alerts = new ArrayList<Integer>();

				if (metricConfig.isShowCount()) {
					alerts.addAll(buildAlertInfo(date, MetricType.COUNT, item, key));
				}
				if (metricConfig.isShowAvg()) {
					alerts.addAll(buildAlertInfo(date, MetricType.AVG, item, key));
				}
				if (metricConfig.isShowSum()) {
					alerts.addAll(buildAlertInfo(date, MetricType.SUM, item, key));
				}

				if (alerts.size() > 0) {
					String alertInfo = buildAlertInfo(alerts, minute);
					m_logger.info(alertInfo);
				}
			}
		}
	}

	private String buildAlertInfo(List<Integer> alerts, int minute) {
		return null;
	}

	private List<Integer> buildAlertInfo(Date date, MetricType type, MetricItem item, String key) {
		String baseLineKey = key + ":" + type;
		double[] baseline = m_baselineService.queryHourlyBaseline(METRIC, baseLineKey, date);
		if (baseline != null) {
			double[] realDatas = extractDatasFromReport(item, type);
			BaselineConfig baselineConfig = m_baselineConfigManager.queryBaseLineConfig(baseLineKey);
			return check(baseline, realDatas, baselineConfig);
		}
		return new ArrayList<Integer>();
	}

	private double[] extractDatasFromReport(MetricItem item, MetricType type) {
		return m_parser.queryOneHourData(item, type);
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
		int start = minute / DURATION_IN_MINUTE;
		int end = minute / DURATION_IN_MINUTE + DURATION_IN_MINUTE;
		double minValue = config.getMinValue();
		double lowerLimit = config.getLowerLimit();
		double upperLimit = config.getUpperLimit();
		for (int i = start; i < end; i++) {
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
