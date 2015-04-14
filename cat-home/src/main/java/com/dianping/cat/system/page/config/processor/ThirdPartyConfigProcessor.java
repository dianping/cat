package com.dianping.cat.system.page.config.processor;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;

import com.dianping.cat.home.alert.thirdparty.entity.Http;
import com.dianping.cat.home.alert.thirdparty.entity.Par;
import com.dianping.cat.home.alert.thirdparty.entity.Socket;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyConfigManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class ThirdPartyConfigProcessor {

	@Inject
	private ThirdPartyConfigManager m_thirdPartyConfigManager;

	public void process(Action action, Payload payload, Model model) {
		switch (action) {
		case THIRD_PARTY_RULE_CONFIGS:
			model.setThirdPartyConfig(m_thirdPartyConfigManager.getConfig());
			break;
		case THIRD_PARTY_RULE_UPDATE:
			Pair<Http, Socket> pair = queryThirdPartyConfigInfo(payload);

			if (pair != null) {
				model.setHttp(pair.getKey());
				model.setSocket(pair.getValue());
			}
			break;
		case THIRD_PARTY_RULE_SUBMIT:
			String type = payload.getType();

			if ("http".equals(type)) {
				m_thirdPartyConfigManager.insert(buildHttp(payload));
			}
			if ("socket".equals(type)) {
				m_thirdPartyConfigManager.insert(payload.getSocket());
			}
			model.setThirdPartyConfig(m_thirdPartyConfigManager.getConfig());
			break;
		case THIRD_PARTY_RULE_DELETE:
			type = payload.getType();
			String ruleId = payload.getRuleId();

			m_thirdPartyConfigManager.remove(ruleId, type);
			model.setThirdPartyConfig(m_thirdPartyConfigManager.getConfig());
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}

	private Http buildHttp(Payload payload) {
		Http http = payload.getHttp();
		String[] pars = payload.getPars().split(",");
		List<Par> lst = new ArrayList<Par>();

		for (int i = 0; i < pars.length; i++) {
			if (StringUtils.isNotEmpty(pars[i])) {
				Par par = new Par();
				String id = pars[i].trim();

				if (!id.contains("=")) {
					Par p = lst.get(lst.size() - 1);

					p.setId(p.getId() + "," + id);
				} else {
					par.setId(id);
					lst.add(par);
				}
			}
		}
		for (Par p : lst) {
			http.addPar(p);
		}
		return http;
	}

	private Pair<Http, Socket> queryThirdPartyConfigInfo(Payload payload) {
		String ruleId = payload.getRuleId();
		String type = payload.getType();
		Http http = null;
		Socket socket = null;

		if (StringUtils.isNotEmpty(ruleId)) {
			if ("http".equals(type)) {
				http = m_thirdPartyConfigManager.queryHttp(ruleId);
			} else if ("socket".equals(type)) {
				socket = m_thirdPartyConfigManager.querySocket(ruleId);
			}
			return new Pair<Http, Socket>(http, socket);
		}
		return null;
	}

}
