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
package com.dianping.cat.consumer.dependency;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.DatabaseParser;
import com.dianping.cat.consumer.DatabaseParser.Database;
import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Named(type = MessageAnalyzer.class, value = DependencyAnalyzer.ID, instantiationStrategy = Named.PER_LOOKUP)
public class DependencyAnalyzer extends AbstractMessageAnalyzer<DependencyReport> implements LogEnabled {
	public static final String ID = "dependency";

	@Inject(ID)
	private ReportManager<DependencyReport> m_reportManager;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	@Inject
	private DatabaseParser m_parser;

	private Set<String> m_types = new HashSet<String>(
	      Arrays.asList("URL", "SQL", "Call", "PigeonCall", "Service", "PigeonService"));

	private Set<String> m_exceptions = new HashSet<String>(Arrays.asList("Exception", "RuntimeException", "Error"));

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB, m_index);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private DependencyReport findOrCreateReport(String domain) {
		return m_reportManager.getHourlyReport(getStartTime(), domain, true);
	}

	@Override
	public DependencyReport getReport(String domain) {
		return m_reportManager.getHourlyReport(getStartTime(), domain, false);
	}

	@Override
	public ReportManager<DependencyReport> getReportManager() {
		return m_reportManager;
	}

	private boolean isCache(String type) {
		return type.startsWith("Cache.");
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

	private String parseDatabase(Transaction t) {
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				String type = message.getType();

				if (type.equals("SQL.Database")) {
					Database database = m_parser.parseDatabase(message.getName());

					return database != null ? database.getName() : null;
				}
			}
		}
		return null;
	}

	private String parseServerName(Transaction t) {
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				if (message.getType().equals("PigeonCall.app")) {
					return message.getName();
				}
			}
		}
		return null;
	}

	@Override
	public void process(MessageTree tree) {
		DependencyReport report = findOrCreateReport(tree.getDomain());
		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			processTransaction(report, tree, (Transaction) message);
		} else if (message instanceof Event) {
			processEvent(report, tree, (Event) message);
		}
	}

	private void processEvent(DependencyReport report, MessageTree tree, Event event) {
		String type = event.getType();

		if (m_exceptions.contains(type)) {
			long current = event.getTimestamp() / 1000 / 60;
			int min = (int) (current % (60));
			Segment segment = report.findOrCreateSegment(min);
			Index index = segment.findOrCreateIndex("Exception");

			index.incTotalCount();
			index.incErrorCount();
		}
	}

	private void processPigeonTransaction(DependencyReport report, MessageTree tree, Transaction t, String type) {
		if ("PigeonCall".equals(type) || "Call".equals(type)) {
			String target = parseServerName(t);
			String callType = "PigeonCall";

			if (target != null && !"null".equalsIgnoreCase(target)) {
				updateDependencyInfo(report, t, target, callType);
				DependencyReport serverReport = findOrCreateReport(target);

				updateDependencyInfo(serverReport, t, tree.getDomain(), "PigeonService");
			}
		}
	}

	private void processSqlTransaction(DependencyReport report, Transaction t, String type) {
		if ("SQL".equals(type)) {
			String database = parseDatabase(t);

			if (database != null) {
				updateDependencyInfo(report, t, database, "Database");
			}
		}
	}

	private void processTransaction(DependencyReport report, MessageTree tree, Transaction t) {
		String type = t.getType();

		processTransactionType(report, t, type);
		processSqlTransaction(report, t, type);
		processPigeonTransaction(report, tree, t, type);

		List<Message> children = t.getChildren();

		for (Message child : children) {
			if (child instanceof Transaction) {
				processTransaction(report, tree, (Transaction) child);
			} else if (child instanceof Event) {
				processEvent(report, tree, (Event) child);
			}
		}
	}

	private void processTransactionType(DependencyReport report, Transaction t, String type) {
		if (m_types.contains(type) || isCache(type)) {
			long current = t.getTimestamp() / 1000 / 60;
			int min = (int) (current % (60));
			Segment segment = report.findOrCreateSegment(min);
			Index index = segment.findOrCreateIndex(type);

			if (!t.getStatus().equals(Transaction.SUCCESS)) {
				index.incErrorCount();
			}
			index.incTotalCount();
			index.setSum(index.getSum() + t.getDurationInMillis());
			index.setAvg(index.getSum() / index.getTotalCount());
		}

		if (isCache(type)) {
			updateDependencyInfo(report, t, type, "Cache");
		}
	}

	private void updateDependencyInfo(DependencyReport report, Transaction t, String target, String type) {
		synchronized (report) {
			long current = t.getTimestamp() / 1000 / 60;
			int min = (int) (current % (60));
			Segment segment = report.findOrCreateSegment(min);
			Dependency dependency = segment.findOrCreateDependency(type + ":" + target);

			dependency.setType(type);
			dependency.setTarget(target);

			if (!t.getStatus().equals(Transaction.SUCCESS)) {
				dependency.incErrorCount();
			}
			dependency.incTotalCount();
			dependency.setSum(dependency.getSum() + t.getDurationInMillis());
			dependency.setAvg(dependency.getSum() / dependency.getTotalCount());
		}
	}

}
