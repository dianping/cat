package com.dianping.cat.report.task.highload;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.Highload;
import com.dianping.cat.home.dal.report.HighloadDao;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.transaction.DisplayNames;
import com.dianping.cat.report.page.transaction.DisplayNames.TransactionNameModel;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public abstract class TransactionHighLoadUpdater extends HighLoadUpdater {

	@Inject(type = ModelService.class, value = TransactionAnalyzer.ID)
	private ModelService<TransactionReport> m_transactionService;

	@Inject
	private ReportServiceManager m_reportService;

	@Inject
	private HighloadDao m_dao;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		try {
			List<HighLoadReport> reports = generateHighLoadReports();

			for (HighLoadReport report : reports) {
				insert(report);
			}
			return true;
		} catch (Exception ex) {
			Cat.logError(ex);
			return false;
		}
	}

	public abstract double calWeight(TransactionName name);

	private Highload convertReport(HighLoadReport report) {
		Highload dbReport = m_dao.createLocal();

		dbReport.setType(getType());
		dbReport.setDate(report.getDate());
		dbReport.setDomain(report.getDomain());
		dbReport.setTransactionNameContent(report.getName().toString());
		dbReport.setWeight(report.getWeight());
		return dbReport;
	}

	private List<HighLoadReport> generateHighLoadReports() {
		Set<String> domains = queryDomains();
		Heap heap = new Heap();
		Date yesterday = TimeHelper.getYesterday();
		Date currentDay = TimeHelper.getCurrentDay();

		for (String domain : domains) {
			try {
				generateHighLoadsByDomain(heap, yesterday, currentDay, domain);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return heap.getReports();
	}

	private void generateHighLoadsByDomain(Heap heap, Date yesterday, Date currentDay, String domain) {
		TransactionReport report = m_reportService.queryTransactionReport(domain, yesterday, currentDay);
		DisplayNames displayNames = new DisplayNames();

		displayNames.display("", getType(), "All", report, "");
		for (TransactionNameModel nameModel : displayNames.getResults()) {
			try {
				TransactionName name = nameModel.getDetail();
				String id = name.getId();

				if (!"TOTAL".equals(id)) {
					double weight = calWeight(name);
					HighLoadReport highloadReport = new HighLoadReport(domain, getType(), name, yesterday, weight);
					heap.add(highloadReport);
				}
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		}
	}

	public abstract String getType();

	private void insert(HighLoadReport report) {
		try {
			m_dao.insert(convertReport(report));
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	protected Set<String> queryDomains() {
		Set<String> domains = new HashSet<String>();
		ModelRequest request = new ModelRequest("cat", System.currentTimeMillis());

		if (m_transactionService.isEligable(request)) {
			ModelResponse<TransactionReport> response = m_transactionService.invoke(request);
			domains.addAll(response.getModel().getDomainNames());
		}
		return domains;
	}

	public class Heap {

		private final int m_size = 100;

		private HighLoadReport[] m_reports = new HighLoadReport[m_size];

		public void add(HighLoadReport report) {
			int nextIndex = findNextValidIndex();

			if (nextIndex == m_size) {
				if (isBigger(m_reports[0], report)) {
					m_reports[0] = report;
					addAdjust();
				}
			} else {
				m_reports[nextIndex] = report;
				if (nextIndex == m_size - 1) {
					sort();
				}
			}
		}

		private void addAdjust() {
			int currentIndex = 0;
			int tmpIndex = 2 * currentIndex + 1;

			while (tmpIndex <= m_size - 1) {
				if (tmpIndex + 1 <= m_size - 1 && m_reports[tmpIndex + 1] != null
				      && isBigger(m_reports[tmpIndex], m_reports[tmpIndex + 1])) {
					tmpIndex = tmpIndex + 1;
				}
				if (isBigger(m_reports[currentIndex], m_reports[tmpIndex])) {
					swap(currentIndex, tmpIndex);
					currentIndex = tmpIndex;
					tmpIndex = 2 * currentIndex + 1;
				} else {
					break;
				}
			}
		}

		private int findNextValidIndex() {
			int i;

			for (i = 0; i < m_size && m_reports[i] != null; i++) {
			}
			return i;
		}

		public List<HighLoadReport> getReports() {
			List<HighLoadReport> reports = new ArrayList<HighLoadReport>();

			for (int i = 0; i < m_size; i++) {
				HighLoadReport currentNode = m_reports[i];

				if (currentNode != null) {
					reports.add(currentNode);
				} else {
					break;
				}
			}
			return reports;
		}

		private void heapAdjust(int startIndex, int endIndex) {
			int currentIndex = startIndex;
			int tmpIndex = 2 * currentIndex + 1;

			while (tmpIndex <= endIndex) {
				if (tmpIndex + 1 <= endIndex && m_reports[tmpIndex + 1] != null
				      && isBigger(m_reports[tmpIndex + 1], m_reports[tmpIndex])) {
					tmpIndex = tmpIndex + 1;
				}
				if (isBigger(m_reports[tmpIndex], m_reports[currentIndex])) {
					swap(currentIndex, tmpIndex);
					currentIndex = tmpIndex;
					tmpIndex = 2 * currentIndex + 1;
				} else {
					break;
				}
			}
		}

		private boolean isBigger(HighLoadReport report1, HighLoadReport report2) {
			TransactionName name1 = report1.getName();
			TransactionName name2 = report2.getName();

			return calWeight(name1) - calWeight(name2) > 0 ? true : false;
		}

		private void sort() {
			int currentIndex = (m_size - 2) / 2;

			for (; currentIndex >= 0; currentIndex--) {
				heapAdjust(currentIndex, m_size - 1);
			}

			for (int i = m_size - 1; i > 0; i--) {
				swap(0, i);
				heapAdjust(0, i - 1);
			}

		}

		private void swap(int index1, int index2) {
			HighLoadReport tmpNode = m_reports[index1];
			m_reports[index1] = m_reports[index2];
			m_reports[index2] = tmpNode;
		}
	}

	public class HighLoadReport {
		private String m_type;

		private String m_domain;

		private TransactionName m_name;

		private Date m_date;

		private double m_weight;

		public HighLoadReport() {
		}

		public HighLoadReport(String domain, String type, TransactionName name, Date date, double weight) {
			m_type = type;
			m_domain = domain;
			m_name = name;
			m_date = date;
			m_weight = weight;
		}

		public Date getDate() {
			return m_date;
		}

		public String getDomain() {
			return m_domain;
		}

		public TransactionName getName() {
			return m_name;
		}

		public String getType() {
			return m_type;
		}

		public double getWeight() {
			return m_weight;
		}

		public void setDate(Date date) {
			m_date = date;
		}

		public void setDomain(String domain) {
			m_domain = domain;
		}

		public void setName(TransactionName name) {
			m_name = name;
		}

		public void setType(String type) {
			m_type = type;
		}

		public void setWeight(double weight) {
			m_weight = weight;
		}
	}

}
