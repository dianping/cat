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
package com.dianping.cat.report.page.transaction;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case HOURLY_REPORT:
			return JspFile.HOURLY_REPORT.getPath();
		case GRAPHS:
			return JspFile.GRAPHS.getPath();
		case HISTORY_REPORT:
			return JspFile.HISTORY_REPORT.getPath();
		case HISTORY_GRAPH:
			return JspFile.HISTORY_GRAPH.getPath();
		case GROUP_GRAPHS:
			return JspFile.GROUP_GRAPHS.getPath();
		case HISTORY_GROUP_GRAPH:
			return JspFile.HISTORY_GROUP_GRAPH.getPath();
		case HISTORY_GROUP_REPORT:
			return JspFile.HISTORY_GROUP_REPORT.getPath();
		case HOURLY_GROUP_REPORT:
			return JspFile.HOURLY_GROUP_REPORT.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
