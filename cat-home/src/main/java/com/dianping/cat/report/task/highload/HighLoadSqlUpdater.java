package com.dianping.cat.report.task.highload;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.TimeHelper;
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

	public static final String ID = Constants.HIGH_LOAD_SQL;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		try {
			List<HighLoadSQL> sqls = generateHighLoadSqls();
			return true;
		} catch (Exception ex) {
			Cat.logError(ex);
			return false;
		}
	}

	private List<HighLoadSQL> generateHighLoadSqls() {
		Set<String> domains = queryDomains();
		Heap heap = new Heap();

		for (String domain : domains) {
			try {
				TransactionReport report = m_reportService.queryTransactionReport(domain, TimeHelper.getYesterday(),
				      TimeHelper.getCurrentDay());
				DisplayNames displayNames = new DisplayNames();

				displayNames.display("", getType(), "All", report, "");
				for (TransactionNameModel nameModel : displayNames.getResults()) {
					HighLoadSQL sql = new HighLoadSQL(domain, nameModel.getDetail());
					heap.add(sql);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		return heap.getSqls();
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getType() {
		return "SQL";
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

		private HighLoadSQL[] m_sqls = new HighLoadSQL[m_size];

		public void add(HighLoadSQL sql) {
			int nextIndex = findNextValidIndex();

			if (nextIndex == m_size) {
				if (isBigger(sql, m_sqls[0])) {
					m_sqls[0] = sql;
					heapAdjust(0, m_size - 1);
				}
			} else {
				m_sqls[nextIndex] = sql;
				if (nextIndex == m_size - 1) {
					sort();
				}
			}
		}

		private int findNextValidIndex() {
			int i;

			for (i = 0; i < m_size && m_sqls[i] != null; i++) {
			}
			return i;
		}

		public List<HighLoadSQL> getSqls() {
			return Arrays.asList(m_sqls);
		}

		private void heapAdjust(int startIndex, int endIndex) {
			int currentIndex = startIndex;
			int tmpIndex = 2 * currentIndex + 1;

			while (tmpIndex <= endIndex) {
				if (m_sqls[tmpIndex + 1] != null && isBigger(m_sqls[tmpIndex + 1], m_sqls[tmpIndex])) {
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

		private boolean isBigger(HighLoadSQL sql1, HighLoadSQL sql2) {
			TransactionName name1 = sql1.getName();
			TransactionName name2 = sql2.getName();

			return name1.getTotalCount() * name1.getAvg() - name2.getTotalCount() * name2.getAvg() > 0 ? true : false;
		}

		private void sort() {
			int currentIndex = (m_size - 1) / 2;

			for (; currentIndex >= 0; currentIndex--) {
				heapAdjust(currentIndex, m_size);
			}

			for (int i = m_size - 1; i > 0; i--) {
				swap(0, i);
				heapAdjust(0, i - 1);
			}

		}

		private void swap(int index1, int index2) {
			HighLoadSQL tmpNode = m_sqls[index1];
			m_sqls[index1] = m_sqls[index2];
			m_sqls[index2] = tmpNode;
		}
	}

	public class HighLoadSQL {
		private String m_domain;

		private TransactionName m_name;

		public HighLoadSQL(String domain, TransactionName name) {
			m_domain = domain;
			m_name = name;
		}

		public String getDomain() {
			return m_domain;
		}

		public TransactionName getName() {
			return m_name;
		}

		public void setDomain(String domain) {
			m_domain = domain;
		}

		public void setName(TransactionName name) {
			m_name = name;
		}

	}

}