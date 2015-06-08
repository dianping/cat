package com.dianping.cat.system.page.config.processor;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.report.page.activity.config.ActivityConfigManager;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class DisplayConfigProcessor {

	@Inject
	private HeartbeatDisplayPolicyManager m_displayPolicyManager;

	@Inject
	private ActivityConfigManager m_activityConfigManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	public void process(Action action, Payload payload, Model model) {
		switch (action) {
		case DISPLAY_POLICY:
			String displayPoicy = payload.getContent();

			if (!StringUtils.isEmpty(displayPoicy)) {
				model.setOpState(m_displayPolicyManager.insert(displayPoicy));
			} else {
				model.setOpState(true);
			}
			model.setContent(m_configHtmlParser.parse(m_displayPolicyManager.getHeartbeatDisplayPolicy().toString()));
			break;

		case ACTIVITY_CONFIG_UPDATE:
			String activityConfig = payload.getContent();

			if (!StringUtils.isEmpty(activityConfig)) {
				model.setOpState(m_activityConfigManager.insert(activityConfig));
			} else {
				model.setOpState(true);
			}
			model.setContent(m_configHtmlParser.parse(m_activityConfigManager.getActivityConfig().toString()));
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}
}
