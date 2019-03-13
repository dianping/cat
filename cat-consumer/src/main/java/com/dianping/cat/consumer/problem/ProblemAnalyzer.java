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
package com.dianping.cat.consumer.problem;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Named(type = MessageAnalyzer.class, value = ProblemAnalyzer.ID, instantiationStrategy = Named.PER_LOOKUP)
public class ProblemAnalyzer extends AbstractMessageAnalyzer<ProblemReport> implements LogEnabled, Initializable {
	public static final String ID = "problem";

	@Inject(ID)
	private ReportManager<ProblemReport> m_reportManager;

	@Inject
	private List<ProblemHandler> m_handlers;

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

	public Set<String> getDomains() {
		return m_reportManager.getDomains(getStartTime());
	}

	@Override
	public ProblemReport getReport(String domain) {
		return m_reportManager.getHourlyReport(getStartTime(), domain, false);
	}

	@Override
	public ReportManager<ProblemReport> getReportManager() {
		return m_reportManager;
	}

	@Override
	public void initialize() throws InitializationException {
		// to work around a performance issue within plexus
		m_handlers = new ArrayList<ProblemHandler>(m_handlers);
	}

	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		ProblemReport report = m_reportManager.getHourlyReport(getStartTime(), domain, true);

		report.addIp(tree.getIpAddress());
		Machine machine = report.findOrCreateMachine(tree.getIpAddress());

		for (ProblemHandler handler : m_handlers) {
			handler.handle(machine, tree);
		}
	}

}
