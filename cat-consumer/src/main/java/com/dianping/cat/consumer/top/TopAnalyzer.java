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
package com.dianping.cat.consumer.top;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.top.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;

@Named(type = MessageAnalyzer.class, value = TopAnalyzer.ID, instantiationStrategy = Named.PER_LOOKUP)
public class TopAnalyzer extends AbstractMessageAnalyzer<TopReport> implements LogEnabled {
	public static final String ID = "top";

	@Inject(ID)
	private ReportManager<TopReport> m_reportManager;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	private Set<String> m_errorTypes;

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		long startTime = getStartTime();

		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(startTime, StoragePolicy.FILE_AND_DB, m_index);
		} else {
			m_reportManager.storeHourlyReports(startTime, StoragePolicy.FILE, m_index);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public TopReport getReport(String domain) {
		return m_reportManager.getHourlyReport(getStartTime(), Constants.CAT, false);
	}

	@Override
	public ReportManager<TopReport> getReportManager() {
		return m_reportManager;
	}

	@Override
	public boolean isEligable(MessageTree tree) {
		if (tree.getEvents().size() > 0) {
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

		if (m_serverFilterConfigManager.validateDomain(domain)) {
			TopReport report = m_reportManager.getHourlyReport(getStartTime(), Constants.CAT, true);

			List<Event> events = tree.getEvents();

			for (Event e : events) {
				processEvent(report, tree, e);
			}
		}
	}

	private void processEvent(TopReport report, MessageTree tree, Event event) {
		String type = event.getType();

		if (m_errorTypes.contains(type)) {
			String domain = tree.getDomain();
			String ip = tree.getIpAddress();
			String exception = event.getName();
			long current = event.getTimestamp() / 1000 / 60;
			int min = (int) (current % (60));
			Segment segment = report.findOrCreateDomain(domain).findOrCreateSegment(min).incError();

			segment.findOrCreateError(exception).incCount();
			segment.findOrCreateMachine(ip).incCount();
		}
	}

	public void setErrorType(String type) {
		m_errorTypes = new HashSet<String>(Splitters.by(',').noEmptyItem().split(type));
	}
}