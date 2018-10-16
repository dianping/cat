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
package com.dianping.cat.analysis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.message.io.DefaultMessageQueue;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;

public class Period {
	private static final int QUEUE_SIZE = 30000;

	private long m_startTime;

	private long m_endTime;

	private Map<String, List<PeriodTask>> m_tasks;

	@Inject
	private MessageAnalyzerManager m_analyzerManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;

	@Inject
	private Logger m_logger;

	public Period(long startTime, long endTime, MessageAnalyzerManager analyzerManager,
							ServerStatisticManager serverStateManager, Logger logger) {
		m_startTime = startTime;
		m_endTime = endTime;
		m_analyzerManager = analyzerManager;
		m_serverStateManager = serverStateManager;
		m_logger = logger;

		List<String> names = m_analyzerManager.getAnalyzerNames();

		m_tasks = new HashMap<String, List<PeriodTask>>();
		for (String name : names) {
			List<MessageAnalyzer> messageAnalyzers = m_analyzerManager.getAnalyzer(name, startTime);

			for (MessageAnalyzer analyzer : messageAnalyzers) {
				MessageQueue queue = new DefaultMessageQueue(QUEUE_SIZE);
				PeriodTask task = new PeriodTask(analyzer, queue, startTime);

				task.enableLogging(m_logger);

				List<PeriodTask> analyzerTasks = m_tasks.get(name);

				if (analyzerTasks == null) {
					analyzerTasks = new ArrayList<PeriodTask>();
					m_tasks.put(name, analyzerTasks);
				}
				analyzerTasks.add(task);
			}
		}
	}

	public void distribute(MessageTree tree) {
		m_serverStateManager.addMessageTotal(tree.getDomain(), 1);
		boolean success = true;
		String domain = tree.getDomain();

		for (Entry<String, List<PeriodTask>> entry : m_tasks.entrySet()) {
			List<PeriodTask> tasks = entry.getValue();
			int length = tasks.size();
			int index = 0;
			boolean manyTasks = length > 1;

			if (manyTasks) {
				index = Math.abs(domain.hashCode()) % length;
			}
			PeriodTask task = tasks.get(index);
			boolean enqueue = task.enqueue(tree);

			if (!enqueue) {
				if (manyTasks) {
					task = tasks.get((index + 1) % length);
					enqueue = task.enqueue(tree);

					if (!enqueue) {
						success = false;
					}
				} else {
					success = false;
				}
			}
		}

		if ((!success) && (!tree.isProcessLoss())) {
			m_serverStateManager.addMessageTotalLoss(tree.getDomain(), 1);

			tree.setProcessLoss(true);
		}
	}

	public void finish() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate = new Date(m_startTime);
		Date endDate = new Date(m_endTime - 1);

		m_logger.info(String
								.format("Finishing %s tasks in period [%s, %s]", m_tasks.size(), df.format(startDate),	df.format(endDate)));

		try {
			for (Entry<String, List<PeriodTask>> tasks : m_tasks.entrySet()) {
				for (PeriodTask task : tasks.getValue()) {
					task.finish();
				}
			}
		} catch (Throwable e) {
			Cat.logError(e);
		} finally {
			m_logger.info(String
									.format("Finished %s tasks in period [%s, %s]", m_tasks.size(), df.format(startDate),	df.format(endDate)));
		}
	}

	public List<MessageAnalyzer> getAnalyzer(String name) {
		List<MessageAnalyzer> analyzers = new ArrayList<MessageAnalyzer>();
		List<PeriodTask> tasks = m_tasks.get(name);

		if (tasks != null) {
			for (PeriodTask task : tasks) {
				analyzers.add(task.getAnalyzer());
			}
		}
		return analyzers;
	}

	public List<MessageAnalyzer> getAnalyzers() {
		List<MessageAnalyzer> analyzers = new ArrayList<MessageAnalyzer>(m_tasks.size());

		for (Entry<String, List<PeriodTask>> tasks : m_tasks.entrySet()) {
			for (PeriodTask task : tasks.getValue()) {
				analyzers.add(task.getAnalyzer());
			}
		}

		return analyzers;
	}

	public long getStartTime() {
		return m_startTime;
	}

	public boolean isIn(long timestamp) {
		return timestamp >= m_startTime && timestamp < m_endTime;
	}

	public void start() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		m_logger.info(String.format("Starting %s tasks in period [%s, %s]", m_tasks.size(),	df.format(new Date(m_startTime)),
								df.format(new Date(m_endTime - 1))));

		for (Entry<String, List<PeriodTask>> tasks : m_tasks.entrySet()) {
			List<PeriodTask> taskList = tasks.getValue();

			for (int i = 0; i < taskList.size(); i++) {
				PeriodTask task = taskList.get(i);

				task.setIndex(i);

				Threads.forGroup("Cat-RealtimeConsumer").start(task);
			}
		}
	}

}
