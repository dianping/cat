package com.dianping.cat.consumer.state;

import java.util.Date;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.DomainManager;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.DefaultReportManager.StoragePolicy;
import com.dianping.cat.service.ReportConstants;
import com.dianping.cat.service.ReportManager;
import com.dianping.cat.statistic.ServerStatistic.Statistic;
import com.dianping.cat.statistic.ServerStatisticManager;

public class StateAnalyzer extends AbstractMessageAnalyzer<StateReport> implements LogEnabled {
	public static final String ID = "state";

	@Inject(ID)
	private ReportManager<StateReport> m_reportManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;

	@Inject
	private DomainManager m_domainManager;

	private void buildStateInfo(Machine machine) {
		long minute = 1000 * 60;
		long start = m_startTime;
		long end = m_startTime + minute * 60;

		if (end > System.currentTimeMillis()) {
			end = System.currentTimeMillis();
		}
		int size = 0;
		double maxTps = 0;
		for (; start < end; start += minute) {
			size++;
			Statistic state = m_serverStateManager.findState(start);

			com.dianping.cat.consumer.state.model.entity.Message temp = machine.findOrCreateMessage(start);
			long messageTotal = state.getMessageTotal();
			temp.setTotal(messageTotal);
			machine.setTotal(machine.getTotal() + messageTotal);

			if (messageTotal > maxTps) {
				maxTps = messageTotal;
			}

			long blockTotal = state.getBlockTotal();
			temp.setBlockTotal(blockTotal);
			machine.setBlockTotal(machine.getBlockTotal() + blockTotal);

			long blockLoss = state.getBlockLoss();
			temp.setBlockLoss(blockLoss);
			machine.setBlockLoss(machine.getBlockLoss() + blockLoss);

			long blockTime = state.getBlockTime();
			temp.setBlockTime(blockTime);
			machine.setBlockTime(machine.getBlockTime() + blockTime);

			long pigeonTimeError = state.getPigeonTimeError();
			temp.setPigeonTimeError(pigeonTimeError);
			machine.setPigeonTimeError(machine.getPigeonTimeError() + pigeonTimeError);

			long networkTimeError = state.getNetworkTimeError();
			temp.setNetworkTimeError(networkTimeError);
			machine.setNetworkTimeError(machine.getNetworkTimeError() + networkTimeError);

			long messageTotalLoss = state.getMessageTotalLoss();
			temp.setTotalLoss(messageTotalLoss);
			machine.setTotalLoss(machine.getTotalLoss() + messageTotalLoss);

			long messageDump = state.getMessageDump();
			temp.setDump(messageDump);
			machine.setDump(machine.getDump() + messageDump);

			long messageDumpLoss = state.getMessageDumpLoss();
			temp.setDumpLoss(messageDumpLoss);
			machine.setDumpLoss(machine.getDumpLoss() + messageDumpLoss);

			double messageSize = state.getMessageSize();
			temp.setSize(messageSize);
			machine.setSize(machine.getSize() + messageSize);

			int processDelayCount = state.getProcessDelayCount();
			temp.setDelayCount(processDelayCount);
			machine.setDelayCount(machine.getDelayCount() + processDelayCount);

			double processDelaySum = state.getProcessDelaySum();
			temp.setDelaySum(processDelaySum);
			machine.setDelaySum(machine.getDelaySum() + processDelaySum);

			double sum = machine.getDelaySum();
			long count = machine.getDelayCount();
			double avg = 0;
			if (count > 0) {
				avg = sum / count;
				machine.setDelayAvg(avg);
			}
			temp.setTime(new Date(start));
		}

		double avgTps = 0;
		if (size > 0) {
			avgTps = machine.getTotal() / (double) size;
		}
		machine.setAvgTps(avgTps);
		machine.setMaxTps(maxTps);
	}

	@Override
	public void doCheckpoint(boolean atEnd) {
		StateReport stateReport = getReport(ReportConstants.CAT);
		Map<String, StateReport> reports = m_reportManager.getHourlyReports(getStartTime());

		reports.put(ReportConstants.CAT, stateReport);
		long startTime = getStartTime();

		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(startTime, StoragePolicy.FILE_AND_DB);
		} else {
			m_reportManager.storeHourlyReports(startTime, StoragePolicy.FILE);
		}
		if (atEnd) {
			long minute = 1000 * 60;
			long start = m_startTime - minute * 60 * 2;
			long end = m_startTime - minute * 60;

			for (; start < end; start += minute) {
				m_serverStateManager.removeState(start);
			}
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public StateReport getReport(String domain) {
		StateReport report = new StateReport(domain);
		report = new StateReport(ReportConstants.CAT);
		report.setStartTime(new Date(m_startTime));
		report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));
		report.getMachines().clear();

		String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
		Machine machine = report.findOrCreateMachine(ip);

		buildStateInfo(machine);
		StateReport startReport = m_reportManager.getHourlyReport(getStartTime(), ReportConstants.CAT, true);

		machine.getProcessDomains().putAll(startReport.findOrCreateMachine(ip).getProcessDomains());
		return report;
	}

	@Override
	protected void process(MessageTree tree) {
		StateReport report = m_reportManager.getHourlyReport(getStartTime(), ReportConstants.CAT, true);
		String domain = tree.getDomain();
		String ip = tree.getIpAddress();
		Machine machine = report.findOrCreateMachine(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());

		machine.findOrCreateProcessDomain(domain).addIp(ip);
		if (validate(domain)) {
			if (!m_domainManager.containsDomainInCat(domain)) {
				m_logger.info("inserting domain info " + domain);
				m_domainManager.insertDomain(domain);
				m_logger.info("inserted domain info " + domain);
			}
			if (!m_domainManager.contaninsIpInCat(ip)) {
				m_logger.info("inserting domain info " + domain + " " + ip);
				m_domainManager.insert(domain, ip);
				m_logger.info("inserted domain info " + domain + " " + ip);
			}
		}
	}

	private boolean validate(String domain) {
		return !domain.equals("PhoenixAgent") && !domain.equals(ReportConstants.FRONT_END);
	}

}
