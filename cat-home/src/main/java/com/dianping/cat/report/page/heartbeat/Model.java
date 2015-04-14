package com.dianping.cat.report.page.heartbeat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.heartbeat.HeartbeatSvgGraph.ExtensionGroup;

@ModelMeta(HeartbeatAnalyzer.ID)
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	private int m_hour;

	private String m_ipAddress;

	@EntityMeta
	private HeartbeatReport m_report;

	private HeartbeatSvgGraph m_result;

	private List<String> m_extensionGroups = new ArrayList<String>();

	private int m_extensionCount;

	private String m_extensionHistoryGraphs;

	private Map<String, ExtensionGroup> m_extensionGraph = new HashMap<String, ExtensionGroup>();

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public String getDomain() {
		if (m_report == null) {
			return getDisplayDomain();
		} else {
			return m_report.getDomain();
		}
	}

	@Override
	public List<String> getDomains() {
		if (m_report == null) {
			ArrayList<String> arrayList = new ArrayList<String>();

			arrayList.add(getDomain());
			return arrayList;
		} else {
			Set<String> domainNames = m_report.getDomainNames();

			return SortHelper.sortDomain(domainNames);
		}
	}

	public int getExtensionCount() {
		return m_extensionCount;
	}

	public Map<String, ExtensionGroup> getExtensionGraph() {
		return m_extensionGraph;
	}

	public List<String> getExtensionGroups() {
		return m_extensionGroups;
	}

	public String getExtensionHistoryGraphs() {
		return m_extensionHistoryGraphs;
	}

	public int getHour() {
		return m_hour;
	}

	@Override
	public String getIpAddress() {
		return m_ipAddress;
	}

	public List<String> getIps() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return SortHelper.sortIpAddress(m_report.getIps());
		}
	}

	public HeartbeatReport getReport() {
		return m_report;
	}

	public HeartbeatSvgGraph getResult() {
		return m_result;
	}

	public void setExtensionCount(int extensionCount) {
		m_extensionCount = extensionCount;
	}

	public void setExtensionGraph(Map<String, ExtensionGroup> extensionGraph) {
		m_extensionGraph = extensionGraph;
	}

	public void setExtensionGroups(List<String> extensionGroups) {
		m_extensionGroups = extensionGroups;
	}

	public void setExtensionHistoryGraphs(String extensionHistoryGraphs) {
		m_extensionHistoryGraphs = extensionHistoryGraphs;
	}

	public void setHour(int hour) {
		m_hour = hour;
	}

	@Override
	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	public void setReport(HeartbeatReport report) {
		m_report = report;
	}

	public void setResult(HeartbeatSvgGraph result) {
		m_result = result;
	}

}
