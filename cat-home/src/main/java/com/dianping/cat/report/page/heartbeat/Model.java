package com.dianping.cat.report.page.heartbeat;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

public class Model extends AbstractReportModel<Action, Context> {
	private HeartbeatReport m_report;
	
	private DisplayHeartbeat m_result;

	private int m_hour;
	
	private String m_ipAddress;
	
	private String m_activeThreadGraph;
	
	private String m_daemonThreadGraph;
	
	private String m_totalThreadGraph;
	
	private String m_startedThreadGraph;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getActiveThreadGraph() {
   	return m_activeThreadGraph;
   }

	public String getDaemonThreadGraph() {
   	return m_daemonThreadGraph;
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
	
	public List<String> getDomains() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return StringSortHelper.sortDomain(m_report.getDomainNames());
		}
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

	public HeartbeatReport getReport() {
		return m_report;
	}

	public DisplayHeartbeat getResult() {
   	return m_result;
   }

	public String getStartedThreadGraph() {
   	return m_startedThreadGraph;
   }

	public String getTotalThreadGraph() {
   	return m_totalThreadGraph;
   }

	public void setActiveThreadGraph(String activeThreadGraph) {
   	m_activeThreadGraph = activeThreadGraph;
   }

	public void setDaemonThreadGraph(String daemonThreadGraph) {
   	m_daemonThreadGraph = daemonThreadGraph;
   }

	public void setHour(int hour) {
		m_hour = hour;
	}

	public void setIpAddress(String ipAddress) {
   	m_ipAddress = ipAddress;
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

	public void setTotalThreadGraph(String totalThreadGraph) {
   	m_totalThreadGraph = totalThreadGraph;
   }
	
}
