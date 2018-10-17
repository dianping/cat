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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

public class DefaultProblemHandler extends ProblemHandler {
	public static final String ID = "default-problem";

	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private Set<String> m_errorTypes;

	@Override
	public void handle(Machine machine, MessageTree tree) {
		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			processTransaction(machine, (Transaction) message, tree);
		} else if (message instanceof Event) {
			processEvent(machine, (Event) message, tree);
		} else if (message instanceof Heartbeat) {
			processHeartbeat(machine, (Heartbeat) message, tree);
		}
	}

	private void processEvent(Machine machine, Event message, MessageTree tree) {
		if (!message.getStatus().equals(Message.SUCCESS) && m_errorTypes.contains(message.getType())) {
			String type = ProblemType.ERROR.getName();
			String status = message.getName();

			Entity entity = findOrCreateEntity(machine, type, status);
			updateEntity(tree, entity, 0);
		}
	}

	private void processHeartbeat(Machine machine, Heartbeat heartbeat, MessageTree tree) {
		String type = heartbeat.getType().toLowerCase();

		if ("heartbeat".equals(type)) {
			String status = heartbeat.getName();
			Entity entity = findOrCreateEntity(machine, type, status);

			updateEntity(tree, entity, 0);
		}
	}

	private void processTransaction(Machine machine, Transaction transaction, MessageTree tree) {
		String transactionStatus = transaction.getStatus();

		if (!transactionStatus.equals(Transaction.SUCCESS)) {
			String type = transaction.getType();
			String name = transaction.getName();
			Entity entity = findOrCreateEntity(machine, type, name);

			updateEntity(tree, entity, 0);
		}

		List<Message> children = transaction.getChildren();

		for (Message message : children) {
			if (message instanceof Transaction) {
				processTransaction(machine, (Transaction) message, tree);
			} else if (message instanceof Event) {
				processEvent(machine, (Event) message, tree);
			} else if (message instanceof Heartbeat) {
				processHeartbeat(machine, (Heartbeat) message, tree);
			}
		}
	}

	public void setErrorType(String type) {
		m_errorTypes = new HashSet<String>(Splitters.by(',').noEmptyItem().split(type));
	}

}