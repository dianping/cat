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
package com.dianping.cat.report.page.model;

import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.report.ReportPage;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private Throwable m_exception;

	private Object m_model;

	private String m_modelInXml;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.XML;
	}

	public Throwable getException() {
		return m_exception;
	}

	public void setException(Throwable exception) {
		m_exception = exception;
	}

	public Object getModel() {
		return m_model;
	}

	public void setModel(Object model) {
		m_model = model;
	}

	public String getModelInXml() {
		return m_modelInXml;
	}

	public void setModelInXml(String modelInXml) {
		m_modelInXml = modelInXml;
	}
}
