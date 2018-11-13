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
package com.dianping.cat.consumer.event;

import org.apache.commons.lang.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.GraphTrendUtil;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.GraphTrend;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.consumer.event.model.transform.DefaultMerger;

public class EventReportMerger extends DefaultMerger {
	public EventReportMerger(EventReport eventReport) {
		super(eventReport);
	}

	@Override
	public void mergeMachine(Machine old, Machine machine) {
	}

	@Override
	public void mergeName(EventName old, EventName other) {
		old.setTotalCount(old.getTotalCount() + other.getTotalCount());
		old.setFailCount(old.getFailCount() + other.getFailCount());
		old.setTps(old.getTps() + other.getTps());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
		}

		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}

		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	@Override
	public void mergeRange(Range old, Range range) {
		old.setCount(old.getCount() + range.getCount());
		old.setFails(old.getFails() + range.getFails());
	}

	public Machine mergesForAllMachine(EventReport report) {
		Machine machine = new Machine(Constants.ALL);

		for (Machine m : report.getMachines().values()) {
			if (!m.getIp().equals(Constants.ALL)) {
				visitMachineChildren(machine, m);
			}
		}

		return machine;
	}

	@Override
	public void mergeType(EventType old, EventType other) {
		old.setTotalCount(old.getTotalCount() + other.getTotalCount());
		old.setFailCount(old.getFailCount() + other.getFailCount());
		old.setTps(old.getTps() + other.getTps());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
		}

		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}

		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	@Override
	public void visitEventReport(EventReport eventReport) {
		super.visitEventReport(eventReport);

		EventReport report = getEventReport();
		report.getIps().addAll(eventReport.getMachines().keySet());
	}

	@Override
	public void mergeGraphTrend(GraphTrend to, GraphTrend from) {
		String toCount = to.getCount();
		String fromCount = from.getCount();
		Integer[] count = mergeIntegerValue(toCount, fromCount);
		to.setCount(StringUtils.join(count, GraphTrendUtil.GRAPH_SPLITTER));

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
