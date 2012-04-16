package com.dianping.cat.report.page.ip;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

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
