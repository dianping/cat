package com.dianping.cat.system.page.config.processor;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.system.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class DisplayConfigProcessor {

	@Inject
	private HeartbeatDisplayPolicyManager m_displayPolicyManager;

	public void process(Action action, Payload payload, Model model) {
		switch (action) {
		case DISPLAY_POLICY:
			String displayPoicy = payload.getContent();

			if (!StringUtils.isEmpty(displayPoicy)) {
				model.setOpState(m_displayPolicyManager.insert(displayPoicy));
			} else {
				model.setOpState(true);
			}
			model.setContent(m_displayPolicyManager.getHeartbeatDisplayPolicy().toString());
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}
}
