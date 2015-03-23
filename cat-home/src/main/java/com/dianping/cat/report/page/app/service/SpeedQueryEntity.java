package com.dianping.cat.report.page.app.service;

import java.util.List;

import org.unidal.helper.Splitters;

import com.dianping.cat.Cat;

public class SpeedQueryEntity extends BaseQueryEntity {

	public SpeedQueryEntity() {
		super();
		m_id = 1;
	}

	public SpeedQueryEntity(String query) {
		List<String> strs = Splitters.by(";").split(query);

		try {
			m_date = parseDate(strs.get(0));
			m_id = parseValue(strs.get(2));
			m_network = parseValue(strs.get(3));
			m_version = parseValue(strs.get(4));
			m_platfrom = parseValue(strs.get(5));
			m_city = parseValue(strs.get(6));
			m_operator = parseValue(strs.get(7));
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

}
