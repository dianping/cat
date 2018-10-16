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
package com.dianping.cat.report.page.business.graph;

import java.util.LinkedHashMap;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;
import com.dianping.cat.consumer.business.model.transform.BaseVisitor;
import com.dianping.cat.helper.MetricType;
import com.dianping.cat.report.page.business.task.BusinessKeyHelper;

public class BusinessDataFetcher {

	@Inject
	private BusinessKeyHelper m_keyHelper;

	public Map<String, double[]> buildGraphData(BusinessReport businessReport) {
		BusinessDataBuilder builder = new BusinessDataBuilder();

		builder.visitBusinessReport(businessReport);
		return builder.getDatas();
	}

	public class BusinessDataBuilder extends BaseVisitor {

		private Map<String, double[]> m_datas = new LinkedHashMap<String, double[]>();

		private String m_domain;

		@Override
		public void visitBusinessReport(BusinessReport report) {
			m_domain = report.getDomain();
			super.visitBusinessReport(report);
		}

		@Override
		public void visitBusinessItem(BusinessItem item) {
			String key = item.getId();

			double[] sum = new double[60];
			double[] count = new double[60];
			double[] avg = new double[60];

			for (Segment seg : item.getSegments().values()) {
				int index = seg.getId();

				sum[index] = seg.getSum();
				count[index] = seg.getCount();
				avg[index] = seg.getAvg();
			}

			m_datas.put(m_keyHelper.generateKey(key, m_domain, MetricType.SUM.getName()), sum);
			m_datas.put(m_keyHelper.generateKey(key, m_domain, MetricType.COUNT.getName()), count);
			m_datas.put(m_keyHelper.generateKey(key, m_domain, MetricType.AVG.getName()), avg);
		}

		public Map<String, double[]> getDatas() {
			return m_datas;
		}
	}

}
