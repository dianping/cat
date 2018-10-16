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
package com.dianping.cat.report.page.statistics.task.heavy;

import com.dianping.cat.consumer.matrix.model.entity.Matrix;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.entity.Ratio;
import com.dianping.cat.consumer.matrix.model.transform.BaseVisitor;
import com.dianping.cat.home.heavy.entity.HeavyCache;
import com.dianping.cat.home.heavy.entity.HeavyCall;
import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.heavy.entity.HeavySql;
import com.dianping.cat.home.heavy.entity.Service;
import com.dianping.cat.home.heavy.entity.Url;

public class MatrixReportVisitor extends BaseVisitor {

	private HeavyReport m_report;

	private String m_currentDomain;

	private String m_currentType;

	private String m_currentName;

	public MatrixReportVisitor setReport(HeavyReport report) {
		m_report = report;
		return this;
	}

	private void updateService(Service service, String logview, long max) {
		service.setDomain(m_currentDomain);
		service.setName(m_currentName);
		if (max > service.getCount()) {
			service.setLogview(logview);
			service.setCount(max);
		}
	}

	private void updateUrl(Url url, String logview, long max) {
		url.setDomain(m_currentDomain);
		url.setName(m_currentName);
		if (max > url.getCount()) {
			url.setLogview(logview);
			url.setCount(max);
		}
	}

	@Override
	public void visitMatrix(Matrix matrix) {
		m_currentType = matrix.getType();
		m_currentName = matrix.getName();
		super.visitMatrix(matrix);
	}

	@Override
	public void visitMatrixReport(MatrixReport matrixReport) {
		m_currentDomain = matrixReport.getDomain();
		if (m_report.getHeavyCache() == null) {
			m_report.setHeavyCache(new HeavyCache());
		}
		if (m_report.getHeavyCall() == null) {
			m_report.setHeavyCall(new HeavyCall());
		}
		if (m_report.getHeavySql() == null) {
			m_report.setHeavySql(new HeavySql());
		}
		super.visitMatrixReport(matrixReport);
	}

	@Override
	public void visitRatio(Ratio ratio) {
		String type = ratio.getType();
		long max = ratio.getMax();
		String logview = ratio.getUrl();

		if ("Call".equals(type)) {
			HeavyCall call = m_report.getHeavyCall();
			String key = m_currentDomain + ":" + m_currentName;
			if (m_currentType.equals("URL")) {
				if (max > 10) {
					Url url = call.findOrCreateUrl(key);
					updateUrl(url, logview, max);
				}
			} else {
				if (max > 10) {
					Service service = call.findOrCreateService(key);
					updateService(service, logview, max);
				}
			}
		} else if ("SQL".equals(type)) {
			HeavySql sql = m_report.getHeavySql();
			String key = m_currentDomain + ":" + m_currentName;
			if (m_currentType.equals("URL")) {
				if (max > 20) {
					Url url = sql.findOrCreateUrl(key);
					updateUrl(url, logview, max);
				}
			} else {
				if (max > 20) {
					Service service = sql.findOrCreateService(key);
					updateService(service, logview, max);
				}
			}
		} else if ("Cache".equals(type)) {
			HeavyCache cache = m_report.getHeavyCache();
			String key = m_currentDomain + ":" + m_currentName;
			if (m_currentType.equals("URL")) {
				if (max > 100) {
					Url url = cache.findOrCreateUrl(key);
					updateUrl(url, logview, max);
				}
			} else {
				if (max > 100) {
					Service service = cache.findOrCreateService(key);
					updateService(service, logview, max);
				}
			}
		}
	}

}
