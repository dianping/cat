package com.dianping.cat.service.app.command;

import java.util.List;
import org.unidal.helper.Splitters;
import com.dianping.cat.Cat;
import com.dianping.cat.service.app.BaseQueryEntity;

public class CommandQueryEntity extends BaseQueryEntity {

	private int m_code = DEFAULT_VALUE;

	private int m_connectType = DEFAULT_VALUE;

	private int m_startMinuteOrder = DEFAULT_VALUE;

	private int m_endMinuteOrder = DEFAULT_VALUE;

	public CommandQueryEntity(boolean showActivity) {
		super();
		if (showActivity) {
			m_id = 1000;
		} else {
			m_id = 1;
		}
	}

	public CommandQueryEntity(String query) {
		List<String> strs = Splitters.by(";").split(query);

		try {
			m_date = parseDate(strs.get(0));
			m_id = parseValue(strs.get(1));
			m_code = parseValue(strs.get(2));
			m_network = parseValue(strs.get(3));
			m_version = parseValue(strs.get(4));
			m_connectType = parseValue(strs.get(5));
			m_platfrom = parseValue(strs.get(6));
			m_city = parseValue(strs.get(7));
			m_operator = parseValue(strs.get(8));
			m_startMinuteOrder = convert2MinuteOrder(strs.get(9));
			m_endMinuteOrder = convert2MinuteOrder(strs.get(10));
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

	public int getConnectType() {
		return m_connectType;
	}

	public int getCode() {
		return m_code;
	}

	public int getId() {
		return m_id;
	}
}