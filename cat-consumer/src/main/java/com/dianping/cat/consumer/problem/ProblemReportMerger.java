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

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.GraphTrendUtil;
import com.dianping.cat.consumer.problem.model.entity.*;
import com.dianping.cat.consumer.problem.model.transform.DefaultMerger;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Stack;

public class ProblemReportMerger extends DefaultMerger {
	private static final int SIZE = 60;

	public ProblemReportMerger(ProblemReport problemReport) {
		super(problemReport);
	}

	protected Entity findOrCreateEntity(Machine machine, Entity entity) {
		String id = entity.getId();
		String type = entity.getType();
		String status = entity.getStatus();
		Entity result = machine.findOrCreateEntity(id);

		result.setStatus(status).setType(type);
		return result;
	}

	protected Entry findOrCreateEntry(Machine machine, Entry entry) {
		String type = entry.getType();
		String status = entry.getStatus();

		for (Entry e : machine.getEntries()) {
			if (e.getType().equals(type) && e.getStatus().equals(status)) {
				return e;
			}
		}

		Entry result = new Entry();

		result.setStatus(status).setType(type);
		machine.addEntry(result);
		return result;
	}

	@Override
	protected void mergeDuration(Duration old, Duration duration) {
		List<String> oldMessages = old.getMessages();
		List<String> newMessages = duration.getMessages();

		old.setValue(duration.getValue());
		old.setCount(old.getCount() + duration.getCount());

		mergeList(oldMessages, newMessages, SIZE);
	}

	protected List<String> mergeList(List<String> oldMessages, List<String> newMessages, int size) {
		int originalSize = oldMessages.size();

		if (originalSize < size) {
			int remainingSize = size - originalSize;

			if (remainingSize >= newMessages.size()) {
				oldMessages.addAll(newMessages);
			} else {
				oldMessages.addAll(newMessages.subList(0, remainingSize));
			}
		}
		return oldMessages;
	}

	@Override
	protected void mergeSegment(Segment old, Segment segment) {
		List<String> oldMessages = old.getMessages();
		List<String> newMessages = segment.getMessages();

		old.setCount(old.getCount() + segment.getCount());
		mergeList(oldMessages, newMessages, SIZE);
	}

	@Override
	protected void visitMachineChildren(Machine to, Machine from) {
		Stack<Object> objs = getObjects();

		for (Entry source : from.getEntries()) {
			Entry target = findOrCreateEntry(to, source);

			objs.push(target);
			source.accept(this);
			objs.pop();
		}
		for (Entity source : from.getEntities().values()) {
			Entity target = findOrCreateEntity(to, source);

			objs.push(target);
			source.accept(this);
			objs.pop();
		}
	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		super.visitProblemReport(problemReport);

		getProblemReport().getIps().addAll(problemReport.getMachines().keySet());
	}

	@Override
	public void mergeGraphTrend(GraphTrend to, GraphTrend from) {
		String toFails = to.getFails();
		String fromFails = from.getFails();
		Integer[] fails = mergeIntegerValue(toFails, fromFails);
		to.setFails(StringUtils.join(fails, GraphTrendUtil.GRAPH_SPLITTER));
	}

	private Integer[] mergeIntegerValue(String to, String from) {
		Integer[] result = null;
		Integer[] source = null;

		if (StringUtils.isNotBlank(from)) {
			source = strToIntegerValue(from.split(GraphTrendUtil.GRAPH_SPLITTER));
		}

		if (StringUtils.isNotBlank(to)) {
			result = strToIntegerValue(to.split(GraphTrendUtil.GRAPH_SPLITTER));
		} else if (source != null) {
			result = new Integer[source.length];
			for (int i = 0; i < source.length; i++) {
				result[i] = 0;
			}
		}

		for (int i = 0; i < source.length; i++) {
			result[i] += source[i];
		}

		return result;
	}

	private Integer[] strToIntegerValue(String[] strs) {
		if (strs != null) {
			int size = strs.length;
			Integer[] result = new Integer[size];

			for (int i = 0; i < size; i++) {
				try {
					result[i] = Integer.parseInt(strs[i]);
				} catch (Exception e) {
					result[i] = 0;
					Cat.logError(e);
				}
			}
			return result;
		}

		return null;
	}

}
