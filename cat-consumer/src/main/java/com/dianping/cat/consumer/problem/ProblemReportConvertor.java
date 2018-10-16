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

import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;

public class ProblemReportConvertor extends BaseVisitor {

	@Override
	public void visitMachine(Machine machine) {
		Map<String, Entity> entities = machine.getEntities();
		List<Entry> entries = machine.getEntries();

		if (entities.isEmpty() && !entries.isEmpty()) {
			for (Entry entry : entries) {
				String type = entry.getType();
				String status = entry.getStatus();
				String id = type + ":" + status;
				Entity entity = machine.findOrCreateEntity(id);

				entity.setType(type).setStatus(status);
				for (Duration duration : entry.getDurations().values()) {
					entity.addDuration(duration);
				}

				for (JavaThread thread : entry.getThreads().values()) {
					entity.addThread(thread);
				}
			}
			entries.clear();
		}
	}
}
