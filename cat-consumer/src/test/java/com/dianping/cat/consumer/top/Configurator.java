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

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;

public class Configurator extends AbstractResourceConfigurator {

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new Configurator());
	}

	protected Class<?> getTestClass() {
		return TopAnalyzerTest.class;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		final String ID = TopAnalyzer.ID;

		all.add(C(ReportManager.class, ID, MockTopReportManager.class)//
								.req(ReportDelegate.class, ID).is(PER_LOOKUP));
		all.add(C(ReportDelegate.class, ID, ExtendedTopDelegate.class));

		return all;
	}

	public static class ExtendedTopDelegate extends TopDelegate {
	}

	public static class MockTopReportManager extends MockReportManager<TopReport> {
		private TopReport m_report;

		@Inject
		private ReportDelegate<TopReport> m_delegate;

		@Override
		public TopReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = (TopReport) m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}

			return m_report;
		}

		@Override
		public void destory() {
		}
	}
}
