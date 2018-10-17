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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

public class LongExecutionProblemHandler extends ProblemHandler implements Initializable {
	public static final String ID = "long-execution";

	@Inject
	private ServerConfigManager m_configManager;

	private int[] m_defaultLongServiceDuration = { 50, 100, 500, 1000, 3000, 5000 };

	private int[] m_defaultLongSqlDuration = { 100, 500, 1000, 3000, 5000 };

	private int[] m_defaultLongUrlDuration = { 1000, 2000, 3000, 5000 };

	private int[] m_defalutLongCallDuration = { 100, 500, 1000, 3000, 5000 };

	private int[] m_defaultLongCacheDuration = { 10, 50, 100, 500 };

	private Map<String, Integer> m_longServiceThresholds = new HashMap<String, Integer>();

	private Map<String, Integer> m_longSqlThresholds = new HashMap<String, Integer>();

	private Map<String, Integer> m_longUrlThresholds = new HashMap<String, Integer>();

	private Map<String, Integer> m_longCallThresholds = new HashMap<String, Integer>();

	private Map<String, Integer> m_longCacheThresholds = new HashMap<String, Integer>();

	public int computeLongDuration(long duration, String domain, int[] defaultLongDuration,
							Map<String, Integer> longThresholds) {
		int[] messageDuration = defaultLongDuration;

		for (int i = messageDuration.length - 1; i >= 0; i--) {
			if (duration >= messageDuration[i]) {
				return messageDuration[i];
			}
		}

		Integer value = longThresholds.get(domain);

		if (value != null && duration >= value) {
			return value;
		} else {
			return -1;
		}
	}

	@Override
	public void handle(Machine machine, MessageTree tree) {
		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;

			processTransaction(machine, transaction, tree);
		}
	}

	@Override
	public void initialize() throws InitializationException {
		Map<String, Domain> domains = m_configManager.getLongConfigDomains();

		for (Domain domain : domains.values()) {
			Integer serviceThreshold = domain.getServiceThreshold();
			Integer urlThreshold = domain.getUrlThreshold();
			Integer sqlThreshold = domain.getSqlThreshold();

			if (serviceThreshold != null) {
				m_longServiceThresholds.put(domain.getName(), serviceThreshold);
			}
			if (urlThreshold != null) {
				m_longUrlThresholds.put(domain.getName(), urlThreshold);
			}
			if (sqlThreshold != null) {
				m_longSqlThresholds.put(domain.getName(), sqlThreshold);
			}
		}
	}

	private void processLongCache(Machine machine, Transaction transaction, MessageTree tree) {
		long duration = ((Transaction) transaction).getDurationInMillis();
		long nomarizeDuration = computeLongDuration(duration, tree.getDomain(), m_defaultLongCacheDuration,
								m_longCacheThresholds);

		if (nomarizeDuration > 0) {
			String type = ProblemType.LONG_CACHE.getName();
			String status = transaction.getName();
			Entity entity = findOrCreateEntity(machine, type, status);

			updateEntity(tree, entity, (int) nomarizeDuration);
		}
	}

	private void processLongCall(Machine machine, Transaction transaction, MessageTree tree) {
		long duration = transaction.getDurationInMillis();
		String domain = tree.getDomain();

		long nomarizeDuration = computeLongDuration(duration, domain, m_defalutLongCallDuration, m_longCallThresholds);
		if (nomarizeDuration > 0) {
			String type = ProblemType.LONG_CALL.getName();
			String status = transaction.getName();
			Entity entity = findOrCreateEntity(machine, type, status);

			updateEntity(tree, entity, (int) nomarizeDuration);
		}
	}

	private void processLongService(Machine machine, Transaction transaction, MessageTree tree) {
		long duration = transaction.getDurationInMillis();
		String domain = tree.getDomain();
		long nomarizeDuration = computeLongDuration(duration, domain, m_defaultLongServiceDuration,	m_longServiceThresholds);

		if (nomarizeDuration > 0) {
			String type = ProblemType.LONG_SERVICE.getName();
			String status = transaction.getName();
			Entity entity = findOrCreateEntity(machine, type, status);

			updateEntity(tree, entity, (int) nomarizeDuration);
		}
	}

	private void processLongSql(Machine machine, Transaction transaction, MessageTree tree) {
		long duration = transaction.getDurationInMillis();
		String domain = tree.getDomain();

		long nomarizeDuration = computeLongDuration(duration, domain, m_defaultLongSqlDuration, m_longSqlThresholds);
		if (nomarizeDuration > 0) {
			String type = ProblemType.LONG_SQL.getName();
			String status = transaction.getName();
			Entity problem = findOrCreateEntity(machine, type, status);

			updateEntity(tree, problem, (int) nomarizeDuration);
		}
	}

	private void processLongUrl(Machine machine, Transaction transaction, MessageTree tree) {
		long duration = (transaction).getDurationInMillis();
		String domain = tree.getDomain();

		long nomarizeDuration = computeLongDuration(duration, domain, m_defaultLongUrlDuration, m_longUrlThresholds);
		if (nomarizeDuration > 0) {
			String type = ProblemType.LONG_URL.getName();
			String status = transaction.getName();
			Entity problem = findOrCreateEntity(machine, type, status);

			updateEntity(tree, problem, (int) nomarizeDuration);
		}
	}

	private void processTransaction(Machine machine, Transaction transaction, MessageTree tree) {
		String type = transaction.getType();

		if (type.startsWith("Cache.")) {
			processLongCache(machine, transaction, tree);
		} else if (type.equals("SQL")) {
			processLongSql(machine, transaction, tree);
		} else if (m_configManager.isRpcClient(type)) {
			processLongCall(machine, transaction, tree);
		} else if (m_configManager.isRpcServer(type)) {
			processLongService(machine, transaction, tree);
		} else if ("URL".equals(type)) {
			processLongUrl(machine, transaction, tree);
		}

		List<Message> messageList = transaction.getChildren();

		for (Message message : messageList) {
			if (message instanceof Transaction) {
				processTransaction(machine, (Transaction) message, tree);
			}
		}
	}

}
