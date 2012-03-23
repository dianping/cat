package com.dianping.cat.report.page.ip;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {
	private IpReport m_report;

	private List<DisplayModel> m_displayModels;

	private int m_hour;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public List<DisplayModel> getDisplayModels() {
		return m_displayModels;
	}

	@Override
	public String getDomain() {
		if (m_report == null) {
<<<<<<< HEAD
			return getDefaultDomain();
=======
			return getDisplayDomain();
>>>>>>> 8fba9da1445e5bf08a418057a70f787f909d543f
		} else {
			return m_report.getDomain();
		}
	}

	public Set<String> getDomains() {
		if (m_report == null) {
			return Collections.emptySet();
		} else {
			return m_report.getAllDomains().getDomains();
		}
	}

	public int getHour() {
		return m_hour;
	}

	public IpReport getReport() {
		return m_report;
	}

	public void setDisplayModels(List<DisplayModel> models) {
		m_displayModels = models;
	}

	public void setHour(int hour) {
		m_hour = hour;
	}

	public void setReport(IpReport report) {
		m_report = report;
	}
	
	public static class DisplayModel {
		private String m_address;

		private int m_lastOne;

		private int m_lastFive;

		private int m_lastFifteen;

		public DisplayModel(String address) {
			m_address = address;
		}

		public String getAddress() {
			return m_address;
		}

		public int getLastFifteen() {
			return m_lastFifteen;
		}

		public int getLastFive() {
			return m_lastFive;
		}

		public int getLastOne() {
			return m_lastOne;
		}

		public void process(int current, int minute, int count) {
			if (current == minute) {
				m_lastOne += count;
				m_lastFive += count;
				m_lastFifteen += count;
			} else if (current < minute) {
				// ignore it
			} else if (current - 5 < minute) {
				m_lastFive += count;
				m_lastFifteen += count;
			} else if (current - 15 < minute) {
				m_lastFifteen += count;
			}
		}
	}
}
