package com.dianping.cat.consumer.cross;

import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.logging.LogEnabled;
import org.unidal.lookup.logging.Logger;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.entity.Local;
import com.dianping.cat.consumer.cross.model.entity.Name;
import com.dianping.cat.consumer.cross.model.entity.Remote;
import com.dianping.cat.consumer.cross.model.entity.Type;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;

public class CrossAnalyzer extends AbstractMessageAnalyzer<CrossReport> implements LogEnabled {
	public static final String ID = "cross";

	@Inject(ID)
	protected ReportManager<CrossReport> m_reportManager;

	@Inject
	protected IpConvertManager m_ipConvertManager;

	private int m_discardLogs = 0;

	private int m_errorAppName;

	public static final String DEFAULT = "unknown";

	public CrossInfo convertCrossInfo(String client, CrossInfo crossInfo) {
		String localAddress = crossInfo.getLocalAddress();
		String remoteAddress = crossInfo.getRemoteAddress();

		int index = remoteAddress.indexOf(":");

		if (index > 0) {
			remoteAddress = remoteAddress.substring(0, index);
		}

		CrossInfo info = new CrossInfo();
		info.setLocalAddress(remoteAddress);

		String clientPort = crossInfo.getClientPort();

		if (clientPort == null) {
			info.setRemoteAddress(localAddress);
		} else {
			info.setRemoteAddress(localAddress + ":" + clientPort);
		}
		info.setRemoteRole("Pigeon.Caller");
		info.setDetailType("PigeonCall");
		info.setApp(client);

		return info;
	}

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB, m_index);

			m_logger.info("discard server logview count " + m_discardLogs + ", errorAppName " + m_errorAppName);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public int getAnalyzerCount() {
		return 2;
	}

	@Override
	public CrossReport getReport(String domain) {
		CrossReport report = m_reportManager.getHourlyReport(getStartTime(), domain, false);

		report.getDomainNames().addAll(m_reportManager.getDomains(getStartTime()));
		return report;
	}

	@Override
	public ReportManager<CrossReport> getReportManager() {
		return m_reportManager;
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	public CrossInfo parseCorssTransaction(Transaction t, MessageTree tree) {
		String type = t.getType();

		if (m_serverConfigManager.isRpcClient(type)) {
			return parsePigeonClientTransaction(t, tree);
		} else if (m_serverConfigManager.isRpcServer(type)) {
			return parsePigeonServerTransaction(t, tree);
		}
		return null;
	}

	private CrossInfo parsePigeonClientTransaction(Transaction t, MessageTree tree) {
		CrossInfo crossInfo = new CrossInfo();
		String localAddress = tree.getIpAddress();
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				String type = message.getType();

				if (type.equals("PigeonCall.server") || type.equals("Call.server")) {
					crossInfo.setRemoteAddress(message.getName());
				}
				if (type.equals("PigeonCall.app") || type.equals("Call.app")) {
					crossInfo.setApp(message.getName());
				}
				if (type.equals("PigeonCall.port") || type.equals("Call.port")) {
					crossInfo.setClientPort(message.getName());
				}
			}
		}

		crossInfo.setLocalAddress(localAddress);
		crossInfo.setRemoteRole("Pigeon.Server");
		crossInfo.setDetailType("PigeonCall");
		return crossInfo;
	}

	private CrossInfo parsePigeonServerTransaction(Transaction t, MessageTree tree) {
		CrossInfo crossInfo = new CrossInfo();
		String localAddress = tree.getIpAddress();
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				String type = message.getType();

				if (type.equals("PigeonService.client") || type.equals("Service.client")) {
					crossInfo.setRemoteAddress(message.getName());
				}
				if (type.equals("PigeonService.app") || type.equals("Service.app")) {
					crossInfo.setApp(message.getName());
				}
			}
		}

		crossInfo.setLocalAddress(localAddress);
		crossInfo.setRemoteRole("Pigeon.Client");
		crossInfo.setDetailType("PigeonService");
		return crossInfo;
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		CrossReport report = m_reportManager.getHourlyReport(getStartTime(), domain, true);

		Message message = tree.getMessage();
		report.addIp(tree.getIpAddress());

		if (message instanceof Transaction) {
			processTransaction(report, tree, (Transaction) message);
		}
	}

	private void processTransaction(CrossReport report, MessageTree tree, Transaction t) {
		CrossInfo crossInfo = parseCorssTransaction(t, tree);

		if (crossInfo != null && crossInfo.validate()) {
			updateCrossReport(report, t, crossInfo);

			String targetDomain = crossInfo.getApp();

			if (m_serverConfigManager.isRpcClient(t.getType()) && !DEFAULT.equals(targetDomain)) {
				CrossInfo serverCrossInfo = convertCrossInfo(tree.getDomain(), crossInfo);

				if (serverCrossInfo != null) {
					CrossReport serverReport = m_reportManager.getHourlyReport(getStartTime(), targetDomain, true);

					updateCrossReport(serverReport, t, serverCrossInfo);
				}
			} else {
				m_errorAppName++;
			}
		}
		List<Message> children = t.getChildren();

		for (Message child : children) {
			if (child instanceof Transaction) {
				processTransaction(report, tree, (Transaction) child);
			}
		}
	}

	public void setIpConvertManager(IpConvertManager ipConvertManager) {
		m_ipConvertManager = ipConvertManager;
	}

	public void setReportManager(ReportManager<CrossReport> reportManager) {
		m_reportManager = reportManager;
	}

	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		m_serverConfigManager = serverConfigManager;
	}

	private void updateCrossReport(CrossReport report, Transaction t, CrossInfo info) {
		synchronized (report) {
			String localIp = info.getLocalAddress();
			String remoteIp = info.getRemoteAddress();
			String role = info.getRemoteRole();
			String transactionName = t.getName();

			Local local = report.findOrCreateLocal(localIp);
			String remoteId = remoteIp + ":" + role;
			Remote remote = local.findOrCreateRemote(remoteId);

			report.addIp(localIp);

			if (StringUtils.isEmpty(remote.getIp())) {
				remote.setIp(remoteIp);
			}
			if (StringUtils.isEmpty(remote.getRole())) {
				remote.setRole(role);
			}
			if (StringUtils.isEmpty(remote.getApp())) {
				remote.setApp(info.getApp());
			}

			Type type = remote.getType();

			if (type == null) {
				type = new Type();
				type.setId(info.getDetailType());
				remote.setType(type);
			}

			Name name = type.findOrCreateName(transactionName);

			type.incTotalCount();
			name.incTotalCount();

			if (!t.isSuccess()) {
				type.incFailCount();
				name.incFailCount();
			}

			double duration = t.getDurationInMicros() / 1000d;

			type.setSum(type.getSum() + duration);
			name.setSum(name.getSum() + duration);
		}
	}

	public static class CrossInfo {
		private String m_remoteRole;

		private String m_localAddress;

		private String m_remoteAddress;

		private String m_detailType;

		private String m_app;

		private String m_clientPort;

		public String getApp() {
			if (StringUtils.isEmpty(m_app)) {
				return DEFAULT;
			} else {
				return m_app;
			}
		}

		public String getClientPort() {
			return m_clientPort;
		}

		public String getDetailType() {
			return m_detailType;
		}

		public String getLocalAddress() {
			return m_localAddress;
		}

		public String getRemoteAddress() {
			return m_remoteAddress;
		}

		public String getRemoteRole() {
			return m_remoteRole;
		}

		public void setApp(String app) {
			m_app = app;
		}

		public void setClientPort(String clientPort) {
			m_clientPort = clientPort;
		}

		public void setDetailType(String detailType) {
			m_detailType = detailType;
		}

		public void setLocalAddress(String localAddress) {
			m_localAddress = localAddress;
		}

		public void setRemoteAddress(String remoteAddress) {
			m_remoteAddress = remoteAddress;
		}

		public void setRemoteRole(String remoteRole) {
			m_remoteRole = remoteRole;
		}

		public boolean validate() {
			if (m_localAddress != null && m_remoteAddress != null) {
				return true;
			} else {
				return false;
			}
		}
	}

}
