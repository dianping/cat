package com.dianping.cat.report.page.heartbeat;

import java.util.ArrayList;
import java.util.List;

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
	
	private String m_diskFreeGraph;
	
	private String m_diskUseableGraph;
	
	private String m_gcCountGraph;

	private String m_heapUsageGraph;

	private int m_hour;

	private String m_ipAddress;

	private String m_noneHeapUsageGraph;
	
	private String m_memoryFreeGraph;

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

	public String getDaemonThreadGraph() {
   	return m_daemonThreadGraph;
   }

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public String getDiskFreeGraph() {
   	return m_diskFreeGraph;
   }

	public String getDiskUseableGraph() {
   	return m_diskUseableGraph;
   }

	@Override
	public String getDomain() {
		if (m_report == null) {
			return getDisplayDomain();
		} else {
			return m_report.getDomain();
		}
	}

	public List<String> getDomains() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return StringSortHelper.sortDomain(m_report.getDomainNames());
		}
	}

	public String getGcCountGraph() {
   	return m_gcCountGraph;
   }

	public String getHeapUsageGraph() {
   	return m_heapUsageGraph;
   }

	public int getHour() {
		return m_hour;
	}

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

	public String getNoneHeapUsageGraph() {
   	return m_noneHeapUsageGraph;
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

	public void setDaemonThreadGraph(String daemonThreadGraph) {
   	m_daemonThreadGraph = daemonThreadGraph;
   }

	public void setDiskFreeGraph(String diskFreeGraph) {
   	m_diskFreeGraph = diskFreeGraph;
   }

	public void setDiskUseableGraph(String diskUseableGraph) {
   	m_diskUseableGraph = diskUseableGraph;
   }

	public void setGcCountGraph(String gcCountGraph) {
   	m_gcCountGraph = gcCountGraph;
   }

	public void setHeapUsageGraph(String heapUsageGraph) {
   	m_heapUsageGraph = heapUsageGraph;
   }

	public void setHour(int hour) {
		m_hour = hour;
	}

	public void setIpAddress(String ipAddress) {
   	m_ipAddress = ipAddress;
   }

	public void setNoneHeapUsageGraph(String noneHeapUsageGraph) {
   	m_noneHeapUsageGraph = noneHeapUsageGraph;
   }

	public void setReport(HeartbeatReport report) {
		m_report = report;
	}

	public String getCatThreadGraph() {
   	return m_catThreadGraph;
   }

	public void setCatThreadGraph(String catThreadGraph) {
   	m_catThreadGraph = catThreadGraph;
   }

	public String getPigeonThreadGraph() {
   	return m_pigeonThreadGraph;
   }

	public void setPigeonThreadGraph(String pigeonThreadGraph) {
   	m_pigeonThreadGraph = pigeonThreadGraph;
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

	public String getMemoryFreeGraph() {
   	return m_memoryFreeGraph;
   }

	public void setMemoryFreeGraph(String memoryFreeGraph) {
   	m_memoryFreeGraph = memoryFreeGraph;
   }
	
}
