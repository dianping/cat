package com.dianping.cat.consumer.state;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.DomainManager;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.state.model.entity.Detail;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.core.dal.Hostinfo;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.DefaultReportManager.StoragePolicy;
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

	@Inject
	private ServerConfigManager m_serverConfigManager;

	@Inject
	private String m_ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

	private Machine buildStateInfo(Machine machine) {
		long minute = 1000 * 60;
		long start = m_startTime;
		long end = m_startTime + minute * 60;
		int size = 0;
		double maxTps = 0;
		long current = System.currentTimeMillis();
		
		if (end > current) {
			end = current;
		}
		for (; start < end; start += minute) {
			Statistic state = m_serverStateManager.findState(start);

			if (state == null) {
				continue;
			}

			Message temp = machine.findOrCreateMessage(start);
			long messageTotal = state.getMessageTotal();
			long messageTotalLoss = state.getMessageTotalLoss();
			long messageSize = state.getMessageSize();

			temp.setTotal(messageTotal).setTotalLoss(messageTotalLoss).setSize(messageSize);
			machine.setTotal(messageTotal + machine.getTotal());
			machine.setTotalLoss(messageTotalLoss + machine.getTotalLoss());
			machine.setSize(messageSize + machine.getSize());

			Map<String, AtomicLong> totals = state.getMessageTotals();
			Map<String, AtomicLong> totalLosses = state.getMessageTotalLosses();
			Map<String, AtomicLong> sizes = state.getMessageSizes();

			for (Entry<String, AtomicLong> entry : totals.entrySet()) {
				String domain = entry.getKey();
				long value = entry.getValue().get();
				ProcessDomain processDomain = machine.findOrCreateProcessDomain(domain);
				Detail detail = processDomain.findOrCreateDetail(start);

				if (totals.containsKey(domain)) {
					processDomain.setTotal(value + processDomain.getTotal());
					detail.setTotal(value);
				}
				if (totalLosses.containsKey(domain)) {
					long losses = totalLosses.get(domain).get();

					processDomain.setTotalLoss(losses + processDomain.getTotalLoss());
					detail.setTotalLoss(losses);
				}
				if (sizes.containsKey(domain)) {
					long totalSize = sizes.get(domain).get();

					processDomain.setSize(totalSize + processDomain.getSize());
					detail.setSize(totalSize);
				}
			}

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

			long messageDump = state.getMessageDump();
			temp.setDump(messageDump);
			machine.setDump(machine.getDump() + messageDump);

			long messageDumpLoss = state.getMessageDumpLoss();
			temp.setDumpLoss(messageDumpLoss);
			machine.setDumpLoss(machine.getDumpLoss() + messageDumpLoss);

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
	public void doCheckpoint(boolean atEnd) {
		long startTime = getStartTime();
		StateReport stateReport = getReport(Constants.CAT);
		Map<String, StateReport> reports = m_reportManager.getHourlyReports(startTime);

		reports.put(Constants.CAT, stateReport);
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
	protected void process(MessageTree tree) {
		StateReport report = m_reportManager.getHourlyReport(getStartTime(), Constants.CAT, true);
		String domain = tree.getDomain();
		String ip = tree.getIpAddress();
		Machine machine = report.findOrCreateMachine(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());

		machine.findOrCreateProcessDomain(domain).addIp(ip);
		if (m_serverConfigManager.validateDomain(domain)) {
			if (!m_domainManager.containsDomainInCat(domain)) {
				m_domainManager.insertDomain(domain);
			}
			Hostinfo info = m_domainManager.queryHostInfoByIp(ip);

			if (info == null) {
				m_domainManager.insert(domain, ip);
			} else if (!info.getDomain().equals(domain)) {
				// only work on online environment
				long current = System.currentTimeMillis();
				Date lastModifiedDate = info.getLastModifiedDate();

				if (lastModifiedDate != null && (current - lastModifiedDate.getTime()) > ONE_HOUR) {
					m_domainManager.update(info.getId(), domain, ip);
					m_logger.info(String.format("old domain is %s , change ip %s to %s", info.getDomain(), ip, domain));
				}
			}
		}
	}

}
