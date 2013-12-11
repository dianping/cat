package com.dianping.cat.system.page.abtest;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.abtest.model.entity.AbtestModel;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.dal.abtest.GroupStrategy;
import com.dianping.cat.report.abtest.entity.AbtestReport;
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.page.abtest.handler.ListViewModel;
import com.dianping.cat.system.page.abtest.handler.ListViewModel.AbtestItem;
import com.dianping.cat.system.page.abtest.handler.ReportHandler.DataSets;

public class Model extends ViewModel<SystemPage, Action, Context> {
	private String m_domain;

	private Date m_date;

	private ABTestEntity m_entity;

	private ListViewModel m_listViewModel;

	private Map<String, List<Project>> m_projectMap;

	private Map<String, MetricItemConfig> m_metricConfigItem;

	private List<GroupStrategy> m_groupStrategyList;

	private AbtestItem m_abtest;

	private AbtestModel m_abtestModel;

	private AbtestReport m_report;

	private List<DataSets> m_dataSets;

	private String m_ipAddress;

	public Model(Context ctx) {
		super(ctx);
	}

	public AbtestItem getAbtest() {
		return m_abtest;
	}

	public AbtestModel getAbtestModel() {
		return m_abtestModel;
	}

	public List<DataSets> getDataSets() {
		return m_dataSets;
	}

	public Date getDate() {
		return m_date;
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public String getDomain() {
		return m_domain;
	}

	public ABTestEntity getEntity() {
		return m_entity;
	}

	public List<GroupStrategy> getGroupStrategyList() {
		return m_groupStrategyList;
	}

	public String getIpAddress() {
		return m_ipAddress;
	}

	public ListViewModel getListViewModel() {
		return m_listViewModel;
	}

	public Map<String, MetricItemConfig> getMetricConfigItem() {
		return m_metricConfigItem;
	}

	public Map<String, List<Project>> getProjectMap() {
		return m_projectMap;
	}

	public AbtestReport getReport() {
		return m_report;
	}

	public String getReportType() {
		return "";
	}

	public void setAbtest(AbtestItem abtest) {
		m_abtest = abtest;
	}

	public void setAbtestModel(AbtestModel abtestModel) {
		m_abtestModel = abtestModel;
	}

	public void setDataSets(List<DataSets> dataSets) {
		m_dataSets = dataSets;
	}

	public void setDate(Date date) {
		m_date = date;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setEntity(ABTestEntity entity) {
		m_entity = entity;
	}

	public void setGroupStrategyList(List<GroupStrategy> groupStrategyList) {
		m_groupStrategyList = groupStrategyList;
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	public void setListViewModel(ListViewModel listViewModel) {
		m_listViewModel = listViewModel;
	}

	public void setMetricConfigItem(Map<String, MetricItemConfig> metricItemConfig) {
		m_metricConfigItem = metricItemConfig;
	}

	public void setProjectMap(Map<String, List<Project>> projectMap) {
		m_projectMap = projectMap;
	}

	public void setReport(AbtestReport report) {
		m_report = report;
	}
}
