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
package com.dianping.cat.report.page.problem.task;

import java.util.Stack;

import com.dianping.cat.consumer.problem.ProblemReportMerger;
import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;

public class HistoryProblemReportMerger extends ProblemReportMerger {

	public HistoryProblemReportMerger(ProblemReport problemReport) {
		super(problemReport);
	}

	@Override
	protected void visitMachineChildren(Machine to, Machine from) {
		Stack<Object> objs = getObjects();

		for (Entity source : from.getEntities().values()) {
			Entity target = findOrCreateEntity(to, source);

			objs.push(target);
			source.accept(this);
			objs.pop();
		}
	}

	@Override
	protected void visitEntityChildren(Entity to, Entity from) {
		Stack<Object> objs = getObjects();
		for (Duration source : from.getDurations().values()) {
			Duration target = to.findDuration(source.getValue());

			if (target == null) {
				target = new Duration(source.getValue());
				to.addDuration(target);
			}

			objs.push(target);
			source.accept(this);
			objs.pop();
		}
	}
}
