package com.dianping.cat.report.task.highload;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.HighloadSql;
import com.dianping.cat.home.dal.report.HighloadSqlDao;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.transaction.DisplayNames;
import com.dianping.cat.report.page.transaction.DisplayNames.TransactionNameModel;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class HighLoadSqlUpdater extends TransactionHighLoadUpdater {

	@Inject(type = ModelService.class, value = TransactionAnalyzer.ID)
	private ModelService<TransactionReport> m_transactionService;

	@Inject
	private ReportServiceManager m_reportService;

	@Inject
	private HighloadSqlDao m_dao;

	public static final String ID = Constants.HIGH_LOAD_SQL;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		try {
			List<HighLoadSQLReport> sqls = generateHighLoadSqls();

			for (HighLoadSQLReport sql : sqls) {
				insertSql(sql);
			}
			return true;
		} catch (Exception ex) {
			Cat.logError(ex);
			return false;
		}
	}

	private HighloadSql convertSql(HighLoadSQLReport sql) {
		HighloadSql dbSql = m_dao.createLocal();

		dbSql.setDate(sql.getDate());
		dbSql.setDomain(sql.getDomain());
		dbSql.setTransactionNameContent(sql.getName().toString());
		dbSql.setWeight(sql.getWeight());
		return dbSql;
	}

	private List<HighLoadSQLReport> generateHighLoadSqls() {
		Set<String> domains = queryDomains();
		Heap heap = new Heap();
		Date yesterday = TimeHelper.getYesterday();
		Date currentDay = TimeHelper.getCurrentDay();

		for (String domain : domains) {
			try {
				generateHighLoadSqlsByDomain(heap, yesterday, currentDay, domain);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		return heap.getSqls();
	}

	private void generateHighLoadSqlsByDomain(Heap heap, Date yesterday, Date currentDay, String domain) {
		TransactionReport report = m_reportService.queryTransactionReport(domain, yesterday, currentDay);
		DisplayNames displayNames = new DisplayNames();

		displayNames.display("", getType(), "All", report, "");
		for (TransactionNameModel nameModel : displayNames.getResults()) {
			try {
				TransactionName name = nameModel.getDetail();
				String id = name.getId();
				if (!"TOTAL".equals(id)) {
					double weight = name.getTotalCount() * name.getAvg();
					HighLoadSQLReport sql = new HighLoadSQLReport(domain, name, yesterday, weight);
					heap.add(sql);
				}
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		}
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getType() {
		return "SQL";
	}

	private void insertSql(HighLoadSQLReport sql) {
		try {
			m_dao.insert(convertSql(sql));
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	private Set<String> queryDomains() {
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

		private HighLoadSQLReport[] m_sqls = new HighLoadSQLReport[m_size];

		public void add(HighLoadSQLReport sql) {
			int nextIndex = findNextValidIndex();

			if (nextIndex == m_size) {
				if (isBigger(m_sqls[0], sql)) {
					m_sqls[0] = sql;
					addAdjust();
				}
			} else {
				m_sqls[nextIndex] = sql;
				if (nextIndex == m_size - 1) {
					sort();
				}
			}
		}

		private void addAdjust() {
			int currentIndex = 0;
			int tmpIndex = 2 * currentIndex + 1;

			while (tmpIndex <= m_size - 1) {
				if (tmpIndex + 1 <= m_size - 1 && m_sqls[tmpIndex + 1] != null
				      && isBigger(m_sqls[tmpIndex], m_sqls[tmpIndex + 1])) {
					tmpIndex = tmpIndex + 1;
				}
				if (isBigger(m_sqls[currentIndex], m_sqls[tmpIndex])) {
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

			for (i = 0; i < m_size && m_sqls[i] != null; i++) {
			}
			return i;
		}

		public List<HighLoadSQLReport> getSqls() {
			List<HighLoadSQLReport> sqls = new ArrayList<HighLoadSQLReport>();

			for (int i = 0; i < m_size; i++) {
				HighLoadSQLReport currentNode = m_sqls[i];

				if (currentNode != null) {
					sqls.add(currentNode);
				} else {
					break;
				}
			}
			return sqls;
		}

		private void heapAdjust(int startIndex, int endIndex) {
			int currentIndex = startIndex;
			int tmpIndex = 2 * currentIndex + 1;

			while (tmpIndex <= endIndex) {
				if (tmpIndex + 1 <= endIndex && m_sqls[tmpIndex + 1] != null
				      && isBigger(m_sqls[tmpIndex + 1], m_sqls[tmpIndex])) {
					tmpIndex = tmpIndex + 1;
				}
				if (isBigger(m_sqls[tmpIndex], m_sqls[currentIndex])) {
					swap(currentIndex, tmpIndex);
					currentIndex = tmpIndex;
					tmpIndex = 2 * currentIndex + 1;
				} else {
					break;
				}
			}
		}

		private boolean isBigger(HighLoadSQLReport sql1, HighLoadSQLReport sql2) {
			TransactionName name1 = sql1.getName();
			TransactionName name2 = sql2.getName();

			return name1.getTotalCount() * name1.getAvg() - name2.getTotalCount() * name2.getAvg() > 0 ? true : false;
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
			HighLoadSQLReport tmpNode = m_sqls[index1];
			m_sqls[index1] = m_sqls[index2];
			m_sqls[index2] = tmpNode;
		}
	}

	public class HighLoadSQLReport {
		private String m_domain;

		private TransactionName m_name;

		private Date m_date;

		private double m_weight;

		public HighLoadSQLReport() {
		}

		public HighLoadSQLReport(String domain, TransactionName name, Date date, double weight) {
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

		public void setWeight(double weight) {
			m_weight = weight;
		}
	}

}