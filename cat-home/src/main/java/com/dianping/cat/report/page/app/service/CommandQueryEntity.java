package com.dianping.cat.report.page.app.service;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.app.AppAlarmRuleParam;
import org.unidal.helper.Splitters;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CommandQueryEntity extends BaseQueryEntity {

	public static final int DEFAULT_COMMAND = 1;

	protected int m_code = DEFAULT_VALUE;

	protected int m_connectType = DEFAULT_VALUE;

	protected int m_source = DEFAULT_VALUE;

	private int m_startMinuteOrder = DEFAULT_VALUE;

	private int m_endMinuteOrder = DEFAULT_VALUE;

	public CommandQueryEntity() {
		super();
		m_id = DEFAULT_COMMAND;
	}

	public CommandQueryEntity(Date date, AppAlarmRuleParam param, int start, int end) {
		m_date = date;
		m_startMinuteOrder = start;
		m_endMinuteOrder = end;
		m_id = param.getCommand();
		m_code = param.getCode();
		m_network = param.getNetwork();
		m_version = param.getVersion();
		m_connectType = param.getConnectType();
		m_platfrom = param.getPlatform();
		m_city = param.getCity();
		m_operator = param.getOperator();
	}

	public CommandQueryEntity(int id) {
		super();
		m_id = id;
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int minute = Calendar.getInstance().get(Calendar.MINUTE);
		m_endMinuteOrder = hour * 60 + minute;
		m_endMinuteOrder = m_endMinuteOrder - m_endMinuteOrder % 5;
		m_startMinuteOrder = m_endMinuteOrder - 30;

		if (m_startMinuteOrder < 0) {
			m_startMinuteOrder = DEFAULT_VALUE;
		}
	}

	public CommandQueryEntity(String query) {
		List<String> strs = Splitters.by(";").split(query);
		int size = strs.size();

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

			if (size == 12) {
				m_source = parseValue(strs.get(9));
			}
			m_startMinuteOrder = convert2MinuteOrder(strs.get(size - 2));
			m_endMinuteOrder = convert2MinuteOrder(strs.get(size - 1));
		} catch (Exception e) {
			Cat.logError(query, e);
		}
	}

	public int getCode() {
		return m_code;
	}

	public int getConnectType() {
		return m_connectType;
	}

	public int getEndMinuteOrder() {
		return m_endMinuteOrder;
	}

	public int getId() {
		return m_id;
	}

	public int getSource() {
		return m_source;
	}

	public int getStartMinuteOrder() {
		return m_startMinuteOrder;
	}

}
