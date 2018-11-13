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
package com.dianping.cat.consumer.storage;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.DatabaseParser;
import com.dianping.cat.consumer.storage.StorageReportUpdater.StorageUpdateItem;
import com.dianping.cat.consumer.storage.builder.StorageBuilder;
import com.dianping.cat.consumer.storage.builder.StorageItem;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;

@Named(type = MessageAnalyzer.class, value = StorageAnalyzer.ID, instantiationStrategy = Named.PER_LOOKUP)
public class StorageAnalyzer extends AbstractMessageAnalyzer<StorageReport> implements LogEnabled, Initializable {

	public static final String ID = "storage";

	@Inject(ID)
	private ReportManager<StorageReport> m_reportManager;

	@Inject
	private DatabaseParser m_databaseParser;

	@Inject
	private StorageReportUpdater m_updater;

	private Map<String, StorageBuilder> m_storageBuilders;

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB, m_index);
			m_databaseParser.showErrorCon();
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public StorageReport getReport(String id) {
		long period = getStartTime();
		StorageReport report = m_reportManager.getHourlyReport(period, id, false);

		m_updater.updateStorageIds(id, m_reportManager.getDomains(period), report);
		return report;
	}

	@Override
	public ReportManager<StorageReport> getReportManager() {
		return m_reportManager;
	}

	@Override
	public void initialize() throws InitializationException {
		m_storageBuilders = lookupMap(StorageBuilder.class);
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

	@Override
	protected void process(MessageTree tree) {
		List<Transaction> transactions = tree.getTransactions();

		for (Transaction t : transactions) {
			String domain = tree.getDomain();
			Collection<StorageBuilder> builders = m_storageBuilders.values();

			for (StorageBuilder builder : builders) {
				if (builder.isEligable(t)) {
					StorageItem item = builder.build(t);
					String id = item.getId();

					if (StringUtils.isNotEmpty(id)) {
						StorageReport report = m_reportManager.getHourlyReport(getStartTime(), item.getReportId(), true);
						StorageUpdateItem param = new StorageUpdateItem();

						param.setDomain(domain).setIp(item.getIp()).setMethod(item.getMethod()).setTransaction(t)
												.setThreshold(item.getThreshold());
						m_updater.updateStorageReport(report, param);
					}
				}
			}
		}
	}

}
