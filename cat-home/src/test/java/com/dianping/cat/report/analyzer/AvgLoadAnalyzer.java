package com.dianping.cat.report.analyzer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.heartbeat.model.entity.Disk;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.service.ReportService;

public class AvgLoadAnalyzer extends ComponentTestCase {

	private String m_start = "2013-11-06 17:00";

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private DecimalFormat m_df = new DecimalFormat("#.00");

	@Inject
	private ReportService m_reportService;

	private Map<String, MachineInfo> m_infos = new LinkedHashMap<String, MachineInfo>();

	private MachineInfo findOrCreate(String ip) {
		MachineInfo info = m_infos.get(ip);

		if (info == null) {
			info = new MachineInfo(ip);
			m_infos.put(ip, info);
		}
		return info;
	}

	@Test
	public void test() throws Exception {
		m_reportService = (ReportService) lookup(ReportService.class);

		Date start = m_sdf.parse(m_start);
		Set<String> domains = queryDomains(start);

		for (String domain : domains) {
			HeartbeatReport report = m_reportService.queryHeartbeatReport(domain, start, new Date(start.getTime()
			      + TimeUtil.ONE_HOUR));

			if(!"PhoenixAgent".equals(domain)){
				new HeartbeatVisitor().visitHeartbeatReport(report);
			}
		}

		printResult();
	}

	private void printResult() {
		StringBuffer sb = new StringBuffer();

		sb.append("Domain").append("\t").append("Ip").append("\t").append("AvgLoad").append("\t").append("MaxLoad")
		      .append("\n");

		for (MachineInfo info : m_infos.values()) {
			sb.append(info.getDomain()).append("\t").append(info.getIp()).append("\t").append(m_df.format(info.getLoad()))
			      .append("\t").append(m_df.format(info.getMaxLoad())).append("\n");

		}

		System.out.println(sb.toString());
	}

	public class HeartbeatVisitor extends com.dianping.cat.consumer.heartbeat.model.transform.BaseVisitor {

		private String m_currentMachine;

		private String m_currentDomain;

		@Override
		public void visitDisk(Disk disk) {
			super.visitDisk(disk);
		}

		@Override
		public void visitHeartbeatReport(HeartbeatReport heartbeatReport) {
			m_currentDomain = heartbeatReport.getDomain();
			super.visitHeartbeatReport(heartbeatReport);
		}

		@Override
		public void visitMachine(Machine machine) {
			m_currentMachine = machine.getIp();
			super.visitMachine(machine);
		}

		@Override
		public void visitPeriod(Period period) {
			double load = period.getSystemLoadAverage();
			MachineInfo info = findOrCreate(m_currentMachine);

			info.setDomain(m_currentDomain);
			info.addload(load);
		}
	}

	private Set<String> queryDomains(Date date) {
		return m_reportService.queryAllDomainNames(date, new Date(date.getTime() + TimeUtil.ONE_HOUR),
		      TransactionAnalyzer.ID);
	}
	
	public static class MachineInfo {

		private String m_ip;

		private String m_domain;

		private double m_loadSum;

		private double m_maxLoad;

		private int m_count;

		public MachineInfo(String ip) {
			m_ip = ip;
		}

		public void addload(double load) {
			if (load > m_maxLoad) {
				m_maxLoad = load;
			}
			m_count++;
			m_loadSum = m_loadSum + load;
		}

		public String getIp() {
			return m_ip;
		}

		public double getLoad() {
			if (m_count > 0) {
				return m_loadSum / m_count;
			} else {
				return 0;
			}
		}

		public double getMaxLoad() {
			return m_maxLoad;
		}

		public String getDomain() {
			return m_domain;
		}

		public void setDomain(String domain) {
			m_domain = domain;
		}
	}

}
