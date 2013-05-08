package com.dianping.cat.system.page.abtest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.Validate;
import org.codehaus.plexus.util.StringUtils;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.system.SystemPage;

public class Payload implements ActionPayload<SystemPage, Action> {
	private SystemPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("status")
	private String m_status;

	@FieldMeta("pageNum")
	private int m_pageNum;

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("description")
	private String m_description;

	@FieldMeta("startDate")
	private Date m_startDate;

	@FieldMeta("endDate")
	private Date m_endDate;

	@FieldMeta("domains")
	private String[] m_domains;

	@FieldMeta("strategyId")
	private int m_strategyId;

	@FieldMeta("strategyConfig")
	private String m_strategyConfig;

	private String m_startDateStr;

	private String m_endDateStr;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");

	@FieldMeta("enable")
	private boolean m_enableAbtest;

	@FieldMeta("ids")
	private String m_ids;

	@FieldMeta("abtestId")
	private int abtestId;

	public void setAction(String action) {
		if (action.equalsIgnoreCase(Action.REPORT.getName())) {
			m_action = Action.getByName(action, Action.REPORT);
		} else if (action.equalsIgnoreCase(Action.LIST.getName())) {
			m_action = Action.getByName(action, Action.LIST);
		} else if (action.equalsIgnoreCase(Action.DOCREATE.getName())) {
			m_action = Action.getByName(action, Action.DOCREATE);
		} else {
			m_action = Action.getByName(action, Action.VIEW);
		}
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getStatus() {
		return m_status;
	}

	public void setStatus(String status) {
		AbtestStatus abstatus = AbtestStatus.getByName(status, null);

		if (abstatus != null) {
			m_status = abstatus.name().toLowerCase();
		} else {
			m_status = "all";
		}
	}

	public int getPageNum() {
		return m_pageNum;
	}

	public void setPageNum(int pageNum) {
		m_pageNum = pageNum;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	public String getReportType() {
		return "";
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.ABTEST);
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		this.m_name = name;
	}

	public String getDescription() {
		return m_description;
	}

	public void setDescription(String description) {
		this.m_description = description;
	}

	public Date getStartDate() {
		return m_startDate;
	}

	public Date getEndDate() {
		return m_endDate;
	}

	public void setStartDate(String startDate) {
		try {
			m_startDateStr = startDate;
			m_startDate = m_sdf.parse(startDate);
		} catch (ParseException e) {
			Cat.logError(e);
		}
	}

	public void setEndDate(String endDate) {
		try {
			m_endDateStr = endDate;
			m_endDate = m_sdf.parse(endDate);
		} catch (ParseException e) {
			Cat.logError(e);
		}
	}

	public String getStartDateStr() {
		return m_startDateStr;
	}

	public String getEndDateStr() {
		return m_endDateStr;
	}

	public String[] getDomains() {
		return m_domains;
	}

	public void setDomains(String[] domains) {
		this.m_domains = domains;
	}

	public int getStrategyId() {
		return m_strategyId;
	}

	public void setStrategyId(int strategyId) {
		this.m_strategyId = strategyId;
	}

	public String getStrategyConfig() {
		return m_strategyConfig;
	}

	public void setStrategyConfig(String strategyConfig) {
		this.m_strategyConfig = strategyConfig;
	}

	public boolean isEnableAbtest() {
		return m_enableAbtest;
	}

	public void setEnableAbtest(boolean enableAbtest) {
		m_enableAbtest = enableAbtest;
	}

	public String getIds() {
		return m_ids;
	}

	public void setIds(String ids) {
		m_ids = ids;
	}

	public int getAbtestId() {
		return abtestId;
	}

	public void setAbtestId(int abtestId) {
		this.abtestId = abtestId;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
		// 验证doCreate的参数
		if (m_action == Action.DOCREATE) {
			try {
				Validate.isTrue(StringUtils.isNotBlank(m_name), "'ABTest Name' is required");
				Validate.isTrue(m_startDate != null, "'Start Time' is required, and formated 'yyyy-MM-dd hh:mm'");
				Validate.isTrue(m_endDate != null, "'End Time' is required, and formated 'yyyy-MM-dd hh:mm'");
				Validate.isTrue(m_domains != null && m_domains.length > 0, "'Domains' is required, choose one at least");
				for (String domain : m_domains) {
					Validate.isTrue(StringUtils.isNotBlank(domain), "'Domains' should not be blank");
				}
				Validate.isTrue(m_strategyId > 0, "'Strategy' is required, choose one at least");
			} catch (IllegalArgumentException e) {
				ctx.setException(e);
			}
		}
		if (m_status == null) {
			m_status = "all";
		}
	}
}
