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
package com.dianping.cat.consumer.top;

import com.dianping.cat.consumer.top.model.entity.Domain;
import com.dianping.cat.consumer.top.model.entity.Error;
import com.dianping.cat.consumer.top.model.entity.Machine;
import com.dianping.cat.consumer.top.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.DefaultMerger;

public class TopReportMerger extends DefaultMerger {

	public TopReportMerger(TopReport topReport) {
		super(topReport);
	}

	@Override
	protected void mergeDomain(Domain old, Domain domain) {
		super.mergeDomain(old, domain);
	}

	@Override
	protected void mergeError(Error old, Error error) {
		old.setCount(old.getCount() + error.getCount());
	}

	@Override
	protected void mergeMachine(Machine to, Machine from) {
		to.setCount(to.getCount() + from.getCount());
	}

	@Override
	protected void mergeSegment(Segment old, Segment segment) {
		old.setError(old.getError() + segment.getError());
	}

	@Override
	protected void mergeTopReport(TopReport old, TopReport topReport) {
		super.mergeTopReport(old, topReport);
	}

}
