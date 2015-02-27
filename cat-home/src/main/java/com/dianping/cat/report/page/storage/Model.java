package com.dianping.cat.report.page.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.storage.model.entity.Machine;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.helper.SortHelper;
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

	public List<String> getAllOperations() {
		if (m_report != null) {
			ArrayList<String> ops = new ArrayList<String>(m_report.getOps());

			Collections.sort(ops);
			return ops;
		} else {
			return new ArrayList<String>();
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

	// @Override
	// public Map<String, Department> getDomainGroups() {
	// // return m_projectService.findDepartments(getDomains());
	// return null;
	// }

	@Override
	public Collection<String> getDomains() {
		if (m_report != null) {
			return SortHelper.sortDomain(m_report.getIds());
		} else {
			return new HashSet<String>();
		}
	}

	public List<String> getIps() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return SortHelper.sortIpAddress(m_report.getIps());
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

	public List<String> getOperations() {
		ArrayList<String> operations = new ArrayList<String>(m_operations);

		Collections.sort(operations);
		return operations;
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
