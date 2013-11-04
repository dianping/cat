package com.dianping.cat.system.page.abtest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.page.abtest.util.AbtestStatus;

public class Payload implements ActionPayload<SystemPage, Action> {
	private SystemPage m_page;

	@FieldMeta("op")
	private Action m_action;

	/* ===============Abtest================ */

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("owner")
	private String m_owner;

	@FieldMeta("description")
	private String m_description;

	@FieldMeta("startDate")
	private Date m_startDate;

	@FieldMeta("endDate")
	private Date m_endDate;

	@FieldMeta("domains")
	private String[] m_domains;

	@FieldMeta("conditions")
	private String m_conditions;

	@FieldMeta("goals")
	private String m_conversionGoals;

	/* ===============Abtest Controls================ */

	@FieldMeta("enable")
	private boolean m_enableAbtest;

	@FieldMeta("suspend")
	private int m_disableAbtest;

	@FieldMeta("ids")
	private String m_ids;

	@FieldMeta("status")
	private String m_status;

	@FieldMeta("pageNum")
	private int m_pageNum;

	@FieldMeta("id")
	private int id;

	@FieldMeta("lastUpdateTime")
	private long m_lastUpdateTime;

	/* ===============GroupStrategy================ */

	@FieldMeta("strategyId")
	private int m_strategyId;

	@FieldMeta("strategyConfig")
	private String m_strategyConfig;

	@FieldMeta("groupStrategyName")
	private String m_groupStrategyName;

	@FieldMeta("groupStrategyClassName")
	private String m_groupStrategyClassName;

	@FieldMeta("groupStrategyFullName")
	private String m_groupStrategyFullName;

	@FieldMeta("groupStrategyDescriptor")
	private String m_groupStrategyDescriptor;

	@FieldMeta("groupStrategyDescription")
	private String m_groupStrategyDescription;

	@FieldMeta("srcCode")
	private String m_srcCode;

	/* ===============Caculator================ */
	@FieldMeta("pv")
	private int m_pv = 0;

	@FieldMeta("conversionRate")
	private int m_conversionRate;

	/* ===============Report================ */
	@FieldMeta("selectMetricType")
	private String m_selectMetricType;

	@FieldMeta("period")
	private String m_period;

	private boolean m_addGs;

	private String m_startDateStr;

	private String m_endDateStr;

	private SimpleDateFormat m_dataFormater = new SimpleDateFormat("yyyy-MM-dd hh:mm");

	@Override
	public Action getAction() {
		return m_action;
	}

	public boolean getAddGs() {
		return m_addGs;
	}

	public String getConditions() {
		return m_conditions;
	}

	public String getConversionGoals() {
		return m_conversionGoals;
	}

	public int getConversionRate() {
		return m_conversionRate;
	}

	public String getDescription() {
		return m_description;
	}

	public int getDisableAbtest() {
		return m_disableAbtest;
	}

	public String[] getDomains() {
		return m_domains;
	}

	public Date getEndDate() {
		return m_endDate;
	}

	public String getEndDateStr() {
		return m_endDateStr;
	}

	public String getGroupStrategyClassName() {
		return m_groupStrategyClassName;
	}

	public String getGroupStrategyDescription() {
		return m_groupStrategyDescription;
	}

	public String getGroupStrategyDescriptor() {
		return m_groupStrategyDescriptor;
	}

	public String getGroupStrategyFullName() {
		return m_groupStrategyFullName;
	}

	public String getGroupStrategyName() {
		return m_groupStrategyName;
	}

	public int getId() {
		return id;
	}

	public String[] getIds() {
		if (m_ids != null) {
			String[] ids = m_ids.split("-");
			return ids;
		} else {
			return null;
		}
	}

	public long getLastUpdateTime() {
		return m_lastUpdateTime;
	}

	public String getName() {
		return m_name;
	}

