package com.dianping.cat.report.task.metric;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.dianping.cat.report.task.TaskHelper;
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

	private Logger m_logger;

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
				Date reportPeriod = new Date(new Date().getTime() - DURATION);
				metricAlert(reportPeriod);
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

	protected void metricAlert(Date reportPeriod) {
		Map<String, MetricItemConfig> metricConfigMap = m_metricConfigManager.getMetricConfig().getMetricItemConfigs();
		Map<String, double[]> baselineMap = new HashMap<String, double[]>();
		Date hourStart = TaskHelper.thisHour(reportPeriod);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(reportPeriod);
		int minute = calendar.get(Calendar.MINUTE);
		for (String metricID : metricConfigMap.keySet()) {
			MetricItemConfig metricConfig = metricConfigMap.get(metricID);
			String domain = metricConfig.getDomain();
			String productLine = m_productLineConfigManager.queryProductLineByDomain(domain);
			for (MetricType type : MetricType.values()) {
				String key = metricID + ":" + type;
				BaselineConfig baselineConfig = m_baselineConfigManager.queryBaseLineConfig(key);
				String baselineKey = key + "+" + reportPeriod.getTime();
				double[] baseline = queryBaseline(baselineMap, baselineKey, key, reportPeriod);
				if (baseline == null) {
					continue;
				}

				MetricReport report = queryMetricReport(productLine, hourStart.getTime());
				double[] datas = extractDatasFromReport(report, metricConfig, type);
				if (datas == null) {
					continue;
				}
				List<Integer> alertList = analyzeDatas(baseline, datas, minute, baselineConfig);
				for (int alertItem : alertList) {
					m_logger.info("ALERT:" + key + "," + reportPeriod + ", minute:" + alertItem);
				}
				
			}
		}
	}

	private double[] queryBaseline(Map<String, double[]> baselineMap, String baselineKey, String key, Date reportPeriod) {
		double[] result = baselineMap.get(baselineKey);
		if (result == null) {
			try {
				result = m_baselineService.queryHourlyBaseline("metric", key, reportPeriod);
				baselineMap.put(baselineKey, result);
			} catch (Exception e) {
				return null;
			}
		}
		return result;
	}

	private double[] extractDatasFromReport(MetricReport report, MetricItemConfig metricConfig, MetricType type) {
		try {
			MetricItem reportItem = report.getMetricItems().get(metricConfig.getMetricKey());
			double[] datas = MetricPointParser.getOneHourData(reportItem, type);
			return datas;
		} catch (NullPointerException e) {
			return null;
		}
	}

	private MetricReport queryMetricReport(String product, long dateTime) {
		ModelRequest request = new ModelRequest(product, ModelPeriod.CURRENT.getStartTime());
		if (m_service.isEligable(request)) {
			ModelResponse<MetricReport> response = m_service.invoke(request);
			MetricReport report = response.getModel();
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable metric service registered for " + request + "!");
		}
	}

	private List<Integer> analyzeDatas(double[] baseline, double[] datas, int minute, BaselineConfig config) {
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
