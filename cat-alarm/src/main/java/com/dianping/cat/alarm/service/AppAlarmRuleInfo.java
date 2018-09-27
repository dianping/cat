package com.dianping.cat.alarm.service;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.AppAlarmRule;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.rule.transform.DefaultSaxParser;
import com.dianping.cat.helper.JsonBuilder;

public class AppAlarmRuleInfo {

	private AppAlarmRule m_entity;

	private Rule m_rule = new Rule();

	public AppAlarmRuleInfo(AppAlarmRule entity) {
		m_entity = entity;

		try {
			m_rule = DefaultSaxParser.parseEntity(Rule.class, entity.getContent());
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public AppAlarmRule getEntity() {
		return m_entity;
	}

	public String getJsonString() {
		return new JsonBuilder().toJson(this);
	}

	public Rule getRule() {
		return m_rule;
	}
}
