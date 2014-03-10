package com.dianping.cat.report.task.metric;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Point;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.system.tool.MailSMS;

public class MetricAlert implements Task {

	@Inject
	private MetricConfigManager m_metricConfigManager;

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	@Inject
	private BaselineService m_baselineService;

	@Inject
	private MailSMS m_mailSms;

	@Inject(type = ModelService.class, value = MetricAnalyzer.ID)
	private ModelService<MetricReport> m_service;

	@Inject
	private AlertConfig m_alertConfig;

	private static final long DURATION = 1 * TimeUtil.ONE_MINUTE;

	private static final int DATA_CHECK_TIME = 2;

	private static final int DATA_AREADY_TIME = 1;

	private Map<String, MetricReport> m_currentReports = new HashMap<String, MetricReport>();

	private Map<String, MetricReport> m_lastReports = new HashMap<String, MetricReport>();

	private boolean computeAlertInfo(int minute, String product, MetricItemConfig config, MetricType type) {
		double[] value = null;
		double[] baseline = null;
		if (minute >= DATA_CHECK_TIME) {
			MetricReport report = fetchMetricReport(product, ModelPeriod.CURRENT);
			String metricKey = m_metricConfigManager.buildMetricKey(config.getDomain(), config.getType(),
			      config.getMetricKey());
			value = queryRealData(minute - DATA_CHECK_TIME, minute - 1, metricKey, report, type);
			baseline = queryBaseLine(minute - DATA_CHECK_TIME, minute - 1, metricKey,
			      new Date(ModelPeriod.CURRENT.getStartTime()), type);
		} else {
			MetricReport currentReport = fetchMetricReport(product, ModelPeriod.CURRENT);
			String metricKey = m_metricConfigManager.buildMetricKey(config.getDomain(), config.getType(),
			      config.getMetricKey());
			double[] currentValue = queryRealData(0, minute - 1, metricKey, currentReport, type);
			double[] currentBaseline = queryBaseLine(0, minute - 1, metricKey,
			      new Date(ModelPeriod.CURRENT.getStartTime()), type);

			MetricReport lastReport = fetchMetricReport(product, ModelPeriod.LAST);
			double[] lastValue = queryRealData(60 - (DATA_CHECK_TIME - minute), 59, metricKey, lastReport, type);
			double[] lastBaseline = queryBaseLine(60 - (DATA_CHECK_TIME - minute), 59, metricKey, new Date(
			      ModelPeriod.CURRENT.getStartTime()), type);

			value = mergerArray(lastValue, currentValue);
			baseline = mergerArray(lastBaseline, currentBaseline);
		}
		return m_alertConfig.checkData(value, baseline);
	}

	private MetricReport fetchMetricReport(String product, ModelPeriod period) {
		if (period == ModelPeriod.CURRENT) {
			MetricReport report = m_currentReports.get(product);

			if (report != null) {
				m_currentReports.put(product, report);
				return report;
			}
		} else if (period == ModelPeriod.LAST) {
			MetricReport report = m_lastReports.get(product);

			if (report != null) {
				m_lastReports.put(product, report);
				return report;
			}
		}
		ModelRequest request = new ModelRequest(product, period.getStartTime());
		
		if (m_service.isEligable(request)) {
			ModelResponse<MetricReport> response = m_service.invoke(request);

			return response.getModel();
		} else {
			throw new RuntimeException("Internal error: no eligable metric service registered for " + request + "!");
		}
	}

	@Override
	public String getName() {
		return "metric-alert";
	}

	private double[] mergerArray(double[] from, double[] to) {
		int fromLength = from.length;
		int toLength = to.length;
		double[] result = new double[fromLength + toLength];
		int index = 0;

		for (int i = 0; i < fromLength; i++) {
			result[i] = from[i];
			index++;
		}
		for (int i = 0; i < toLength; i++) {
			result[i + index] = to[i];
		}
		return result;
	}

	private void processProductLine(ProductLine productLine) {
		List<String> domains = m_productLineConfigManager.queryDomainsByProductLine(productLine.getId());
		List<MetricItemConfig> configs = m_metricConfigManager.queryMetricItemConfigs(new HashSet<String>(domains));
		long current = (System.currentTimeMillis() - DATA_AREADY_TIME * 1000) / 1000 / 60;
		int minute = (int) (current % (60));
		String product = productLine.getId();

		for (MetricItemConfig config : configs) {
			boolean alert = false;
			if (config.isShowAvg()) {
				alert = computeAlertInfo(minute, product, config, MetricType.AVG);
			}
			if (config.isShowCount()) {
				alert = computeAlertInfo(minute, product, config, MetricType.COUNT);
			}
			if (config.isShowSum()) {
				alert = computeAlertInfo(minute, product, config, MetricType.SUM);
			}
			if (alert) {
				sendAlertInfo(productLine, config);
			}
		}
	}

	private double[] queryBaseLine(int start, int end, String baseLineKey, Date date, MetricType type) {
		double[] baseline = m_baselineService.queryHourlyBaseline(MetricAnalyzer.ID, baseLineKey + ":" + type, date);
		int length = end - start + 1;
		double[] result = new double[length];
		System.arraycopy(baseline, start, result, 0, length);

		return result;
	}

	private double[] queryRealData(int start, int end, String metricKey, MetricReport report, MetricType type) {
		double[] all = new double[60];
		Map<Integer, Point> map = report.findOrCreateMetricItem(metricKey).findOrCreateAbtest("-1").findOrCreateGroup("")
		      .getPoints();

		for (Entry<Integer, Point> entry : map.entrySet()) {
			Integer minute = entry.getKey();
			Point point = entry.getValue();

			if (type == MetricType.AVG) {
				all[minute] = point.getAvg();
			} else if (type == MetricType.COUNT) {
				all[minute] = (double) point.getCount();
			} else if (type == MetricType.SUM) {
				all[minute] = point.getSum();
			}
		}
		int length = end - start + 1;
		double[] result = new double[length];
		System.arraycopy(all, start, result, 0, length);

		return result;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		boolean active = true;
		while (active) {
			Transaction t = Cat.newTransaction("MetricAlert", "Redo");
			long current = System.currentTimeMillis();

			m_currentReports.clear();
			m_lastReports.clear();
			try {
				Map<String, ProductLine> productLines = m_productLineConfigManager.getCompany().getProductLines();

				for (ProductLine productLine : productLines.values()) {
					try {
						processProductLine(productLine);
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
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

	private void sendAlertInfo(ProductLine productLine, MetricItemConfig config) {
		List<String> emails = m_alertConfig.buildMailReceivers(productLine);
		String title = m_alertConfig.buildMailTitle(productLine, config);
		String content = m_alertConfig.buildMailContent(productLine, config);

		m_mailSms.sendEmail(title, content, emails);
	}

	@Override
	public void shutdown() {
	}

}