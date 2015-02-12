package com.dianping.cat.report.page.storage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.dianping.cat.consumer.storage.model.entity.Machine;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {

	private StorageReport m_report;

	private Set<String> m_operations;

	private String m_countTrend;

	private String m_avgTrend;

	private String m_errorTrend;

	private String m_longTrend;

	public Model(Context ctx) {
		super(ctx);
	}

	public Set<String> getAllOperations() {
		if (m_report != null) {
			return m_report.getOps();
		} else {
			return new HashSet<String>();
		}
	}

	public String getAvgTrend() {
		return m_avgTrend;
	}

	public String getCountTrend() {
		return m_countTrend;
	}

	@Override
	public Action getDefaultAction() {
		return Action.HOURLY_DATABASE;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	@Override
	public Collection<String> getDomains() {
		if (m_report != null) {
			return m_report.getIds();
		} else {
			return new HashSet<String>();
		}
	}

	public String getErrorTrend() {
		return m_errorTrend;
	}

	public String getLongTrend() {
		return m_longTrend;
	}

	public Machine getMachine() {
		Machine machine = new Machine();

		if (m_report != null) {
			Collection<Machine> machines = m_report.getMachines().values();

			if (machines.size() > 0) {
				machine = machines.iterator().next();
			}
		}
		return machine;
	}

	public Set<String> getOperations() {
		return m_operations;
	}

	public StorageReport getReport() {
		return m_report;
	}

	public void setAvgTrend(String avgTrend) {
		m_avgTrend = avgTrend;
	}

	public void setCountTrend(String countTrend) {
		m_countTrend = countTrend;
	}

	public void setErrorTrend(String errorTrend) {
		m_errorTrend = errorTrend;
	}

	public void setLongTrend(String longTrend) {
		m_longTrend = longTrend;
	}

	public void setOperations(Set<String> operations) {
		m_operations = operations;
	}

	public void setReport(StorageReport report) {
		m_report = report;
	}
}