	public String getOwner() {
		return m_owner;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	public int getPageNum() {
		return m_pageNum;
	}

	public String getPeriod() {
		return m_period;
	}

	public int getPv() {
		return m_pv;
	}

	public String getReportType() {
		return "";
	}

	public String getSelectMetricType() {
		return m_selectMetricType;
	}

	public String getSrcCode() {
		return m_srcCode;
	}

	public Date getStartDate() {
		return m_startDate;
	}

	public String getStartDateStr() {
		return m_startDateStr;
	}

	public String getStatus() {
		return m_status;
	}

	public String getStrategyConfig() {
		return m_strategyConfig;
	}

	public int getStrategyId() {
		return m_strategyId;
	}

	public boolean isEnableAbtest() {
		return m_enableAbtest;
	}

	public void setAction(String action) {
		if (action.equalsIgnoreCase(Action.REPORT.getName())) {
			m_action = Action.getByName(action, Action.REPORT);
		} else if (action.equalsIgnoreCase(Action.CREATE.getName())) {
			m_action = Action.getByName(action, Action.CREATE);
		} else {
			m_action = Action.getByName(action, Action.VIEW);
		}
	}

	public void setAddGs(boolean addGs) {
		m_addGs = addGs;
	}

	public void setConditions(String conditions) {
		m_conditions = conditions;
	}

	public void setConversionRate(int conversionRate) {
		m_conversionRate = conversionRate;
	}

	public void setDescription(String description) {
		this.m_description = description;
	}

	public void setDisableAbtest(int disableAbtest) {
		m_disableAbtest = disableAbtest;
	}

	public void setDomains(String[] domains) {
		this.m_domains = domains;
	}

	public void setEndDate(String endDate) {
		try {
			m_endDateStr = endDate;
			m_endDate = m_dataFormater.parse(endDate);
		} catch (ParseException e) {
			Cat.logError(e);
		}
	}

	public void setEndDate2(Date endDate) {
		m_endDate = endDate;
	}

	public void setGoals(String goals) {
		m_conversionGoals = goals;
	}

	public void setGroupStrategyClassName(String groupStrategyClassName) {
		m_groupStrategyClassName = groupStrategyClassName;
	}

	public void setGroupStrategyDescription(String groupStrategyDescription) {
		m_groupStrategyDescription = groupStrategyDescription;
	}

	public void setGroupStrategyDescriptor(String groupStrategyDescriptor) {
		m_groupStrategyDescriptor = groupStrategyDescriptor;
	}

	public void setGroupStrategyFullName(String groupStrategyFullName) {
		m_groupStrategyFullName = groupStrategyFullName;
	}

	public void setGroupStrategyName(String groupStrategyName) {
		m_groupStrategyName = groupStrategyName;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setIds(String ids) {
		m_ids = ids;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		m_lastUpdateTime = lastUpdateTime;
	}

	public void setName(String name) {
		this.m_name = name;
	}

	public void setOwner(String owner) {
		m_owner = owner;
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.ABTEST);
	}

	public void setPageNum(int pageNum) {
		m_pageNum = pageNum;
	}

	public void setPeriod(String period) {
		m_period = period;
	}

	public void setPv(int pv) {
		m_pv = pv;
	}

	public void setSelectMetricType(String selectMetricType) {
		m_selectMetricType = selectMetricType;
	}

	public void setSrcCode(String srcCode) {
		m_srcCode = srcCode;
	}

	public void setStartDate(String startDate) {
		try {
			m_startDateStr = startDate;
			m_startDate = m_dataFormater.parse(startDate);
		} catch (ParseException e) {
			Cat.logError(e);
		}
	}

	public void setStartDate2(Date startDate) {
		m_startDate = startDate;
	}

	public void setStatus(String status) {
		AbtestStatus abstatus = AbtestStatus.getByName(status, null);

		if (abstatus != null) {
			m_status = abstatus.name().toLowerCase();
		} else {
			m_status = "all";
		}
	}

	public void setStrategyConfig(String strategyConfig) {
		this.m_strategyConfig = strategyConfig;
	}

	public void setStrategyId(int strategyId) {
		this.m_strategyId = strategyId;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}

		if (m_status == null) {
			m_status = "all";
		}

		if (m_disableAbtest != -1 && m_disableAbtest != 1) {
			m_disableAbtest = 0;
		}

		if (ctx.getHttpServletRequest().getMethod().equalsIgnoreCase("post")) {
			if (m_action == Action.CREATE) {
				try {
					Validate.isTrue(StringUtils.isNotBlank(m_name), "'ABTest Name' is required");
					Validate.isTrue(m_domains != null && m_domains.length > 0, "'Domains' is required, choose one at least");
					for (String domain : m_domains) {
						Validate.isTrue(StringUtils.isNotBlank(domain), "'Domains' should not be blank");
					}
					Validate.isTrue(m_strategyId > 0, "'Strategy' is required, choose one at least");
				} catch (IllegalArgumentException e) {
					ctx.setException(e);
				}
			} else if (m_action == Action.AJAX_ADDGROUPSTRATEGY) {
				try {
					Validate.isTrue(StringUtils.isNotBlank(m_groupStrategyName), "'GroupStrategy Name' is required");
					Validate.isTrue(StringUtils.isNotBlank(m_groupStrategyClassName),
					      "'GroupStrategy ClassName' is required");
				} catch (IllegalArgumentException e) {
					ctx.setException(e);
				}
			}
		}
	}
}
