package com.dianping.cat.report.task.metric;

import java.util.Calendar;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.company.model.entity.ProductLine;

import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Event;

import com.dianping.cat.report.page.nettopo.Connection;
import com.dianping.cat.report.page.nettopo.Interface;
import com.dianping.cat.report.page.nettopo.NetGraph;
import com.dianping.cat.report.page.nettopo.NetTopology;

import com.dianping.cat.system.tool.MailSMS;

public class NetGraphFillData implements Task, LogEnabled {

	@Inject
	private MailSMS m_mailSms;

	@Inject
	private RemoteMetricReportService m_service;
	
	private Logger m_logger;
	
	@Inject
	private AlertConfig m_alertConfig;

	@Inject
	private AlertInfo m_alertInfo;
	
	private static final long DURATION = TimeUtil.ONE_MINUTE;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void run() {
		NetGraph netGraph = NetGraph.getInstance();
		netGraph.buildGraphFromConfig();
		
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
			long current = System.currentTimeMillis();

			System.out.println("nimei");
			for (NetTopology netTopology : netGraph.getNetTopologys()) {
				for (Connection connection : netTopology.getConnections()) {
					for (Interface interface_ : connection.getFirstData()) {
						interface_.UpdateData();
					}
					
					for (Interface interface_ : connection.getSecondData()) {
						interface_.UpdateData();
					}
				}
			}
			System.out.println("nimeimei");
			
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

	private void sendAlertInfo(ProductLine productLine, MetricItemConfig config, String content) {
		List<String> emails = m_alertConfig.buildMailReceivers(productLine);
		List<String> phones = m_alertConfig.buildSMSReceivers(productLine);
		String title = m_alertConfig.buildMailTitle(productLine, config);

		m_logger.info(title + " " + content + " " + emails);
		m_mailSms.sendEmail(title, content, emails);
		m_mailSms.sendSms(title + " " + content, content, phones);

		Cat.logEvent("NetGraphFillData", productLine.getId(), Event.SUCCESS, title + "  " + content);
	}

	@Override
	public void shutdown() {
	}

	@Override
	public String getName() {
		return "NetGraphFillData";
	}

}