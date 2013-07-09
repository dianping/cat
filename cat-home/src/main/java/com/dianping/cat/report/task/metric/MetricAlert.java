package com.dianping.cat.report.task.metric;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;

public class MetricAlert implements Task, LogEnabled {

	@Inject
	protected MetricConfigManager m_metricConfigManager;

	@Inject
	protected BaselineService m_baselineService;

	@Inject
	protected ProductLineConfigManager m_productLineConfigManager;

	@Inject
	protected BaselineConfigManager m_baselineConfigManager;

	@Inject
	protected ServerConfigManager m_manager;

	@Inject
	protected ReportService m_reportService;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private static final int DURATION_IN_MINUTE = 10;

	private static final long DURATION = TimeUtil.ONE_MINUTE;

	@Override
	public void run() {

		boolean active = true;

		while (active) {
			Transaction t = Cat.newTransaction("MetricAlert", "Redo");
			long current = System.currentTimeMillis();
			try {
				Map<String, MetricItemConfig> configMap = m_metricConfigManager.getMetricConfig().getMetricItemConfigs();
				for (String metricID : configMap.keySet()) {
					MetricItemConfig metricConfig = configMap.get(metricID);
					String domain = metricConfig.getDomain();
					for (MetricType type : MetricType.values()) {
						String key = metricID + ":" + type;
						Date reportPeriod = new Date(new Date().getTime() - DURATION);
						double[] baseline = m_baselineService.queryHourlyBaseline("metricBaseline", key, reportPeriod);
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(reportPeriod);
						int minute = calendar.get(Calendar.MINUTE);

						Date start = TaskHelper.thisHour(reportPeriod);
						Date end = new Date(start.getTime() + TimeUtil.ONE_HOUR);
						MetricReport report = m_reportService.queryMetricReport(domain, start, end);
						BaselineConfig baselineConfig = m_baselineConfigManager.queryBaseLineConfig(key);
						MetricItem reportItem = report.getMetricItems().get(metricConfig.getMetricKey());
						double[] datas = MetricPointParser.getOneHourData(reportItem, type);
						List<Integer> alertList = metricAlarm(baseline, datas, minute, baselineConfig);
						String alert = "";
						for (int alertItem : alertList) {
							alert = alert + alertItem + ",";
						}
						m_logger.info("ALERT:" + alert);
					}
				}
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
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

	private List<Integer> metricAlarm(double[] baseline, double[] datas, int minute, BaselineConfig config) {
		List<Integer> result = new ArrayList<Integer>();
		int start = minute / DURATION_IN_MINUTE;
		int end = minute / DURATION_IN_MINUTE + 10;
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
