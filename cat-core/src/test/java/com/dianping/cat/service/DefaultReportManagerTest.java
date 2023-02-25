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
package com.dianping.cat.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unidal.dal.jdbc.DalException;

import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.report.DefaultReportManager;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.DomainValidator;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.ReportDelegate;

public class DefaultReportManagerTest {

	private static final String MOCK = "MockReportDeletegate";

	private static final String DOMAIN1 = "domain1";

	private static final String DOMAIN2 = "domain2";

	private static final String DOMAIN3 = "domain3";

	private static int m_bucketInsertCount;

	private DefaultReportManager<String> m_manager = new DefaultReportManager<String>();

	private MockReportContentDao m_reportContentDao = new MockReportContentDao();

	private MockHourlyReportDao m_hourlyReportDao = new MockHourlyReportDao();

	private MockBucketManager m_bucketManager = new MockBucketManager();

	private long m_start;

	@Before
	public void setUp() {
		m_manager.setBucketManager(m_bucketManager);
		m_manager.setName("manager");
		m_manager.setReportContentDao(m_reportContentDao);
		m_manager.setReportDao(m_hourlyReportDao);
		m_manager.setReportDelegate(new MockReportDeletegate());
		m_manager.setValidator(new DomainValidator());

		long time = System.currentTimeMillis();
		m_start = time - time % (3600 * 1000L);
	}

	@Test
	public void testClean() {
		long hour = 3600 * 1000L;
		m_manager.getHourlyReport(m_start - 3 * hour, DOMAIN1, true);
		m_manager.getHourlyReport(m_start - 4 * hour, DOMAIN2, true);
		m_manager.getHourlyReport(m_start, DOMAIN3, true);
		m_manager.cleanup(m_start);

		Map<String, String> reports = m_manager.getHourlyReports(m_start);
		Assert.assertEquals(0, reports.size());
	}

	@Test
	public void testCreateReport() {
		Assert.assertEquals(0, m_manager.getDomains(m_start).size());

		String report1 = m_manager.getHourlyReport(m_start, DOMAIN1, true);
		String report2 = m_manager.getHourlyReport(m_start, DOMAIN2, false);

		Assert.assertEquals(MOCK, report1);
		Assert.assertEquals(MOCK, report2);
		Set<String> domains = m_manager.getDomains(m_start);
		Assert.assertEquals("[domain1]", domains.toString());

		m_manager.getHourlyReport(m_start, DOMAIN3, true);
		domains = m_manager.getDomains(m_start);
		Assert.assertEquals("[domain3, domain1]", domains.toString());

		Map<String, String> reports = m_manager.getHourlyReports(m_start);
		Assert.assertEquals(2, reports.size());

		for (Entry<String, String> entry : reports.entrySet()) {
			Assert.assertEquals(MOCK, entry.getValue());
		}
	}

	@Test
	public void testLoadReport() {
		m_manager.loadHourlyReports(m_start, null, 0);

		Map<String, String> reports = m_manager.getHourlyReports(m_start);
		Assert.assertEquals(3, reports.size());

		for (Entry<String, String> entry : reports.entrySet()) {
			Assert.assertEquals(MOCK, entry.getValue());
		}
	}

	@Test
	public void testStoreReport() {
		m_manager.getHourlyReport(m_start, DOMAIN1, true);
		m_manager.getHourlyReport(m_start, DOMAIN2, true);
		m_manager.getHourlyReport(m_start, DOMAIN3, true);
		m_manager.storeHourlyReports(m_start, StoragePolicy.FILE_AND_DB, 0);

		Assert.assertEquals(3, m_reportContentDao.count);
		Assert.assertEquals(3, m_hourlyReportDao.count);
		Assert.assertEquals(3, m_bucketInsertCount);
	}

	public class MockBucketManager implements ReportBucketManager {

		@Override
		public void closeBucket(ReportBucket bucket) {
		}

		@Override
		public ReportBucket getReportBucket(long timestamp, String name, int index) throws IOException {
			return new MockStringBucket();
		}

		@Override
		public void clearOldReports() {
		}

	}

	public class MockHourlyReportDao extends HourlyReportDao {

		public int count;

		@Override
		public int insert(HourlyReport proto) throws DalException {
			return count++;
		}

	}

	public class MockReportContentDao extends HourlyReportContentDao {

		public int count;

		@Override
		public int insert(HourlyReportContent proto) throws DalException {
			return count++;
		}

	}

	public class MockReportDeletegate implements ReportDelegate<String> {

		@Override
		public void afterLoad(Map<String, String> reports) {
		}

		@Override
		public void beforeSave(Map<String, String> reports) {
		}

		@Override
		public byte[] buildBinary(String report) {
			return MOCK.getBytes();
		}

		@Override
		public String buildXml(String report) {
			return MOCK;
		}

		@Override
		public boolean createHourlyTask(String report) {
			return false;
		}

		@Override
		public String getDomain(String report) {
			return MOCK;
		}

		@Override
		public String makeReport(String domain, long startTime, long duration) {
			return MOCK;
		}

		@Override
		public String mergeReport(String old, String other) {
			return MOCK;
		}

		@Override
		public String parseBinary(byte[] bytes) {
			return MOCK;
		}

		@Override
		public String parseXml(String xml) throws Exception {
			return MOCK;
		}

	}

	public class MockStringBucket implements ReportBucket {

		@Override
		public void close() throws IOException {
		}

		@Override
		public String findById(String id) throws IOException {
			return MOCK;
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public Collection<String> getIds() {
			List<String> list = new ArrayList<String>();

			list.add(DOMAIN1);
			list.add(DOMAIN2);
			list.add(DOMAIN3);
			return list;
		}

		@Override
		public void initialize(String name, Date timestamp, int index) throws IOException {
		}

		@Override
		public boolean storeById(String id, String data) throws IOException {
			m_bucketInsertCount++;
			return true;
		}
	}
}
