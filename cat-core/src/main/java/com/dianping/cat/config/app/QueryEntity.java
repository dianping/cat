package com.dianping.cat.config.app;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.unidal.helper.Splitters;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;

public class QueryEntity {

	private Date m_date;

	private int m_command;

	private int m_code = DEFAULT_VALUE;

	private int m_network = DEFAULT_VALUE;

	private int m_version = DEFAULT_VALUE;

	private int m_channel = DEFAULT_VALUE;

	private int m_platfrom = DEFAULT_VALUE;

	private int m_city = DEFAULT_VALUE;

	private int m_operator = DEFAULT_VALUE;

	private int m_startMinuteOrder = DEFAULT_VALUE;

	private int m_endMinuteOrder = DEFAULT_VALUE;

	public static final int DEFAULT_VALUE = -1;

	public QueryEntity() {
		m_command = 1;
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		m_date = cal.getTime();
	}

	public QueryEntity(String query) {
		List<String> strs = Splitters.by(";").split(query);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		try {
			m_date = sdf.parse(strs.get(0));
			m_command = parseValue(strs.get(1));
			m_code = parseValue(strs.get(2));
			m_network = parseValue(strs.get(3));
			m_version = parseValue(strs.get(4));
			m_channel = parseValue(strs.get(5));
			m_platfrom = parseValue(strs.get(6));
			m_city = parseValue(strs.get(7));
			m_operator = parseValue(strs.get(8));
			m_startMinuteOrder = convert2MinuteOrder(strs.get(9));
			m_endMinuteOrder = convert2MinuteOrder(strs.get(10));
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private int convert2MinuteOrder(String time) {
		int current = DEFAULT_VALUE;
		
		if (StringUtils.isNotEmpty(time)) {
			try {
				current = Integer.parseInt(time);
			} catch (NumberFormatException e) {
				String[] pair = time.split(":");
				int hour = Integer.parseInt(pair[0]);
				int minute = Integer.parseInt(pair[1]);
				current = hour * 60 + minute;
				current = current - current % 5;
			}
		}
		return current;
	}

	private int parseValue(String str) {
		if (StringUtils.isEmpty(str)) {
			return DEFAULT_VALUE;
		} else {
			return Integer.parseInt(str);
		}
	}

	public Date getDate() {
		return m_date;
	}

	public void setDate(Date date) {
		m_date = date;
	}

	public int getCommand() {
		return m_command;
	}

	public void setCommand(int command) {
		m_command = command;
	}

	public int getCode() {
		return m_code;
	}

	public void setCode(int code) {
		m_code = code;
	}

	public int getNetwork() {
		return m_network;
	}

	public void setNetwork(int network) {
		m_network = network;
	}

	public int getVersion() {
		return m_version;
	}

	public void setVersion(int version) {
		m_version = version;
	}

	public int getChannel() {
		return m_channel;
	}

	public void setChannel(int channel) {
		m_channel = channel;
	}

	public int getPlatfrom() {
		return m_platfrom;
	}

	public void setPlatfrom(int platfrom) {
		m_platfrom = platfrom;
	}

	public int getCity() {
		return m_city;
	}

	public void setCity(int city) {
		m_city = city;
	}

	public int getOperator() {
		return m_operator;
	}

	public void setOperator(int operator) {
		m_operator = operator;
	}

	public int getStartMinuteOrder() {
		return m_startMinuteOrder;
	}

	public void setStartMinuteOrder(int startMinuteOrder) {
		m_startMinuteOrder = startMinuteOrder;
	}

	public int getEndMinuteOrder() {
		return m_endMinuteOrder;
	}

	public void setEndMinuteOrder(int endMinuteOrder) {
		m_endMinuteOrder = endMinuteOrder;
	}

	@Override
	public String toString() {
		return "QueryEntity [m_date=" + m_date + ", m_command=" + m_command + ", m_code=" + m_code + ", m_network="
		      + m_network + ", m_version=" + m_version + ", m_channel=" + m_channel + ", m_platfrom=" + m_platfrom
		      + ", m_city=" + m_city + ", m_operator=" + m_operator + ", m_startMinuteOrder=" + m_startMinuteOrder
		      + ", m_endMinuteOrder=" + m_endMinuteOrder + "]";
	}

}
