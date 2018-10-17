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

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;

public class EventTpsStatisticsComputer extends BaseVisitor {

	public double m_duration = 3600;

	public EventTpsStatisticsComputer setDuration(double duration) {
		m_duration = duration;
		return this;
	}

	@Override
	public void visitName(EventName name) {
		if (m_duration > 0) {
			name.setTps(name.getTotalCount() * 1.0 / m_duration);
		}
	}

	@Override
	public void visitType(EventType type) {
		if (m_duration > 0) {
			type.setTps(type.getTotalCount() * 1.0 / m_duration);
			super.visitType(type);
		}
	}
}
