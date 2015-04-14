package com.dianping.cat.report.task.notify.render;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;

public class EventRender extends BaseVisitor {
	private Date m_date;

	private String m_dateStr;

	private String m_domain;

	private String m_eventLink = "http://%s/cat/r/e?op=history&domain=%s&date=%s&reportType=day";

	private Map<Object, Object> m_result = new HashMap<Object, Object>();

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMddHH");

	private int m_totalDays;

	private String m_currentIp;

	private String m_typeGraphLink = "http://%s/cat/r/e?op=historyGraph&domain=%s&date=%s&ip=All&reportType=day&type=%s";

	private List<Type> m_types = new ArrayList<Type>();

	private String m_ip;

	public EventRender(Date date, String domain, int day, String ip) {
		m_domain = domain;
		m_date = date;
		m_dateStr = m_sdf.format(date);
		m_totalDays = day;
		m_ip = ip;
	}

	private String buildEventUrl(Date date) {
		String dateStr = m_sdf.format(m_date);

		return String.format(m_eventLink, m_ip, m_domain, dateStr);
	}

	private String buildGraphUrl(EventType type) {
		return String.format(m_typeGraphLink, m_ip, m_domain, m_dateStr, type.getId());
	}

	public Map<Object, Object> getRenderResult() {
		return m_result;
	}

	@Override
	public void visitEventReport(EventReport eventReport) {
		super.visitEventReport(eventReport);
		Date lastDay = new Date(m_date.getTime() - TimeHelper.ONE_DAY);
		Date lastWeek = new Date(m_date.getTime() - 7 * TimeHelper.ONE_DAY);
		String currentUrl = buildEventUrl(m_date);
		String lastDayUrl = buildEventUrl(lastDay);
		String lastWeekUrl = buildEventUrl(lastWeek);

		m_result.put("current", currentUrl);
		m_result.put("lastDay", lastDayUrl);
		m_result.put("lastWeek", lastWeekUrl);
		m_result.put("types", m_types);
	}

	@Override
	public void visitMachine(Machine machine) {
		m_currentIp = machine.getIp();
		super.visitMachine(machine);
	}

	@Override
	public void visitType(EventType type) {
		if (m_currentIp.equals(Constants.ALL)) {
			Type temp = new Type();

			type.setTps(type.getTotalCount() * 1000d / TimeHelper.ONE_DAY / m_totalDays);
			temp.setType(type);
			temp.setUrl(buildGraphUrl(type));
			m_types.add(temp);
		}
	}

	public static class Type {
		private EventType m_type;

		private String m_url;

		public EventType getType() {
			return m_type;
		}

		public String getUrl() {
			return m_url;
		}

		public void setType(EventType type) {
			m_type = type;
		}

		public void setUrl(String url) {
			m_url = url;
		}
	}

}
