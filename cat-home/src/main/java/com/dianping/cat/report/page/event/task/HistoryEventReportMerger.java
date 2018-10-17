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
package com.dianping.cat.report.page.event.task;

import com.dianping.cat.consumer.event.EventReportMerger;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;

public class HistoryEventReportMerger extends EventReportMerger {

	double m_duration = 1;

	public HistoryEventReportMerger(EventReport eventReport) {
		super(eventReport);
	}

	@Override
	public void mergeName(EventName old, EventName other) {
		old.getRanges().clear();
		other.getRanges().clear();
		super.mergeName(old, other);
		old.setTps(old.getTotalCount() / (m_duration * 24 * 3600));
	}

	@Override
	public void visitName(EventName name) {
		name.getRanges().clear();
		super.visitName(name);
	}

	@Override
	public void mergeType(EventType old, EventType other) {
		super.mergeType(old, other);
		old.setTps(old.getTotalCount() / (m_duration * 24 * 3600));
	}

	public HistoryEventReportMerger setDuration(double duration) {
		m_duration = duration;
		return this;
	}
}
