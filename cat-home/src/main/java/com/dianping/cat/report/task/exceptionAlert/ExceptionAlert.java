package com.dianping.cat.report.task.exceptionAlert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.hsqldb.lib.StringUtil;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.company.model.entity.Domain;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.alertReport.entity.AlertReport;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.dependency.Context;
import com.dianping.cat.report.page.dependency.ExternalInfoBuilder;
import com.dianping.cat.report.page.dependency.Model;
import com.dianping.cat.report.page.dependency.Payload;
import com.dianping.cat.report.page.top.TopMetric.Item;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.metric.AlertConfig;
import com.dianping.cat.system.config.ExceptionThresholdConfigManager;
import com.dianping.cat.system.tool.DefaultMailImpl;

public class ExceptionAlert implements Task, LogEnabled {

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private ExternalInfoBuilder m_externalInfoBuilder;

	@Inject
	private AlertConfig m_alertConfig;

	@Inject
	private DefaultMailImpl m_mailSms;

	@Inject
	protected ProductLineConfigManager m_productLineConfigManager;

	@Inject
	private MetricConfigManager m_metricConfigManager;

	@Inject
	protected ReportService m_reportService;

	@Inject
	private ExceptionThresholdConfigManager m_configManager;

	private static final long DURATION = TimeUtil.ONE_MINUTE;

	private Logger m_logger;

	private void buildAllErrorInfo(Payload payload, Model model) {
		model.setReportStart(new Date(payload.getDate()));
		model.setReportEnd(new Date(payload.getDate() + TimeUtil.ONE_HOUR - 1));
		m_externalInfoBuilder.buildTopErrorInfo(payload, model);
	}

	private void normalize(Model model, Payload payload) {

		m_normalizePayload.normalize(model, payload);

		Integer minute = parseQueryMinute(payload);
		int maxMinute = 60;
		List<Integer> minutes = new ArrayList<Integer>();

		if (payload.getPeriod().isCurrent()) {
			long current = System.currentTimeMillis() / 1000 / 60;
			maxMinute = (int) (current % (60));
		}
		for (int i = 0; i < 60; i++) {
			minutes.add(i);
		}
		model.setMinute(minute);
		model.setMaxMinute(maxMinute);
		model.setMinutes(minutes);
	}

	private int parseQueryMinute(Payload payload) {
		int minute = 0;
		String min = payload.getMinute();

		if (StringUtil.isEmpty(min)) {
			long current = System.currentTimeMillis() / 1000 / 60;
			minute = (int) (current % (60));
		} else {
			minute = Integer.parseInt(min);
		}

		return minute;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		boolean active = true;
		while (active) {
			int minute = Calendar.getInstance().get(Calendar.MINUTE);
			String minuteStr = String.valueOf(minute);

			if (minute < 10) {
				minuteStr = '0' + minuteStr;
			}
			Transaction t = Cat.newTransaction("ExceptionAlert", "M" + minuteStr);
			long current = System.currentTimeMillis();

			try {
				Payload payload = new Payload();
				payload.setMinuteCounts(5);
				payload.setTopCounts(Integer.MAX_VALUE);
				payload.setAction("exceptionAlert");
				Context ctx = new Context();
				Model model = new Model(ctx);
				normalize(model, payload);
				buildAllErrorInfo(payload, model);
//				test();
//				System.out.println(model.getTopMetric().getError().getResult().values());
				for (Entry<String, List<Item>> item : model.getTopMetric().getError().getResult().entrySet()) {
					for (Item i : item.getValue()) {
						if (i.getAlert() > 0) {
//							sendAlertInfo(i.getDomain(), "Exception Alert !"+ "[" +i.getDomain() + "] : [" + i.getException() , i.getAlert());
							 System.out.println("alertErrors:" + item.getKey() + ":" + i.getDomain() + ":" +i.getException() + "["+
							 i.getAlert()+"]");
						}
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
					Thread.sleep(TimeUtil.ONE_MINUTE);
				}
			} catch (InterruptedException e) {
				active = false;
			}
		}

	}

	private void test() throws ParseException {
		AlertReportBuilder builder = new AlertReportBuilder(m_reportService, m_configManager);
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2014-04-29 20:00");
		builder.buildHourlyTask("alert", "Cat", date);
		
	}

	private ProductLine getProductLineByDomain(String domain) {
		Collection<ProductLine> productLines = m_productLineConfigManager.queryAllProductLines().values();
		for (ProductLine product : productLines) {
			Map<String, Domain> domains = product.getDomains();
			if (domains.containsKey(domain)) {
				return product;
			}
		}
		return null;
	}

	private void sendAlertInfo(String domain, String content, int alert) {
		ProductLine productLine = getProductLineByDomain(domain);

		List<String> emails = m_alertConfig.buildMailReceivers(productLine);
		List<String> phones = m_alertConfig.buildSMSReceivers(productLine);
		String title = productLine + ":" + domain + "exception alert !";

		m_logger.info(title + " " + content + " " + emails);
		m_mailSms.sendEmail(title, content, emails);
		if (alert == 2) {
			m_mailSms.sendSms(title + " " + content, content, phones);
		}

		Cat.logEvent("MetricAlert", productLine.getId(), Event.SUCCESS, title + "  " + content);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return "exception-alert";
	}

	@Override
	public void shutdown() {
	}
}
