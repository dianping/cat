package com.dianping.cat.consumer.state;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.state.model.entity.Detail;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.statistic.ServerStatistic.Statistic;
import com.dianping.cat.statistic.ServerStatisticManager;

public class StateAnalyzer extends AbstractMessageAnalyzer<StateReport> implements LogEnabled {
	public static final String ID = "state";

	@Inject(ID)
	private ReportManager<StateReport> m_reportManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	@Inject
	private String m_ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

	private Machine buildStateInfo(Machine machine) {
		long minute = 1000 * 60;
		long start = m_startTime;
		long end = m_startTime + minute * 60;
		double maxTps = 0;
		long current = System.currentTimeMillis();
		int size = 0;

		if (end > current) {
			end = current;
		}
		for (; start < end; start += minute) {
			Statistic state = m_serverStateManager.findOrCreateState(start);
			Message temp = machine.findOrCreateMessage(start);
			Map<String, AtomicLong> totals = state.getMessageTotals();
			Map<String, AtomicLong> totalLosses = state.getMessageTotalLosses();
			Map<String, AtomicLong> sizes = state.getMessageSizes();

			for (Entry<String, AtomicLong> entry : totals.entrySet()) {
				String domain = entry.getKey();
				long value = entry.getValue().get();
				ProcessDomain processDomain = machine.findOrCreateProcessDomain(domain);
				Detail detail = processDomain.findOrCreateDetail(start);

				processDomain.setTotal(value + processDomain.getTotal());
				detail.setTotal(value + detail.getTotal());
			}
			for (Entry<String, AtomicLong> entry : totalLosses.entrySet()) {
				String domain = entry.getKey();
				long value = entry.getValue().get();
				ProcessDomain processDomain = machine.findOrCreateProcessDomain(domain);
				Detail detail = processDomain.findOrCreateDetail(start);

				processDomain.setTotalLoss(value + processDomain.getTotalLoss());
				detail.setTotalLoss(value + detail.getTotalLoss());
			}
			for (Entry<String, AtomicLong> entry : sizes.entrySet()) {
				String domain = entry.getKey();
				long value = entry.getValue().get();
				ProcessDomain processDomain = machine.findOrCreateProcessDomain(domain);
				Detail detail = processDomain.findOrCreateDetail(start);

				processDomain.setSize(value + processDomain.getSize());
				detail.setSize(value + detail.getSize());
			}

			long messageTotal = state.getMessageTotal();
			long messageTotalLoss = state.getMessageTotalLoss();
			long messageSize = state.getMessageSize();
			long blockTotal = state.getBlockTotal();
			long blockLoss = state.getBlockLoss();
			long blockTime = state.getBlockTime();
			long pigeonTimeError = state.getPigeonTimeError();
			long networkTimeError = state.getNetworkTimeError();
			long messageDump = state.getMessageDump();
			long messageDumpLoss = state.getMessageDumpLoss();
			int processDelayCount = state.getProcessDelayCount();
			double processDelaySum = state.getProcessDelaySum();

			temp.setTotal(messageTotal).setTotalLoss(messageTotalLoss).setSize(messageSize);
			temp.setBlockTotal(blockTotal).setBlockLoss(blockLoss).setBlockTime(blockTime);
			temp.setPigeonTimeError(pigeonTimeError).setNetworkTimeError(networkTimeError).setDump(messageDump);
			temp.setDumpLoss(messageDumpLoss).setDelayCount(processDelayCount).setDelaySum(processDelaySum);

			machine.setTotal(messageTotal + machine.getTotal()).setTotalLoss(messageTotalLoss + machine.getTotalLoss())
			      .setSize(messageSize + machine.getSize());
			machine.setBlockTotal(machine.getBlockTotal() + blockTotal).setBlockLoss(machine.getBlockLoss() + blockLoss)
			      .setBlockTime(machine.getBlockTime() + blockTime);
			machine.setPigeonTimeError(machine.getPigeonTimeError() + pigeonTimeError)
			      .setNetworkTimeError(machine.getNetworkTimeError() + networkTimeError)
			      .setDump(machine.getDump() + messageDump);
			machine.setDumpLoss(machine.getDumpLoss() + messageDumpLoss)
			      .setDelayCount(machine.getDelayCount() + processDelayCount)
			      .setDelaySum(machine.getDelaySum() + processDelaySum);

			double avg = 0;
			long count = machine.getDelayCount();

			if (count > 0) {
				avg = machine.getDelaySum() / count;
				machine.setDelayAvg(avg);
			}
			if (messageTotal > maxTps) {
				maxTps = messageTotal;
			}
			temp.setTime(new Date(start));
			size++;
		}

		double avgTps = 0;
		if (size > 0) {
			avgTps = machine.getTotal() / (double) size;
		}
		machine.setAvgTps(avgTps);
		machine.setMaxTps(maxTps);

		return machine;
	}

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		long startTime = getStartTime();
		StateReport stateReport = getReport(Constants.CAT);
		Map<String, StateReport> reports = m_reportManager.getHourlyReports(startTime);

		reports.put(Constants.CAT, stateReport);
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(startTime, StoragePolicy.FILE_AND_DB, m_index);
		} else {
			m_reportManager.storeHourlyReports(startTime, StoragePolicy.FILE, m_index);
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
		StateReport report = new StateReport(Constants.CAT);

		report.setStartTime(new Date(m_startTime));
		report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

		Machine machine = buildStateInfo(report.findOrCreateMachine(m_ip));
		StateReport stateReport = m_reportManager.getHourlyReport(getStartTime(), Constants.CAT, true);
		Map<String, ProcessDomain> processDomains = stateReport.findOrCreateMachine(m_ip).getProcessDomains();

		for (Map.Entry<String, ProcessDomain> entry : machine.getProcessDomains().entrySet()) {
			ProcessDomain processDomain = processDomains.get(entry.getKey());

			if (processDomain != null) {
				entry.getValue().getIps().addAll(processDomain.getIps());
			}
		}
		return report;
	}

	@Override
   public ReportManager<StateReport> getReportManager() {
	   return m_reportManager;
   }

	@Override
	protected void loadReports() {
		// do nothing
	}
	
	@Override
	protected void process(MessageTree tree) {
		String domain = tree.getDomain();

		if (m_serverFilterConfigManager.validateDomain(domain)) {
			StateReport report = m_reportManager.getHourlyReport(getStartTime(), Constants.CAT, true);
			String ip = tree.getIpAddress();
			Machine machine = report.findOrCreateMachine(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());

			machine.findOrCreateProcessDomain(domain).addIp(ip);
		}
	}
}
