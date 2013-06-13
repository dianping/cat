package com.dianping.cat.report.page.heartbeat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

public class Model extends AbstractReportModel<Action, Context> {
	private String m_activeThreadGraph;

	private String m_catMessageOverflowGraph;

	private String m_catMessageProducedGraph;

	private String m_catMessageSizeGraph;

	private String m_catThreadGraph;

	private String m_daemonThreadGraph;

	private String m_diskHistoryGraph;

	private int m_disks;

	private String m_disksGraph;

	private String m_heapUsageGraph;

	private int m_hour;

	private String m_httpThreadGraph;

	private String m_ipAddress;

	private String m_memoryFreeGraph;

	private String m_mobileResponse;

	private String m_newGcCountGraph;

	private String m_noneHeapUsageGraph;

	private String m_oldGcCountGraph;

	private String m_pigeonThreadGraph;

	private HeartbeatReport m_report;

	private DisplayHeartbeat m_result;

	private String m_startedThreadGraph;

	private String m_systemLoadAverageGraph;

	private String m_totalThreadGraph;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getActiveThreadGraph() {
		return m_activeThreadGraph;
	}

	public String getCatMessageOverflowGraph() {
		return m_catMessageOverflowGraph;
	}

	public String getCatMessageProducedGraph() {
		return m_catMessageProducedGraph;
	}

	public String getCatMessageSizeGraph() {
		return m_catMessageSizeGraph;
	}

	public String getCatThreadGraph() {
		return m_catThreadGraph;
	}

	public String getDaemonThreadGraph() {
		return m_daemonThreadGraph;
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public String getDiskHistoryGraph() {
		return m_diskHistoryGraph;
	}

	public int getDiskRows() {
		return (m_disks + 2) / 3;
	}

	public int getDisks() {
		return m_disks;
	}

	public String getDisksGraph() {
		return m_disksGraph;
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

			return StringSortHelper.sortDomain(domainNames);
		}
	}

	public String getHeapUsageGraph() {
		return m_heapUsageGraph;
	}

	public int getHour() {
		return m_hour;
	}

	public String getHttpThreadGraph() {
		return m_httpThreadGraph;
	}

	@Override
	public String getIpAddress() {
		return m_ipAddress;
	}

	public List<String> getIps() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return StringSortHelper.sortDomain(m_report.getIps());
		}
	}

	public String getMemoryFreeGraph() {
		return m_memoryFreeGraph;
	}

	public String getMobileResponse() {
		return m_mobileResponse;
	}

	public String getNewGcCountGraph() {
		return m_newGcCountGraph;
	}

	public String getNoneHeapUsageGraph() {
		return m_noneHeapUsageGraph;
	}

	public String getOldGcCountGraph() {
		return m_oldGcCountGraph;
	}

	public String getPigeonThreadGraph() {
		return m_pigeonThreadGraph;
	}

	public HeartbeatReport getReport() {
		return m_report;
	}

	public DisplayHeartbeat getResult() {
		return m_result;
	}

	public String getStartedThreadGraph() {
		return m_startedThreadGraph;
	}

	public String getSystemLoadAverageGraph() {
		return m_systemLoadAverageGraph;
	}

	public String getTotalThreadGraph() {
		return m_totalThreadGraph;
	}

	public void setActiveThreadGraph(String activeThreadGraph) {
		m_activeThreadGraph = activeThreadGraph;
	}

	public void setCatMessageOverflowGraph(String catMessageOverflowGraph) {
		m_catMessageOverflowGraph = catMessageOverflowGraph;
	}

	public void setCatMessageProducedGraph(String catMessageProducedGraph) {
		m_catMessageProducedGraph = catMessageProducedGraph;
	}

	public void setCatMessageSizeGraph(String catMessageSizeGraph) {
		m_catMessageSizeGraph = catMessageSizeGraph;
	}

	public void setCatThreadGraph(String catThreadGraph) {
		m_catThreadGraph = catThreadGraph;
	}

	public void setDaemonThreadGraph(String daemonThreadGraph) {
		m_daemonThreadGraph = daemonThreadGraph;
	}

	public void setDiskHistoryGraph(String diskHistoryGraph) {
		m_diskHistoryGraph = diskHistoryGraph;
	}

	public void setDisks(int disks) {
		m_disks = disks;
	}

	public void setDisksGraph(String disksGraph) {
		m_disksGraph = disksGraph;
	}

	public void setHeapUsageGraph(String heapUsageGraph) {
		m_heapUsageGraph = heapUsageGraph;
	}

	public void setHour(int hour) {
		m_hour = hour;
	}

	public void setHttpThreadGraph(String httpThreadGraph) {
		m_httpThreadGraph = httpThreadGraph;
	}

	@Override
	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	public void setMemoryFreeGraph(String memoryFreeGraph) {
		m_memoryFreeGraph = memoryFreeGraph;
	}

	public void setMobileResponse(String mobileResponse) {
		m_mobileResponse = mobileResponse;
	}

	public void setNewGcCountGraph(String gcCountGraph) {
		m_newGcCountGraph = gcCountGraph;
	}

	public void setNoneHeapUsageGraph(String noneHeapUsageGraph) {
		m_noneHeapUsageGraph = noneHeapUsageGraph;
	}

	public void setOldGcCountGraph(String gcCountGraph) {
		m_oldGcCountGraph = gcCountGraph;
	}

	public void setPigeonThreadGraph(String pigeonThreadGraph) {
		m_pigeonThreadGraph = pigeonThreadGraph;
	}

	public void setReport(HeartbeatReport report) {
		m_report = report;
	}

	public void setResult(DisplayHeartbeat result) {
		m_result = result;
	}

	public void setStartedThreadGraph(String startedThreadGraph) {
		m_startedThreadGraph = startedThreadGraph;
	}

	public void setSystemLoadAverageGraph(String systemLoadAverageGraph) {
		m_systemLoadAverageGraph = systemLoadAverageGraph;
	}

	public void setTotalThreadGraph(String totalThreadGraph) {
		m_totalThreadGraph = totalThreadGraph;
	}

}
