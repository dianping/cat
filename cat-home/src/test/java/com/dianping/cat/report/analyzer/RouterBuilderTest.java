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
package com.dianping.cat.report.analyzer;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.Task;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.report.task.ReportFacade;
import com.dianping.cat.system.page.router.service.RouterConfigService;

public class RouterBuilderTest extends ComponentTestCase {

	public String day1 = "2014-11-10";

	public String day2 = "2014-11-09";

	public String day3 = "2014-11-08";

	public String day4 = "2014-11-07";

	@Test
	public void test() throws Exception {
		ReportFacade reportFacade = (ReportFacade) lookup(ReportFacade.class);
		Task task = new Task();
		Date reportPeriod = new SimpleDateFormat("yyyy-MM-dd").parse(day3);

		task.setReportName(Constants.REPORT_ROUTER);
		task.setReportPeriod(reportPeriod);
		task.setReportDomain(Constants.CAT);
		task.setTaskType(1);
		reportFacade.builderReport(task);

		task.setReportPeriod(new SimpleDateFormat("yyyy-MM-dd").parse(day4));
		reportFacade.builderReport(task);
	}

	@Test
	public void test1() throws Exception {
		RouterConfigService service = lookup(RouterConfigService.class);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		RouterConfig report1 = service.queryReport(Constants.CAT, sdf.parse(day3), sdf.parse(day4));

		RouterConfig report2 = service.queryReport(Constants.CAT, sdf.parse(day3), sdf.parse(day4));

		Assert.assertEquals(report1.toString(), report2.toString());
	}

}
