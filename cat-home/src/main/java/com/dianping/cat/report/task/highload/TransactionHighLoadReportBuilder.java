package com.dianping.cat.report.task.highload;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.highload.entity.HighloadReport;
import com.dianping.cat.home.highload.entity.Name;
import com.dianping.cat.home.highload.entity.Type;
import com.dianping.cat.home.highload.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.transaction.DisplayNames;
import com.dianping.cat.report.page.transaction.DisplayNames.TransactionNameModel;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class TransactionHighLoadReportBuilder implements ReportTaskBuilder {

	@Inject
	protected ReportServiceManager m_reportService;

	@Inject(type = ModelService.class, value = TransactionAnalyzer.ID)
	private ModelService<TransactionReport> m_transactionService;

	public static final String ID = Constants.HIGH_LOAD_REPORT;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		HighloadReport report = generateHighloadReport(period);
		DailyReport dailyReport = new DailyReport();

		dailyReport.setContent("");
		dailyReport.setIp("");
		dailyReport.setDomain("");
		dailyReport.setCreationDate(new Date());
		dailyReport.setName(name);
		dailyReport.setPeriod(period);
		dailyReport.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(report);

		return m_reportService.insertDailyReport(dailyReport, binaryContent);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException(getID() + " don't support hourly update");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException(getID() + " don't support monthly update");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException(getID() + " don't support weekly update");
	}

	private double calWeight(Name name) {
		return name.getTotalCount() * name.getAvg();
	}

	private Name convertToHighloadName(String domain, TransactionName transactionName) {
		Name name = new Name();

		name.setDomain(domain);
		name.setId(transactionName.getId());
		name.setTotalCount(transactionName.getTotalCount());
		name.setFailCount(transactionName.getFailCount());
		name.setFailPercent(transactionName.getFailPercent());
		name.setMin(transactionName.getMin());
		name.setMax(transactionName.getMax());
		name.setAvg(transactionName.getAvg());
		name.setStd(transactionName.getStd());
		name.setTps(transactionName.getTps());
		name.setLine95Value(transactionName.getLine95Value());
		name.setLine99Value(transactionName.getLine99Value());
		name.setWeight(calWeight(name));

		return name;
	}

	private HighloadReport generateHighloadReport(Date startTime) {
		Date endTime = TimeHelper.addDays(startTime, 1);
		Set<String> domains = queryDomains(startTime, endTime);
		HighloadReport report = new HighloadReport();

		report.setStartTime(startTime);
		report.setEndTime(endTime);
		report.addType(generateType(domains, "SQL", startTime, endTime));

		return report;
	}

	private void generateNamesByDomain(Heap heap, String domain, String type, Date startTime, Date endTime) {
		TransactionReport report = m_reportService.queryTransactionReport(domain, startTime, endTime);
		DisplayNames displayNames = new DisplayNames();

		displayNames.display("", type, "All", report, "");
		for (TransactionNameModel nameModel : displayNames.getResults()) {
			try {
				TransactionName name = nameModel.getDetail();
				String id = name.getId();

				if (!"TOTAL".equals(id)) {
					heap.add(convertToHighloadName(domain, name));
				}
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		}
	}

	private Type generateType(Set<String> domains, String typeId, Date startTime, Date endTime) {
		Heap heap = new Heap();
		Type type = new Type();

		type.setId(typeId);
		for (String domain : domains) {
			try {
				generateNamesByDomain(heap, domain, typeId, startTime, endTime);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		for (Name name : heap.getNames()) {
			type.addName(name);
		}

		return type;
	}

	private String getID() {
		return ID;
	}

	protected Set<String> queryDomains(Date startTime, Date endTime) {
		return m_reportService.queryAllDomainNames(startTime, endTime, "transaction");
	}

	public class Heap {

		private final int m_size = 100;

		private Name[] m_names = new Name[m_size];

		public void add(Name name) {
			int nextIndex = findNextValidIndex();

			if (nextIndex == m_size) {
				if (isBigger(name, m_names[0])) {
					m_names[0] = name;
					addAdjust();
				}
			} else {
				m_names[nextIndex] = name;
				if (nextIndex == m_size - 1) {
					sort();
				}
			}
		}

		private void addAdjust() {
			int currentIndex = 0;
			int tmpIndex = 2 * currentIndex + 1;

			while (tmpIndex <= m_size - 1) {
				if (tmpIndex + 1 <= m_size - 1 && m_names[tmpIndex + 1] != null
				      && isBigger(m_names[tmpIndex], m_names[tmpIndex + 1])) {
					tmpIndex = tmpIndex + 1;
				}
				if (isBigger(m_names[currentIndex], m_names[tmpIndex])) {
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

			for (i = 0; i < m_size && m_names[i] != null; i++) {
			}
			return i;
		}

		public List<Name> getNames() {
			List<Name> reports = new ArrayList<Name>();

			if (findNextValidIndex() == m_size) {
				sort();
			}
			for (int i = 0; i < m_size; i++) {
				Name currentNode = m_names[i];

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
				if (tmpIndex + 1 <= endIndex && m_names[tmpIndex + 1] != null
				      && isBigger(m_names[tmpIndex + 1], m_names[tmpIndex])) {
					tmpIndex = tmpIndex + 1;
				}
				if (isBigger(m_names[tmpIndex], m_names[currentIndex])) {
					swap(currentIndex, tmpIndex);
					currentIndex = tmpIndex;
					tmpIndex = 2 * currentIndex + 1;
				} else {
					break;
				}
			}
		}

		private boolean isBigger(Name name1, Name name2) {
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
			Name tmpNode = m_names[index1];
			m_names[index1] = m_names[index2];
			m_names[index2] = tmpNode;
		}
	}

}
