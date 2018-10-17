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

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.unidal.web.mvc.view.Viewer;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.report.ReportPage;

public class XmlViewer implements Viewer<ReportPage, Action, Context, Model> {
	@Override
	public void view(Context ctx, Model model) throws ServletException, IOException {
		TransactionReport report = model.getReport();
		HttpServletResponse res = ctx.getHttpServletResponse();

		if (report != null) {
			ServletOutputStream out = res.getOutputStream();

			res.setContentType("text/xml");
			out.print(report.toString());
		} else {
			res.sendError(404, "Not found!");
		}
	}
}
