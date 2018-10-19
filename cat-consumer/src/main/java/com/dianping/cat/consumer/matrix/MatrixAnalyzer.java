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
package com.dianping.cat.consumer.matrix;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.matrix.model.entity.Matrix;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.entity.Ratio;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Named(type = MessageAnalyzer.class, value = MatrixAnalyzer.ID, instantiationStrategy = Named.PER_LOOKUP)
public class MatrixAnalyzer extends AbstractMessageAnalyzer<MatrixReport> implements LogEnabled {
	public static final String ID = "matrix";

	@Inject(ID)
	private ReportManager<MatrixReport> m_reportManager;

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

	@Override
	public MatrixReport getReport(String domain) {
		return m_reportManager.getHourlyReport(getStartTime(), domain, false);
	}

	@Override
	public ReportManager<MatrixReport> getReportManager() {
		return m_reportManager;
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
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
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		MatrixReport report = m_reportManager.getHourlyReport(getStartTime(), domain, true);
		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			String messageType = message.getType();

			if (messageType.equals("URL") || messageType.equals("Service") || messageType.equals("PigeonService")) {
				Matrix matrix = report.findOrCreateMatrix(message.getName());
				matrix.setType(message.getType());
				matrix.setName(message.getName());
				long duration = ((Transaction) message).getDurationInMicros();
				matrix.incCount();
				matrix.setTotalTime(matrix.getTotalTime() + duration);

				Map<String, Ratio> ratios = new HashMap<String, Ratio>();
				ratios.put("Call", new Ratio());
				ratios.put("SQL", new Ratio());
				ratios.put("Cache", new Ratio());

				processTransaction(tree, (Transaction) message, ratios);

				for (Entry<String, Ratio> entry : ratios.entrySet()) {
					String type = entry.getKey();
					Ratio ratio = entry.getValue();
					int count = ratio.getTotalCount();
					long time = ratio.getTotalTime();

					Ratio real = matrix.findOrCreateRatio(type);
					if (real.getMin() > count || real.getMin() == 0) {
						real.setMin(count);
					}
					if (real.getMax() < count) {
						real.setMax(count);
						real.setUrl(tree.getMessageId());
					}
					real.setTotalCount(real.getTotalCount() + count);
					real.setTotalTime(real.getTotalTime() + time);
				}
				if (matrix.getUrl() == null) {
					matrix.setUrl(tree.getMessageId());
				}
			}
		}
	}

	private void processTransaction(MessageTree tree, Transaction t, Map<String, Ratio> ratios) {
		List<Message> children = t.getChildren();
		String type = t.getType();
		Ratio ratio = null;

		if (m_serverConfigManager.isRpcClient(type)) {
			ratio = ratios.get("Call");
		} else if (type.equals("SQL")) {
			ratio = ratios.get("SQL");
		} else if (type.startsWith("Cache.")) {
			ratio = ratios.get("Cache");
		}
		if (ratio != null) {
			ratio.incTotalCount();
			ratio.setTotalTime(ratio.getTotalTime() + t.getDurationInMicros());
		}

		for (Message child : children) {
			if (child instanceof Transaction) {
				processTransaction(tree, (Transaction) child, ratios);
			}
		}
	}

}
