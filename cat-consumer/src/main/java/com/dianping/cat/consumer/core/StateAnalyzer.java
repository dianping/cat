package com.dianping.cat.consumer.core;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;
import com.dianping.cat.consumer.state.model.transform.DefaultXmlBuilder;
import com.dianping.cat.core.dal.Hostinfo;
import com.dianping.cat.core.dal.HostinfoDao;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.statistic.ServerStatistic.Statistic;
import com.dianping.cat.statistic.ServerStatisticManager;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

public class StateAnalyzer extends AbstractMessageAnalyzer<StateReport> implements LogEnabled {
	public static final String ID = "state";

	@Inject
	private ServerStatisticManager m_serverStateManager;

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private HostinfoDao m_hostInfoDao;

	@Inject
	private HourlyReportDao m_reportDao;

	@Inject
	private ProjectDao m_projectDao;
	
	@Inject
	private TaskManager m_taskManager;

	private Set<String> m_domains = new HashSet<String>();

	private Map<String, StateReport> m_reports = new HashMap<String, StateReport>();

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

	private void storeStateReport(StateReport report) {
		try {
			Date period = new Date(m_startTime);
			String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

			DefaultXmlBuilder builder = new DefaultXmlBuilder(true);

			HourlyReport r = m_reportDao.createLocal();
			String xml = builder.buildXml(report);
			String domain = report.getDomain();

			r.setName(ID);
			r.setDomain(domain);
			r.setPeriod(period);
			r.setIp(ip);
			r.setType(1);
			r.setContent(xml);

			m_reportDao.insert(r);
			
			m_taskManager.createTask(period, domain, ID, TaskProlicy.ALL_EXCLUED_HOURLY);
		} catch (Throwable e) {
			Cat.getProducer().logError(e);
		}
	}

	@Override
	public void doCheckpoint(boolean atEnd) {
		storeReport(atEnd);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public StateReport getReport(String domain) {
		StateReport report = new StateReport(domain);
		report = new StateReport("Cat");
		report.setStartTime(new Date(m_startTime));
		report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

		report.getMachines().clear();
		String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
		Machine machine = report.findOrCreateMachine(ip);

		buildStateInfo(machine);
		StateReport base = m_reports.get(domain);

		machine.getProcessDomains().putAll(base.findOrCreateMachine(ip).getProcessDomains());
		return report;
	}

	private void insertDomainInfo(String domain) {
		try {
			m_projectDao.findByDomain(domain, ProjectEntity.READSET_FULL);
		} catch (DalNotFoundException e) {
			Project project = m_projectDao.createLocal();

			project.setDomain(domain);
			project.setProjectLine("Default");
			project.setDepartment("Default");

			try {
				m_projectDao.insert(project);
			} catch (Exception ex) {
				Cat.logError(ex);
			}

		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	@Override
	protected void process(MessageTree tree) {
		StateReport report = m_reports.get("Cat");

		if (report == null) {
			report = new StateReport("Cat");
			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

			m_reports.put("Cat", report);
		}

		String domain = tree.getDomain();
		String ip = tree.getIpAddress();
		Machine machine = report.findOrCreateMachine(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		ProcessDomain processDomains = machine.findOrCreateProcessDomain(domain);

		if (validate(domain) && !m_domains.contains(domain)) {
			insertDomainInfo(domain);
			m_domains.add(domain);
		}
		processDomains.addIp(ip);
	}

	private boolean validate(String domain) {
		return !domain.equals("PhoenixAgent") && !domain.equals("FrondEnd");
	}

	private void storeReport(boolean atEnd) {
		if (atEnd) {
			Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

			t.setStatus(Message.SUCCESS);
			try {
				// insert the domain-ip info
				for (StateReport report : m_reports.values()) {
					new Visitor().visitStateReport(report);
				}
				// build cat state info
				for (String domain : m_reports.keySet()) {
					StateReport report = getReport(domain);

					Bucket<String> reportBucket = m_bucketManager.getReportBucket(m_startTime, "state");
					reportBucket.storeById(domain, report.toString());

					if (atEnd) {
						storeStateReport(report);

						long minute = 1000 * 60;
						long start = m_startTime - minute * 60 * 2;
						long end = m_startTime - minute * 60;

						for (; start < end; start += minute) {
							m_serverStateManager.removeState(start);
						}
					}
				}
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
			} finally {
				t.complete();
			}
		}
	}

	public class Visitor extends BaseVisitor {

		@Override
		public void visitProcessDomain(ProcessDomain processDomain) {
			String domain = processDomain.getName();
			Set<String> ips = processDomain.getIps();

			for (String ip : ips) {
				try {
					// Hack For PhoenixAgent
					if (validate(domain)) {
						Hostinfo info = m_hostInfoDao.createLocal();

						info.setDomain(domain);
						info.setIp(ip);
						m_hostInfoDao.insert(info);
					}
				} catch (DalException e) {
					Cat.logError(e);
				}
			}
		}
	}

}
