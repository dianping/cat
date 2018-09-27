package com.dianping.cat.alarm.app.crash;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.alarm.crash.entity.ExceptionLimit;
import com.dianping.cat.alarm.receiver.entity.Receiver;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.alarm.spi.receiver.DefaultContactor;

public class CrashContactor extends DefaultContactor implements Contactor {
	public static final String ID = AlertType.CRASH.getName();

	@Inject
	protected AlertConfigManager m_alertConfigManager;

	@Inject
	protected CrashRuleConfigManager m_crashAlarmRuleManager;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public List<String> queryEmailContactors(String id) {
		List<String> mailReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return mailReceivers;
		} else {
			mailReceivers.addAll(buildDefaultMailReceivers(receiver));
			ExceptionLimit rule = m_crashAlarmRuleManager.queryExceptionLimit(id);

			if (rule != null) {
				mailReceivers.addAll(split(rule.getMails()));
			}

			return mailReceivers;
		}
	}

	@Override
	public List<String> queryWeiXinContactors(String id) {
		List<String> weixinReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return weixinReceivers;
		} else {
			weixinReceivers.addAll(buildDefaultWeixinReceivers(receiver));
			ExceptionLimit rule = m_crashAlarmRuleManager.queryExceptionLimit(id);

			if (rule != null) {
				weixinReceivers.addAll(split(rule.getMails()));
			}
			
			return weixinReceivers;
		}
	}

	@Override
	public List<String> querySmsContactors(String id) {
		List<String> smsReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return smsReceivers;
		} else {
			smsReceivers.addAll(buildDefaultSMSReceivers(receiver));

			return smsReceivers;
		}
	}

	@Override
	public List<String> queryDXContactors(String id) {
		List<String> receivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return receivers;
		} else {
			receivers.addAll(buildDefaultDXReceivers(receiver));
			ExceptionLimit rule = m_crashAlarmRuleManager.queryExceptionLimit(id);

			if (rule != null) {
				receivers.addAll(split(rule.getMails()));
			}

			return receivers;
		}
	}

}
