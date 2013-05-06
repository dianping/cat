package com.dianping.cat.report.page.query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("queryType")
	private String m_queryType;
	
	@FieldMeta("reportLevel")
	private String m_reportLevel;

	@FieldMeta("queryDomain")
	private String m_queryDomain;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("start")
	private Date m_start;

	@FieldMeta("end")
	private Date m_end;
	
	private String m_startStr;
	
	private String m_endStr;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("MM/dd/yyyy");

	public Payload() {
		super(ReportPage.QUERY);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public Date getEnd() {
		return m_end;
	}

	public String getEndStr() {
		return m_endStr;
	}

	public String getName() {
		return m_name;
	}

	public String getQueryDomain() {
		return m_queryDomain;
	}

	public String getQueryType() {
		return m_queryType;
	}

	public String getReportLevel() {
		return m_reportLevel;
	}

	public Date getStart() {
		return m_start;
	}

	public String getStartStr() {
		return m_startStr;
	}

	public String getType() {
		return m_type;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setEnd(String end) {
		try {
			m_endStr = end;
			m_end = m_sdf.parse(end);
		} catch (ParseException e) {
			Cat.logError(e);
		}
	}

	public void setName(String name) {
		m_name = name;
	}

	public void setQueryDomain(String queryDomain) {
		m_queryDomain = queryDomain;
	}

	public void setQueryType(String queryType) {
		m_queryType = queryType;
	}

	public void setReportLevel(String reportLevel) {
		m_reportLevel = reportLevel;
	}

	public void setStart(String start) {
		try {
			m_startStr = start;
			m_start = m_sdf.parse(start);
		} catch (ParseException e) {
			Cat.logError(e);
		}
	}

	public void setType(String type) {
		m_type = type;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
