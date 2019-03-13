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
package com.dianping.cat.report.page.cross;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case HOURLY_PROJECT:
			return JspFile.HOURLY_PROJECT.getPath();
		case HOURLY_HOST:
			return JspFile.HOURLY_HOST.getPath();
		case HOURLY_METHOD:
			return JspFile.HOURLY_METHOD.getPath();
		case HISTORY_HOST:
			return JspFile.HISTORY_HOST.getPath();
		case HISTORY_METHOD:
			return JspFile.HISTORY_METHOD.getPath();
		case HISTORY_PROJECT:
			return JspFile.HISTORY_PROJECT.getPath();
		case METHOD_QUERY:
			return JspFile.METHOD_QUERY.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
