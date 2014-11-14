package com.dianping.cat.service.app.speed;

import java.util.List;

import org.unidal.helper.Splitters;

import com.dianping.cat.Cat;
import com.dianping.cat.service.app.BaseQueryEntity;

public class SpeedQueryEntity extends BaseQueryEntity {

	public SpeedQueryEntity(String query) {
		List<String> strs = Splitters.by(";").split(query);

		try {
			m_date = parseDate(strs.get(0));
			m_id = parseValue(strs.get(1));
			m_network = parseValue(strs.get(2));
			m_version = parseValue(strs.get(3));
			m_platfrom = parseValue(strs.get(4));
			m_city = parseValue(strs.get(5));
			m_operator = parseValue(strs.get(6));
		} catch (Exception e) {
			Cat.logError(e);
		}
	}
}
