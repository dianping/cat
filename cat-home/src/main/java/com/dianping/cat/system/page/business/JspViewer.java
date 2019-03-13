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
package com.dianping.cat.system.page.business;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.system.SystemPage;

public class JspViewer extends BaseJspViewer<SystemPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case DELETE:
		case CustomDelete:
		case LIST:
		case AlertRuleAddSubmit:
		case AddSubmit:
		case CustomAddSubmit:
			return JspFile.VIEW.getPath();
		case ADD:
			return JspFile.ADD.getPath();
		case AlertRuleAdd:
			return JspFile.AlertAdd.getPath();
		case TagConfig:
			return JspFile.TAG.getPath();
		case CustomAdd:
			return JspFile.CustomAdd.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
