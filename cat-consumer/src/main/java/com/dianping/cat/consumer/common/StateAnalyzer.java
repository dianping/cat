package com.dianping.cat.consumer.common;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dainping.cat.consumer.dal.report.Hostinfo;
import com.dainping.cat.consumer.dal.report.HostinfoDao;
import com.dainping.cat.consumer.dal.report.Project;
import com.dainping.cat.consumer.dal.report.ProjectDao;
import com.dainping.cat.consumer.dal.report.ProjectEntity;
import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dainping.cat.consumer.dal.report.Task;
import com.dainping.cat.consumer.dal.report.TaskDao;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;
import com.dianping.cat.consumer.state.model.transform.DefaultXmlBuilder;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.status.ServerState.State;
import com.dianping.cat.status.ServerStateManager;
import com.dianping.cat.storage.BucketManager;
import com.site.dal.jdbc.DalException;
import com.site.dal.jdbc.DalNotFoundException;
import com.site.lookup.annotation.Inject;

public class StateAnalyzer extends AbstractMessageAnalyzer<StateReport> implements LogEnabled {

	private Map<String, StateReport> m_reports = new HashMap<String, StateReport>();

	@Inject
	private ServerStateManager m_serverStateManager;

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private HostinfoDao m_hostInfoDao;

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private TaskDao m_taskDao;

	@Inject
	private ProjectDao m_projectDao;

	@Override
	public void doCheckpoint(boolean atEnd) {
		storeReport(atEnd);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public Set<String> getDomains() {
		return m_reports.keySet();
	}

	@Override
	public StateReport getReport(String domain) {
		StateReport report = m_reports.get(domain);

		if (report == null) {
			report = new StateReport(domain);
		}
		Machine machine = report.findOrCreateMachine(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		
		buildStateInfo(machine);
		return report;
	}

	@Override
	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
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

		processDomains.addIp(ip);
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;
	}

	private void buildStateReport() {
		StateReport report = new StateReport();
		report.setDomain("Cat");
		report.setStartTime(new Date(m_startTime));
		report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

		Machine machine = report.findOrCreateMachine(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());

		// build cat state info
		long minute = 1000 * 60;
		buildStateInfo(machine);

		long start = m_startTime - minute * 60 * 2;
		long end = m_startTime - minute * 60;
		for (; start < end; start += minute) {
			m_serverStateManager.RemoveState(start);
		}
		try {
			Date period = new Date(m_startTime);
			String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

			DefaultXmlBuilder builder = new DefaultXmlBuilder(true);

			Report r = m_reportDao.createLocal();
			String xml = builder.buildXml(report);
			String domain = report.getDomain();

			r.setName("state");
			r.setDomain(domain);
			r.setPeriod(period);
			r.setIp(ip);
			r.setType(1);
			r.setContent(xml);

			m_reportDao.insert(r);
		} catch (Throwable e) {
			Cat.getProducer().logError(e);
		}

	}

	private void buildStateInfo(Machine machine) {
		long minute = 1000 * 60;
		long start = m_startTime;
		long end = m_startTime + minute * 60;

		for (; start < end; start += minute) {
			State state = m_serverStateManager.findState(start);

			com.dianping.cat.consumer.state.model.entity.Message temp = machine.findOrCreateMessage(start);
			temp.setTotal(state.getMessageTotal());
			temp.setTotalLoss(state.getMessageTotalLoss());
			temp.setDump(state.getMessageDump());
			temp.setDumpLoss(state.getMessageDumpLoss());
			temp.setSize(state.getMessageSize());
			temp.setDelayCount(state.getProcessDelayCount());
			temp.setDelaySum(state.getProcessDelaySum());
		}
	}

	public class Visitor extends BaseVisitor {

		@Override
		public void visitProcessDomain(ProcessDomain processDomain) {
			String domain = processDomain.getName();
			Set<String> ips = processDomain.getIps();

			for (String ip : ips) {
				try {
					Hostinfo info = m_hostInfoDao.createLocal();

					info.setDomain(domain);
					info.setIp(ip);
					m_hostInfoDao.insert(info);
				} catch (DalException e) {
					Cat.logError(e);
				}
			}

			try {
				insertDomainInfo(domain);
			} catch (Exception e) {

				Cat.logError(e);
			}
		}

	}

	private void storeReport(boolean atEnd) {
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		t.setStatus(Message.SUCCESS);
		try {
			for (StateReport report : m_reports.values()) {
				new Visitor().visitStateReport(report);
			}

			if (atEnd) {
				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
				// Create task for health report
				for (String domain : m_reports.keySet()) {
					try {
						Task task = m_taskDao.createLocal();

						task.setCreationDate(new Date());
						task.setProducer(ip);
						task.setReportDomain(domain);
						task.setReportName("health");
						task.setReportPeriod(period);
						task.setStatus(1); // status todo
						m_taskDao.insert(task);
					} catch (Exception e) {
						Cat.logError(e);
						t.setStatus(e);
					}
				}
				buildStateReport();
			}
		} catch (Exception e) {
			t.setStatus(e);
			Cat.logError(e);
		} finally {
			t.complete();
		}
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
}
