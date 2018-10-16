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
package com.dianping.cat.report.page.alert;

import java.util.Date;
import java.util.Map;

import org.unidal.web.mvc.ViewModel;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.alert.Handler.AlertMinute;

@ModelMeta("model")
public class Model extends ViewModel<ReportPage, Action, Context> {

	private String m_alertResult;

	private Map<String, AlertMinute> m_alertMinutes;

	public Model(Context ctx) {
		super(ctx);
	}

	public Map<String, AlertMinute> getAlertMinutes() {
		return m_alertMinutes;
	}

	public void setAlertMinutes(Map<String, AlertMinute> alertMinutes) {
		m_alertMinutes = alertMinutes;
	}

	public String getAlertResult() {
		return m_alertResult;
	}

	public void setAlertResult(String alertResult) {
		m_alertResult = alertResult;
	}

	public Date getDate() {
		return new Date();
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public String getDomain() {
		return Constants.CAT;
	}

	public String getIpAddress() {
		return null;
	}

}
