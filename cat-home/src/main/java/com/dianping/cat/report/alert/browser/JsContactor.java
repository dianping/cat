package com.dianping.cat.report.alert.browser;

import com.dianping.cat.alarm.receiver.entity.Receiver;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;
import com.dianping.cat.home.js.entity.ExceptionLimit;
import org.unidal.lookup.annotation.Inject;

import java.util.ArrayList;
import java.util.List;

public class JsContactor extends ProjectContactor implements Contactor {

	public static final String ID = AlertType.JS.getName();

	@Inject
	protected AlertConfigManager m_alertConfigManager;

	@Inject
	protected JsRuleConfigManager m_jsRuleConfigManager;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public List<String> queryDXContactors(String id) {
		List<String> receivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return receivers;
		} else {
			receivers.addAll(buildDefaultDXReceivers(receiver));
			String[] domainAndLevel = id.split(JsRuleConfigManager.SPLITTER);

			if (domainAndLevel.length > 1) {
				ExceptionLimit rule = m_jsRuleConfigManager.queryExceptionLimit(domainAndLevel[0], domainAndLevel[1]);

				if (rule != null) {
					receivers.addAll(split(rule.getMails()));
				}
			}

			return receivers;
		}
	}

	@Override
	public List<String> queryEmailContactors(String id) {
		List<String> mailReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return mailReceivers;
		} else {
			mailReceivers.addAll(buildDefaultMailReceivers(receiver));
			String[] domainAndLevel = id.split(JsRuleConfigManager.SPLITTER);

			if (domainAndLevel.length > 1) {
				ExceptionLimit rule = m_jsRuleConfigManager.queryExceptionLimit(domainAndLevel[0], domainAndLevel[1]);

				if (rule != null) {
					mailReceivers.addAll(split(rule.getMails()));
				}
			}

			return mailReceivers;
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
	public List<String> queryWeiXinContactors(String id) {
		List<String> weixinReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return weixinReceivers;
		} else {
			weixinReceivers.addAll(buildDefaultWeixinReceivers(receiver));
			String[] domainAndLevel = id.split(JsRuleConfigManager.SPLITTER);

			if (domainAndLevel.length > 1) {
				ExceptionLimit rule = m_jsRuleConfigManager.queryExceptionLimit(domainAndLevel[0], domainAndLevel[1]);

				if (rule != null) {
					weixinReceivers.addAll(split(rule.getMails()));
				}
			}
			return weixinReceivers;
		}
	}

}
