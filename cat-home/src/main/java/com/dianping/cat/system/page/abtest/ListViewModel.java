package com.dianping.cat.system.page.abtest;

import java.util.Date;
import java.util.List;

import com.dianping.cat.home.dal.abtest.Abtest;
import com.dianping.cat.home.dal.abtest.AbtestRun;
import com.dianping.cat.system.page.abtest.util.AbtestStatus;

public class ListViewModel {

	private int m_totalPages;

	private int m_createdCount;

	private int m_readyCount;

	private int m_runningCount;

	private int m_terminatedCount;

	private int m_suspendedCount;

	private List<AbtestItem> m_items;

	public int getCreatedCount() {
		return m_createdCount;
	}

	public List<AbtestItem> getItems() {
		return m_items;
	}

	public int getReadyCount() {
		return m_readyCount;
	}

	public int getRunningCount() {
		return m_runningCount;
	}

	public int getSuspendedCount() {
		return m_suspendedCount;
	}

	public int getTerminatedCount() {
		return m_terminatedCount;
	}

	public int getTotalPages() {
		return m_totalPages;
	}

	public void setCreatedCount(int createdCount) {
		m_createdCount = createdCount;
	}

	public void setItems(List<AbtestItem> item) {
		m_items = item;
	}

	public void setReadyCount(int readyCount) {
		m_readyCount = readyCount;
	}

	public void setRunningCount(int runningCount) {
		m_runningCount = runningCount;
	}

	public void setSuspendedCount(int suspendedCount) {
		m_suspendedCount = suspendedCount;
	}

	public void setTerminatedCount(int terminatedCount) {
		m_terminatedCount = terminatedCount;
	}

	public void setTotalPages(int totalPages) {
		m_totalPages = totalPages;
	}

	public static class AbtestItem {

		private Abtest m_abtest;

		private AbtestRun m_run;

		public AbtestItem(Abtest abtest, AbtestRun run) {
			m_abtest = abtest;
			m_run = run;
		}

		public Abtest getAbtest() {
			return m_abtest;
		}

		public int getCaseId() {
			return m_run.getCaseId();
		}

		public String getConditions() {
			return m_run.getConditions();
		}

		public String getConversionGoals() {
			return m_run.getConversionGoals();
		}

		public String getDescription() {
			return m_abtest.getDescription();
		}

		public String getDomains() {
			return m_run.getDomains();
		}

		public Date getEndDate() {
			return m_run.getEndDate();
		}

		public int getGroupStrategy() {
			return m_abtest.getGroupStrategy();
		}

		public int getId() {
			return m_run.getId();
		}

		public String getName() {
			return m_abtest.getName();
		}

		public String getOwner() {
			return m_abtest.getOwner();
		}

		public AbtestRun getRun() {
			return m_run;
		}

		public Date getStartDate() {
			return m_run.getStartDate();
		}

		public AbtestStatus getStatus() {
			return AbtestStatus.calculateStatus(m_run, new Date());
		}

		public String getStrategyConfiguration() {
			return m_run.getStrategyConfiguration();
		}

		public void setAbtest(Abtest abtest) {
			m_abtest = abtest;
		}

		public void setRun(AbtestRun run) {
			m_run = run;
		}
	}
}
