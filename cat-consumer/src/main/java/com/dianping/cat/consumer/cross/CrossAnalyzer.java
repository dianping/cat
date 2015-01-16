package com.dianping.cat.consumer.cross;

import java.util.List;

import com.dianping.cat.consumer.cross.model.entity.*;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.CatConstants;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.DefaultReportManager.StoragePolicy;
import com.dianping.cat.service.ReportManager;
import org.unidal.lookup.util.StringUtils;

public class CrossAnalyzer extends AbstractMessageAnalyzer<CrossReport> implements LogEnabled {
	public static final String ID = "cross";

	@Inject(ID)
	protected ReportManager<CrossReport> m_reportManager;

	@Inject
	private IpConvertManager m_ipConvertManager;

	private static final String UNKNOWN = "Unknown";

	private int m_discardLogs = 0;

	private int m_errorAppName;

	@Override
	public void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB);

			m_logger.info("discard server logview count " + m_discardLogs + ", errorAppName " + m_errorAppName);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public CrossReport getReport(String domain) {
		CrossReport report = m_reportManager.getHourlyReport(getStartTime(), domain, false);

		report.getDomainNames().addAll(m_reportManager.getDomains(getStartTime()));
		return report;
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE);
	}

	public CrossInfo parseCrossTransaction(Transaction t, MessageTree tree) {
		if (m_serverConfigManager.discardTransaction(t)) {
			return null;
		} else {
			String type = t.getType();

			if (m_serverConfigManager.isClientCall(type)) {
				return parsePigeonClientTransaction(t, tree);
			} else if (m_serverConfigManager.isServer(type)) {
				return parsePigeonServerTransaction(t, tree);
			}
			return null;
		}
	}

	private CrossInfo parsePigeonClientTransaction(Transaction t, MessageTree tree) {
		CrossInfo crossInfo = new CrossInfo();
		String localIp = tree.getIpAddress();
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				String type = message.getType();

                if (CatConstants.TYPE_ESB_CALL_SERVER.equals(type) || CatConstants.TYPE_SOA_CALL_SERVER.equals(type)) {
					crossInfo.setRemoteAddress(message.getName());
				}
                if (CatConstants.TYPE_ESB_CALL_APP.equals(type) || CatConstants.TYPE_SOA_CALL_APP.equals(type)) {
					crossInfo.setApp(message.getName());
				}
			}
		}

		crossInfo.setLocalAddress(localIp);
        crossInfo.setRemoteRole(t.getType() + ".Server");
        crossInfo.setDetailType(t.getType());
		return crossInfo;
	}

	public CrossInfo convertCrossInfo(String client, CrossInfo crossInfo) {
		String localIp = crossInfo.getLocalAddress();
		String remoteAddress = crossInfo.getRemoteAddress();
		int index = remoteAddress.indexOf(":");

		if (index > 0) {
			remoteAddress = remoteAddress.substring(0, index);
		}

		CrossInfo info = new CrossInfo();
		info.setLocalAddress(remoteAddress);
		info.setRemoteAddress(localIp + ":Caller");
        info.setRemoteRole(crossInfo.getDetailType() + ".Caller");
        info.setDetailType(crossInfo.getDetailType());
		info.setApp(client);

		return info;
	}

	private void updateServerCrossReport(Transaction t, String remoteDomain, CrossInfo info) {
		CrossReport report = m_reportManager.getHourlyReport(getStartTime(), remoteDomain, true);

		updateCrossReport(report, t, info);
	}

	private CrossInfo parsePigeonServerTransaction(Transaction t, MessageTree tree) {
		CrossInfo crossInfo = new CrossInfo();
		String localIp = tree.getIpAddress();
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				String type = message.getType();
                if (CatConstants.TYPE_ESB_SERVICE_CLIENT.equals(type) || CatConstants.TYPE_SOA_SERVICE_CLIENT.equals(type)) {
					String name = message.getName();
					int index = name.indexOf(":");

					if (index > 0) {
						name = name.substring(0, index);
					}
					String formatIp = m_ipConvertManager.convertHostNameToIP(name);

					if (formatIp != null && formatIp.length() > 0) {
						crossInfo.setRemoteAddress(formatIp);
					}
				}
                if (CatConstants.TYPE_ESB_SERVICE_APP.equals(type) || CatConstants.TYPE_SOA_SERVICE_APP.equals(type)) {
					crossInfo.setApp(message.getName());
				}
			}
		}

		if (crossInfo.getRemoteAddress().equals(UNKNOWN)) {
			m_discardLogs++;
			return null;
		}

		crossInfo.setLocalAddress(localIp);
        crossInfo.setRemoteRole(t.getType() + ".Client");
        crossInfo.setDetailType(t.getType());
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
		CrossInfo crossInfo = parseCrossTransaction(t, tree);

		if (crossInfo != null) {
			updateCrossReport(report, t, crossInfo);

			String remoteDomain = crossInfo.getApp();
			if (m_serverConfigManager.isClientCall(t.getType()) && StringUtils.isNotEmpty(remoteDomain)) {
				CrossInfo info = convertCrossInfo(tree.getDomain(), crossInfo);
				updateServerCrossReport(t, remoteDomain, info);
			}else{
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
		String localIp = info.getLocalAddress();
		String remoteIp = info.getRemoteAddress();
		String role = info.getRemoteRole();
		String transactionName = t.getName();
		Local local = report.findOrCreateLocal(localIp);
		Remote remote = local.findOrCreateRemote(remoteIp);

		report.addIp(localIp);
		remote.setRole(role);
		remote.setApp(info.getApp());

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

	public static class CrossInfo {
		private String m_remoteRole = UNKNOWN;

		private String m_LocalAddress = UNKNOWN;

		private String m_RemoteAddress = UNKNOWN;

		private String m_detailType = UNKNOWN;

		private String m_app = "";

		public String getDetailType() {
			return m_detailType;
		}

		public String getLocalAddress() {
			return m_LocalAddress;
		}

		public String getRemoteAddress() {
			return m_RemoteAddress;
		}

		public String getRemoteRole() {
			return m_remoteRole;
		}

		public String getApp() {
			return m_app;
		}

		public void setDetailType(String detailType) {
			m_detailType = detailType;
		}

		public void setLocalAddress(String localAddress) {
			m_LocalAddress = localAddress;
		}

		public void setRemoteAddress(String remoteAddress) {
			m_RemoteAddress = remoteAddress;
		}

		public void setRemoteRole(String remoteRole) {
			m_remoteRole = remoteRole;
		}

		public void setApp(String app) {
			m_app = app;
		}
	}
}
