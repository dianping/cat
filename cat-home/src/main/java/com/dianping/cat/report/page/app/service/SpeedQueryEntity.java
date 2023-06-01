package com.dianping.cat.report.page.app.service;

import com.dianping.cat.Cat;
import org.unidal.helper.Splitters;

import java.util.List;

public class SpeedQueryEntity extends BaseQueryEntity {

	private int m_startMinuteOrder = DEFAULT_VALUE;

	private int m_endMinuteOrder = DEFAULT_VALUE;

	public SpeedQueryEntity() {
		super();
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
			m_startMinuteOrder = convert2MinuteOrder(strs.get(8));
			m_endMinuteOrder = convert2MinuteOrder(strs.get(9));
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public int getStartMinuteOrder() {
		return m_startMinuteOrder;
	}

	public int getEndMinuteOrder() {
		return m_endMinuteOrder;
	}
}
