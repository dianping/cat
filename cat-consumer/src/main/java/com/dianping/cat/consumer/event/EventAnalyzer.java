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
package com.dianping.cat.consumer.event;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.AtomicMessageConfigManager;
import com.dianping.cat.consumer.event.model.entity.*;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.List;
import java.util.Set;

@Named(type = MessageAnalyzer.class, value = EventAnalyzer.ID, instantiationStrategy = Named.PER_LOOKUP)
public class EventAnalyzer extends AbstractMessageAnalyzer<EventReport> implements LogEnabled {

	public static final String ID = "event";

	@Inject(ID)
	private ReportManager<EventReport> m_reportManager;

	@Inject
	private AtomicMessageConfigManager m_atomicMessageConfigManager;

	private final EventTpsStatisticsComputer m_computer = new EventTpsStatisticsComputer();

	private int m_typeCountLimit = 100;

	private static final int m_statusCodeCountLimit = 100;

	private long m_nextClearTime;

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB, m_index);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
		}
	}

	@Override
	public EventReport getReport(String domain) {
		long period = getStartTime();
		long timestamp = System.currentTimeMillis();
		long remainder = timestamp % 3600000;
		long current = timestamp - remainder;
		EventReport report = m_reportManager.getHourlyReport(period, domain, false);

		if (period == current) {
			report.accept(m_computer.setDuration(remainder / 1000.0));
		} else if (period < current) {
			report.accept(m_computer.setDuration(3600));
		}

		// report.getIps().addAll(report.getMachines().keySet());

		return report;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private EventType findOrCreateType(Machine machine, String type) {
		EventType eventType = machine.findType(type);

		if (eventType == null) {
			int size = machine.getTypes().size();

			if (size > m_typeCountLimit) {
				eventType = machine.findOrCreateType(CatConstants.OTHERS);
			} else {
				eventType = machine.findOrCreateType(type);
			}
		}

		return eventType;
	}

	private EventName findOrCreateName(EventType type, String name, String domain) {
		EventName eventName = type.findName(name);

		if (eventName == null) {
			int size = type.getNames().size();

			if (size > m_atomicMessageConfigManager.getMaxNameThreshold(domain)) {
				eventName = type.findOrCreateName(CatConstants.OTHERS);
			} else {
				eventName = type.findOrCreateName(name);
			}
		}

		return eventName;
	}

	private StatusCode findOrCreateStatusCode(EventName name, String codeName) {
		StatusCode code = name.findStatusCode(codeName);

		if (code == null) {
			int size = name.getStatusCodes().size();

			if (size > m_statusCodeCountLimit) {
				code = name.findOrCreateStatusCode(CatConstants.OTHERS);
			} else {
				code = name.findOrCreateStatusCode(codeName);
			}
		}
		return code;
	}

	private void cleanUpReports() {
		String minute = TimeHelper.getMinuteStr();
		Transaction t = Cat.newTransaction("CleanUpEventReports", minute);

		try {
			Set<String> domains = m_reportManager.getDomains(m_startTime);

			for (String domain : domains) {
				Transaction tran = Cat.newTransaction("CleanUpEvent", minute);

				tran.addData("domain", domain);

				EventReportCountFilter visitor = new EventReportCountFilter(m_serverConfigManager.getMaxTypeThreshold(),
										m_atomicMessageConfigManager.getMaxNameThreshold(domain), m_serverConfigManager.getTypeNameLengthLimit());

				try {
					EventReport report = m_reportManager.getHourlyReport(m_startTime, domain, false);

					visitor.visitEventReport(report);
					tran.setSuccessStatus();
				} catch (Exception e) {
					try {
						EventReport report = m_reportManager.getHourlyReport(m_startTime, domain, false);

						visitor.visitEventReport(report);
						tran.setSuccessStatus();
					} catch (Exception re) {
						tran.setStatus(re);
						Cat.logError(re);
					}
				} finally {
					tran.complete();
				}
			}
			t.setSuccessStatus();
		} catch (Exception e) {
			Cat.logError(e);
		} finally {
			t.complete();
		}
	}

	private String formatStatus(String status) {
		if (status.length() > 128) {
			return status.substring(0, 128);
		} else {
			return status;
		}
	}

	@Override
	public ReportManager<EventReport> getReportManager() {
		return m_reportManager;
	}

	public void setReportManager(ReportManager<EventReport> reportManager) {
		m_reportManager = reportManager;
	}

	@Override
	public void initialize(long startTime, long duration, long extraTime) {
		super.initialize(startTime, duration, extraTime);

		m_typeCountLimit = m_serverConfigManager.getMaxTypeThreshold();

		final long current = System.currentTimeMillis();

		if (startTime < current) {
			m_nextClearTime = TimeHelper.getCurrentMinute().getTime() + TimeHelper.ONE_MINUTE * 2;
		} else {
			m_nextClearTime = startTime + TimeHelper.ONE_MINUTE * 2;
		}
	}

	@Override
	public boolean isEligable(MessageTree tree) {
		List<Event> events = tree.getEvents();

		if (events != null && events.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		String ip = tree.getIpAddress();
		EventReport report = m_reportManager.getHourlyReport(getStartTime(), domain, true);
		List<Event> events = tree.findOrCreateEvents();

		for (Event event : events) {
			String data = String.valueOf(event.getData());
			int total = 1;
			int fail = 0;
			boolean batchData = data.length() > 0 && data.charAt(0) == CatConstants.BATCH_FLAG;

			if (batchData) {
				String[] tab = data.substring(1).split(CatConstants.SPLIT);

				total = Integer.parseInt(tab[0]);
				fail = Integer.parseInt(tab[1]);
			} else {
				if (!event.isSuccess()) {
					fail = 1;
				}
			}
			processEvent(report, tree, event, ip, total, fail, batchData);
		}

		if (System.currentTimeMillis() > m_nextClearTime) {
			m_nextClearTime = m_nextClearTime + TimeHelper.ONE_MINUTE;

			Threads.forGroup("cat").start(new Runnable() {

				@Override
				public void run() {
					cleanUpReports();
				}

			});
		}
	}

	private void processEvent(EventReport report, MessageTree tree, Event event, String ip, int total, int fail,
							boolean batchData) {
		Machine machine = report.findOrCreateMachine(ip);
		EventType type = findOrCreateType(machine, event.getType());
		EventName name = findOrCreateName(type, event.getName(), report.getDomain());
		String messageId = tree.getMessageId();

		type.incTotalCount(total);
		name.incTotalCount(total);

		if (fail > 0) {
			type.incFailCount(fail);
			name.incFailCount(fail);
		}

		if (type.getSuccessMessageUrl() == null) {
			type.setSuccessMessageUrl(messageId);
		}

		if (name.getSuccessMessageUrl() == null) {
			name.setSuccessMessageUrl(messageId);
		}

		if (!batchData) {
			if (event.isSuccess()) {
				type.setSuccessMessageUrl(messageId);
				name.setSuccessMessageUrl(messageId);
			} else {
				type.setSuccessMessageUrl(messageId);
				name.setSuccessMessageUrl(messageId);

				String statusCode = formatStatus(event.getStatus());

				findOrCreateStatusCode(name, statusCode).incCount();
			}
		}

		type.setFailPercent(type.getFailCount() * 100.0 / type.getTotalCount());
		name.setFailPercent(name.getFailCount() * 100.0 / name.getTotalCount());

		processEventGrpah(name, event, total, fail);
	}

	private void processEventGrpah(EventName name, Event event, int total, int fail) {
		long current = event.getTimestamp() / 1000 / 60;
		int min = (int) (current % (60));
		Range range = name.findOrCreateRange(min);

		range.incCount(total);

		if (fail > 0) {
			range.incFails(fail);
		}
	}

}
