package com.dianping.cat.report.analyzer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportEntity;

public class DBAAnalyzer extends ComponentTestCase {

	private TransactionReportMerger m_merger = new TransactionReportMerger(new TransactionReport());

	@Test
	public void test() throws Exception {
		MonthlyReportDao dao = lookup(MonthlyReportDao.class);
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2013-06-01 00:00");
		MonthlyReport monthreport = dao.findReportByDomainNamePeriod(date, "All", TransactionAnalyzer.ID,
		      MonthlyReportEntity.READSET_FULL);
		String content = monthreport.getContent();
		TransactionReport report = DefaultSaxParser.parse(content);

		Collection<Machine> machines = report.getMachines().values();

		Machine info = new Machine("info");
		Machine biz = new Machine("biz");

		for (Machine machine : machines) {
			String id = machine.getIp();
			Machine temp = info;

			if (!isInfo(id)) {
				temp = biz;
			}

			
			for (TransactionType type : machine.getTypes().values()) {
				if(!machine.getIp().equals(Constants.ALL)){
					TransactionType old = temp.findOrCreateType(type.getId());
					m_merger.mergeType(old, type);
				}
			}
		}

		System.out.println("Type\tTotalCount\tFailureCount\tAvg\t95Line");
		System.out.println("信息线");
		for (TransactionType type : info.getTypes().values()) {
			if (type.getId().equals("SQL") || type.getId().startsWith("Cache.")) {
				System.out.println(type.getId() + '\t' + type.getTotalCount() + '\t' + type.getFailCount() + '\t'
				      + type.getAvg() + '\t' + type.getLine95Value());
			}
		}
		System.out.println("商务线");
		for (TransactionType type : biz.getTypes().values()) {
			if (type.getId().equals("SQL") || type.getId().startsWith("Cache.")) {
				System.out.println(type.getId() + '\t' + type.getTotalCount() + '\t' + type.getFailCount() + '\t'
				      + type.getAvg() + '\t' + type.getLine95Value());
			}
		}
	}

	public boolean isInfo(String domain) throws IOException {
		String content = Files.forIO().readFrom(this.getClass().getResourceAsStream("infoDomains"), "utf8");
		String[] domains = content.split("\n");
		Map<String, String> domainMap = new HashMap<String, String>();

		for (int i = 0; i < domains.length; i++) {
			domainMap.put(domains[i].trim(), domains[i].trim());
		}

		return domainMap.containsKey(domain);
	}

}
