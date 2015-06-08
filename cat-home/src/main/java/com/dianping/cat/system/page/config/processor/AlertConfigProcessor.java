package com.dianping.cat.system.page.config.processor;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.report.alert.sender.config.AlertConfigManager;
import com.dianping.cat.report.alert.sender.config.AlertPolicyManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class AlertConfigProcessor {

	@Inject
	private AlertConfigManager m_alertConfigManager;

	@Inject
	private AlertPolicyManager m_alertPolicyManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	public void process(Action action, Payload payload, Model model) {
		switch (action) {
		case ALERT_DEFAULT_RECEIVERS:
			String alertDefaultReceivers = payload.getContent();
			String allOnOrOff = payload.getAllOnOrOff();
			String xmlContent = m_alertConfigManager.buildReceiverContentByOnOff(alertDefaultReceivers, allOnOrOff);

			if (!StringUtils.isEmpty(alertDefaultReceivers)) {
				model.setOpState(m_alertConfigManager.insert(xmlContent));
			} else {
				model.setOpState(true);
			}
			model.setContent(m_configHtmlParser.parse(m_alertConfigManager.getAlertConfig().toString()));
			break;
		case ALERT_POLICY:
			String alertPolicy = payload.getContent();

			if (!StringUtils.isEmpty(alertPolicy)) {
				model.setOpState(m_alertPolicyManager.insert(alertPolicy));
			} else {
				model.setOpState(true);
			}
			model.setContent(m_configHtmlParser.parse(m_alertPolicyManager.getAlertPolicy().toString()));
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}
}
