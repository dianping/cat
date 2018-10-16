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
package com.dianping.cat.report.service;

import java.util.Date;

import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportDao;

public class ReportDaoTest extends ComponentTestCase {

	@Test
	public void test() throws DalException {
		HourlyReportDao dao = lookup(HourlyReportDao.class);
		HourlyReport proto = new HourlyReport();

		proto.setCreationDate(new Date());
		proto.setDomain("domain");
		proto.setEndDate(new Date());
		proto.setName("name");
		proto.setIp("ip");
		proto.setPeriod(new Date());
		proto.setType(1);
		dao.insert(proto);
	}

	@Test
	public void testDaily() throws DalException {
		DailyReportDao dao = lookup(DailyReportDao.class);
		DailyReport proto = new DailyReport();

		proto.setCreationDate(new Date());
		proto.setDomain("domain");
		proto.setEndDate(new Date());
		proto.setName("name");
		proto.setIp("ip");
		proto.setPeriod(new Date());
		proto.setType(1);
		dao.insert(proto);
	}

	@Test
	public void testWeek() throws DalException {
		WeeklyReportDao dao = lookup(WeeklyReportDao.class);
		WeeklyReport proto = new WeeklyReport();

		proto.setCreationDate(new Date());
		proto.setDomain("domain");
		proto.setName("name");
		proto.setIp("ip");
		proto.setPeriod(new Date());
		proto.setType(1);
		dao.insert(proto);
	}

	@Test
	public void testMonth() throws DalException {
		MonthlyReportDao dao = lookup(MonthlyReportDao.class);
		MonthlyReport proto = new MonthlyReport();

		proto.setCreationDate(new Date());
		proto.setDomain("domain");
		proto.setName("name");
		proto.setIp("ip");
		proto.setPeriod(new Date());
		proto.setType(1);
		dao.insert(proto);
	}

}
