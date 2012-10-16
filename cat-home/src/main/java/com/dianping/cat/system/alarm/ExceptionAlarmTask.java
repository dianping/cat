package com.dianping.cat.system.alarm;

import java.util.List;

import com.dianping.cat.system.alarm.connector.Connector;
import com.dianping.cat.system.alarm.entity.AlarmData;
import com.dianping.cat.system.alarm.exception.ExceptionRuleManager;
import com.dianping.cat.system.alarm.template.ThresholdRule;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

public class ExceptionAlarmTask implements Task {

	@Inject
	private ExceptionRuleManager m_manager;

	@Inject
	private Connector m_connector;

	@Override
	public void run() {
		while (true) {
			long time = System.currentTimeMillis();

			try {
				List<ThresholdRule> rules = m_manager.getAllExceptionRules();

				for (ThresholdRule rule : rules) {
					AlarmData data = m_connector.fetchAlarmData(rule.getConnectUrl());

					rule.addData(data);
					String content = rule.match();

					if (content != null) {
						// send alarm
					}
				}
			} catch (Exception e) {

			}
			long duration = System.currentTimeMillis() - time;
			try {
				Thread.sleep(30 * 1000 - duration);
			} catch (Exception e) {
				// igrone
			}
		}
	}

	@Override
	public String getName() {
		return "ExceptionAlarm";
	}

	@Override
	public void shutdown() {
	}

}
