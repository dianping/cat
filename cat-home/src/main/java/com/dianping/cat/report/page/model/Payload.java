package com.dianping.cat.report.page.model;

import java.util.Arrays;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.PathMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.service.ModelPeriod;

public class Payload implements ActionPayload<ReportPage, Action> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("ip")
	private String m_ipAddress;

	@FieldMeta("messageId")
	private String m_messageId;

	@FieldMeta("waterfall")
	private boolean m_waterfall;

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("city")
	private String m_city;

	@FieldMeta("channel")
	private String m_channel;

	private ReportPage m_page;

	@PathMeta("path")
	private String[] m_path;

	@FieldMeta("thread")
	private String m_threadId;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("metricType")
	private String m_metricType;

	@FieldMeta("database")
	private String m_database;

	@FieldMeta("province")
	private String m_province;
	
	@FieldMeta("queryType")
	private String m_queryType;

	@FieldMeta("cdn")
	private String m_cdn = "ALL";
	
	public String getQueryType() {
   	return m_queryType;
   }

	public void setQueryType(String queryType) {
   	m_queryType = queryType;
   }

	@Override
	public Action getAction() {
		return m_action;
	}
	
	public String getCdn() {
		return m_cdn;
	}
	
	public String getChannel() {
   	return m_channel;
   }
	
	public String getCity() {
   	return m_city;
   }

	public String getDatabase() {
		return m_database;
	}

	public String getDomain() {
		if (m_path.length > 1) {
			return m_path[1];
		} else {
			return null;
		}
	}

	public String getIpAddress() {
		return m_ipAddress;
	}

	public String getMessageId() {
		return m_messageId;
	}

	public String getMetricType() {
		return m_metricType;
	}

	public String getName() {
		return m_name;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public ModelPeriod getPeriod() {
		if (m_path.length > 2) {
			return ModelPeriod.getByName(m_path[2], ModelPeriod.CURRENT);
		} else {
			return ModelPeriod.CURRENT;
		}
	}

	public String getProvince() {
		return m_province;
	}

	public String getReport() {
		if (m_path.length > 0) {
			return m_path[0];
		} else {
			return null;
		}
	}

	public String getThreadId() {
		return m_threadId;
	}
	
	public String getType() {
		return m_type;
	}

	public boolean isWaterfall() {
		return m_waterfall;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.XML);
	}

	public void setCdn(String cdn) {
		m_cdn = cdn;
	}

	public void setChannel(String channel) {
   	m_channel = channel;
   }

	public void setCity(String city) {
   	m_city = city;
   }

	public void setDatabase(String database) {
		m_database = database;
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	public void setMessageId(String messageId) {
		m_messageId = messageId;
	}

	public void setMeticType(String metricType) {
		m_metricType = metricType;
	}

	public void setName(String name) {
		m_name = name;
	}
	
	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.MODEL);
	}
	
	public void setPath(String[] path) {
		if (path == null) {
			m_path = new String[0];
		} else {
			m_path = Arrays.copyOf(path, path.length);
		}
	}

	public void setProvince(String province) {
		m_province = province;
	}

	public void setThreadId(String threadId) {
		m_threadId = threadId;
	}

	public void setType(String type) {
		m_type = type;
	}

	public void setWaterfall(boolean waterfall) {
		m_waterfall = waterfall;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.XML;
		}
	}
}
