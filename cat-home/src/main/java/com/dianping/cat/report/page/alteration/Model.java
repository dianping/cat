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
package com.dianping.cat.report.page.alteration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.alteration.Handler.AlterationMinute;

@ModelMeta("model")
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	private String m_insertResult;

	private Map<String, AlterationMinute> m_alterationMinuites;

	public Model(Context ctx) {
		super(ctx);
	}

	public Map<String, AlterationMinute> getAlterationMinuites() {
		return m_alterationMinuites;
	}

	public void setAlterationMinuites(Map<String, AlterationMinute> alterationMinuites) {
		m_alterationMinuites = alterationMinuites;
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public String getDomain() {
		return Constants.CAT;
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}

	public String getInsertResult() {
		return m_insertResult;
	}

	public void setInsertResult(String insertResult) {
		m_insertResult = insertResult;
	}

}
