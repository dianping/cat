package com.dianping.cat.report.analyzer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;
import com.dianping.cat.report.service.HourlyReportService;

public class OpDataCollectTest extends ComponentTestCase {

	private HourlyReportService m_hourlyReportService;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	@Test
	public void test() throws Exception {
		m_hourlyReportService = (HourlyReportService) lookup(HourlyReportService.class);
		String dateStr1 = "2013-03-22 16:00";
		String dateStr2 = "2013-04-26 16:00";
		String dateStr3 = "2013-05-24 16:00";

		String[] domains = getDomains();
		System.out.println("Domain\tDate\tType\tTotal\tAvg\tLine95\tStd\tQPS");
		for (String domain : domains) {
			buildTransactionData(domain, dateStr1);
			buildTransactionData(domain, dateStr2);
			buildTransactionData(domain, dateStr3);
		}

		System.out.println("Domain\tDate\tLoad\tHttp");
		for (String domain : domains) {
			buildHeartbeatData(domain, dateStr1);
			buildHeartbeatData(domain, dateStr2);
			buildHeartbeatData(domain, dateStr3);
		}
	}

	private void buildHeartbeatData(String domain, String str) throws Exception {
		Date start = m_sdf.parse(str);
		Date end = new Date(start.getTime() + TimeUtil.ONE_HOUR);
		HeartbeatReport heartbeatReport = m_hourlyReportService.queryHeartbeatReport(domain, start, end);
		showHeartbeatInfo(domain, str, heartbeatReport);
	}

	private void buildTransactionData(String domain, String str) throws Exception {
		Date start = m_sdf.parse(str);
		Date end = new Date(start.getTime() + TimeUtil.ONE_HOUR);
		TransactionReport transactionReport = m_hourlyReportService.queryTransactionReport(domain, start, end);

		showTransactionInfo(domain, str, transactionReport);

	}

	private void showTransactionInfo(String domain, String str, TransactionReport report) {
		TransactionReport temp = new TransactionReport();
		TransactionReportMerger merger = new TransactionReportMerger(temp);
		Machine machine = merger.mergesForAllMachine(report);
		report.addMachine(machine);

		new TransactionVisitor(domain, str).visitTransactionReport(report);
	}

	private void showHeartbeatInfo(String domain, String str, HeartbeatReport report) {
		HeartbeatVisitor visitor = new HeartbeatVisitor();
		visitor.visitHeartbeatReport(report);
		System.out.println(report.getDomain() + "\t" + str + "\t" + visitor.computeLoad() + "\t" + visitor.computeHttp());
	}

	private String[] getDomains() {
		String domains = "MobileMembercardMainApiWeb,MobileMembercardMainServer,MobileMembercardRecomServer,PayEngine,"
		      + "tuangou-paygate,DealService,TuanGouApiMobile,TuanGouRank,TuanGouRemote,TuanGouWap,TuanGouWeb,MobileApi,"
		      + "MLocationService,MConfigAPI,MobileRecomStore,UserBaseService,GroupService,GroupWeb,PiccenterDisplay,"
		      + "PiccenterStorage,PiccenterUpload,ShopServer,ShopWeb,ShoppicService,ShopSearchWeb,ReviewServer,UserService";

		return domains.split(",");
	}

	class HeartbeatVisitor extends com.dianping.cat.consumer.heartbeat.model.transform.BaseVisitor {

		private List<Double> loads = new ArrayList<Double>();

		private List<Double> https = new ArrayList<Double>();

		public double computeLoad() {
			double sum = 0.0;

			for (Double temp : loads) {
				sum += temp;
			}
			return sum / loads.size();
		}

		public double computeHttp() {
			double sum = 0.0;

			for (Double temp : https) {
				sum += temp;
			}
			return sum / https.size();
		}

		@Override
		public void visitPeriod(Period period) {
			double load = period.getSystemLoadAverage();
			double http = period.getHttpThreadCount();

			loads.add(load);
			https.add(http);
		}
	}

	class TransactionVisitor extends BaseVisitor {
		public Set<String> ALL_TYPES = new HashSet<String>();

		private String m_domain;

		private String m_str;

		public TransactionVisitor(String domain, String str) {
			ALL_TYPES.add("URL");
			ALL_TYPES.add("Service");
			ALL_TYPES.add("PigeonService");
			m_domain = domain;
			m_str = str;
		}

		@Override
		public void visitMachine(Machine machine) {
			String ip = machine.getIp();

			if (CatString.ALL.equalsIgnoreCase(ip)) {
				super.visitMachine(machine);
			}
		}

		@Override
		public void visitType(TransactionType type) {
			StringBuilder sb = new StringBuilder(128);
			if (ALL_TYPES.contains(type.getId())) {
				sb.append(m_domain).append("\t");
				sb.append(m_str).append("\t");
				sb.append(type.getId()).append("\t");
				sb.append(type.getTotalCount()).append("\t");
				sb.append(type.getAvg()).append("\t");
				sb.append(type.getLine95Value()).append("\t");
				sb.append(type.getStd()).append("\t");
				sb.append(1000 * type.getTotalCount() / TimeUtil.ONE_HOUR).append("\t");
				System.out.println(sb.toString());
			}
		}
	}

}
