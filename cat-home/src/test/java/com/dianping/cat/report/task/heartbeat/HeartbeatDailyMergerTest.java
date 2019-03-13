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
package com.dianping.cat.report.task.heartbeat;

import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.heartbeat.task.HeartbeatDailyMerger;

public class HeartbeatDailyMergerTest {

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Test
	public void test() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("heartbeat.xml"), "utf-8");
		HeartbeatReport report1 = DefaultSaxParser.parse(oldXml);
		report1.setStartTime(m_sdf.parse("2015-02-26 00:00:00"));
		HeartbeatReport report2 = DefaultSaxParser.parse(oldXml);
		report2.setStartTime(m_sdf.parse("2015-02-26 05:00:00"));
		String result = Files.forIO().readFrom(getClass().getResourceAsStream("dailyReport.xml"), "utf-8");

		HeartbeatDailyMerger merger = new HeartbeatDailyMerger(new HeartbeatReport("cat"),
								m_sdf.parse("2015-02-26 00:00:00").getTime());

		merger.visitHeartbeatReport(report1);
		merger.visitHeartbeatReport(report2);
		Assert.assertEquals("Check the merge result!", result.replace("\r", ""),
								merger.getHeartbeatReport().toString().replace("\r", ""));
	}

}
