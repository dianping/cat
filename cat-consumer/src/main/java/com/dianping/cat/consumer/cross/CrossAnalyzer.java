/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.consumer.cross;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.cross.model.entity.*;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.util.StringUtils;

import java.util.List;

@Named(type = MessageAnalyzer.class, value = CrossAnalyzer.ID, instantiationStrategy = Named.PER_LOOKUP)
public class CrossAnalyzer extends AbstractMessageAnalyzer<CrossReport> implements LogEnabled {
	public static final String ID = "cross";

	public static final String DEFAULT = "unknown";

	@Inject(ID)
	protected ReportManager<CrossReport> m_reportManager;

	@Inject
	protected IpConvertManager m_ipConvertManager;

	private int m_discardLogs = 0;

	private int m_errorAppName;

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
	public CrossReport getReport(String domain) {
		return m_reportManager.getHourlyReport(getStartTime(), domain, false);
	}

	@Override
	public ReportManager<CrossReport> getReportManager() {
		return m_reportManager;
	}

	public void setReportManager(ReportManager<CrossReport> reportManager) {
		m_reportManager = reportManager;
	}

	@Override
	public boolean isEligable(MessageTree tree) {
		if (tree.getTransactions().size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	public CrossInfo parseCrossTransaction(Transaction t, MessageTree tree) {
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

		report.addIp(tree.getIpAddress());

		List<Transaction> transactions = tree.getTransactions();

		for (Transaction t : transactions) {
			processTransaction(report, tree, (Transaction) t);
		}
	}

	private void processTransaction(CrossReport report, MessageTree tree, Transaction t) {
		CrossInfo crossInfo = parseCrossTransaction(t, tree);

		if (crossInfo != null && crossInfo.validate()) {
			updateCrossReport(report, t, crossInfo);

			String targetDomain = crossInfo.getApp();

			if (m_serverConfigManager.isRpcClient(t.getType()) && !DEFAULT.equals(targetDomain)	&& !"null"
									.equalsIgnoreCase(targetDomain)) {
				CrossInfo serverCrossInfo = convertCrossInfo(tree.getDomain(), crossInfo);

				if (serverCrossInfo != null) {
					CrossReport serverReport = m_reportManager.getHourlyReport(getStartTime(), targetDomain, true);

					updateCrossReport(serverReport, t, serverCrossInfo);
				}
			} else {
				m_errorAppName++;
			}
		}
	}

	public void setIpConvertManager(IpConvertManager ipConvertManager) {
		m_ipConvertManager = ipConvertManager;
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

		public void setApp(String app) {
			m_app = app;
		}

		public String getClientPort() {
			return m_clientPort;
		}

		public void setClientPort(String clientPort) {
			m_clientPort = clientPort;
		}

		public String getDetailType() {
			return m_detailType;
		}

		public void setDetailType(String detailType) {
			m_detailType = detailType;
		}

		public String getLocalAddress() {
			return m_localAddress;
		}

		public void setLocalAddress(String localAddress) {
			m_localAddress = localAddress;
		}

		public String getRemoteAddress() {
			return m_remoteAddress;
		}

		public void setRemoteAddress(String remoteAddress) {
			m_remoteAddress = remoteAddress;
		}

		public String getRemoteRole() {
			return m_remoteRole;
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
