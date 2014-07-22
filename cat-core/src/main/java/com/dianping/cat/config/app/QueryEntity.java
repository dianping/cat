package com.dianping.cat.config.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jboss.netty.util.internal.StringUtil;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;

public class QueryEntity {

	private Date m_date;

	private int m_command;

	private int m_code;

	private int m_network;

	private int m_version;

	private int m_channel;

	private int m_platfrom;

	private int m_city;

	private int m_operator;

	public QueryEntity(String query) {
		String[] strs = query.split(";");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		try {
			m_date = sdf.parse(strs[0]);
			m_command = parseValue(strs[1]);
			m_code = parseValue(strs[2]);
			m_network = parseValue(strs[3]);
			m_version = parseValue(strs[4]);
			m_channel = parseValue(strs[5]);
			m_platfrom = parseValue(strs[6]);
			m_city = parseValue(strs[7]);
			m_operator = parseValue(strs[8]);
		} catch (ParseException e) {
			Cat.logError(e);
		}
	}

	private int parseValue(String str) {
		if (StringUtils.isEmpty(str)) {
			return -1;
		} else {
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {
				Cat.logError(e);
			}
		}
		return -1;
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

	@Override
	public String toString() {
		return "QueryEntity [m_date=" + m_date + ", m_command=" + m_command + ", m_code=" + m_code + ", m_network="
		      + m_network + ", m_version=" + m_version + ", m_channel=" + m_channel + ", m_platfrom=" + m_platfrom
		      + ", m_city=" + m_city + ", m_operator=" + m_operator + "]";
	}

}
