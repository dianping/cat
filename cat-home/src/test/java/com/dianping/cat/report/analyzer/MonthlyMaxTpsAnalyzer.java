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
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;

public class MonthlyMaxTpsAnalyzer extends ComponentTestCase {

	private String m_start = "2013-08-23 16:00";

	private String m_end = "2013-11-01 00:00";

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	@Inject
	private TransactionReportService m_reportService;

	private Map<String, DomainInfo> m_infos = new LinkedHashMap<String, DomainInfo>();

	private DomainInfo findOrCreate(String domain) {
		DomainInfo info = m_infos.get(domain);

		if (info == null) {
			info = new DomainInfo();
			m_infos.put(domain, info);
		}
		return info;
	}

	@Test
	public void test() throws Exception {
		m_reportService = lookup(TransactionReportService.class);

		long start = m_sdf.parse(m_start).getTime();
		long end = m_sdf.parse(m_end).getTime();

		for (; start < end; start = start + TimeHelper.ONE_DAY) {
			Date current = new Date(start);
			Date next = new Date(current.getTime() + TimeHelper.ONE_HOUR);
			Set<String> domains = queryDomains(current);
			System.out.println("Process " + m_sdf.format(current));

			for (String domain : domains) {
				TransactionReport report = m_reportService.queryReport(domain, current, next);
				ReportVisitor visitor = new ReportVisitor();

				visitor.visitTransactionReport(report);
				Index index = visitor.getIndex();
				findOrCreate(domain).add(getMonthStart(start), index);
			}
		}
		printCount();
		printMachine();
	}

	private long getMonthStart(long start) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(start);

		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);

		return cal.getTimeInMillis();
	}

	private void printCount() throws Exception {
		Set<String> domains = m_infos.keySet();
		String month1 = "2013-08-01 00:00";
		String month2 = "2013-09-01 00:00";
		String month3 = "2013-10-01 00:00";
		StringBuilder sb = new StringBuilder();

		sb.append("domain").append("\t").append(month1).append("\t").append(month2).append("\t").append(month3)
								.append("\t\n");
		for (String domain : domains) {
			sb.append(domain).append("\t");
			printDomain(sb, m_sdf.parse(month1).getTime(), domain);
			printDomain(sb, m_sdf.parse(month2).getTime(), domain);
			printDomain(sb, m_sdf.parse(month3).getTime(), domain);
			sb.append("\n");
		}

		System.out.println(sb.toString());
	}

	private void printMachine() throws Exception {
		Set<String> domains = m_infos.keySet();
		String month1 = "2013-08-01 00:00";
		String month2 = "2013-09-01 00:00";
		String month3 = "2013-10-01 00:00";
		StringBuilder sb = new StringBuilder();

		sb.append("domain").append("\t").append(month1).append("\t").append(month2).append("\t").append(month3)
								.append("\t\n");
		for (String domain : domains) {
			sb.append(domain).append("\t");
			printMachine(sb, m_sdf.parse(month1).getTime(), domain);
			printMachine(sb, m_sdf.parse(month2).getTime(), domain);
			printMachine(sb, m_sdf.parse(month3).getTime(), domain);
			sb.append("\n");
		}
		System.out.println(sb.toString());
	}

	private void printDomain(StringBuilder sb, long start, String domain) {
		DomainInfo info = m_infos.get(domain);
		Index index = info.getIndexs().get(start);
		if (index != null) {
			sb.append(index.getCount()).append("\t");
		} else {
			sb.append(0).append("\t");
		}
	}

	private void printMachine(StringBuilder sb, long start, String domain) {
		DomainInfo info = m_infos.get(domain);
		Index index = info.getIndexs().get(start);
		if (index != null) {
			sb.append(index.getMachineNumber()).append("\t");
		} else {
			sb.append(0).append("\t");
		}
	}

	private Set<String> queryDomains(Date date) {
		return m_reportService
								.queryAllDomainNames(date, new Date(date.getTime() + TimeHelper.ONE_HOUR),	TransactionAnalyzer.ID);
	}

	public static class ReportVisitor extends BaseVisitor {

		private int m_machineNumber;

		private long m_count;

		@Override
		public void visitMachine(Machine machine) {
			m_machineNumber++;
			super.visitMachine(machine);
		}

		@Override
		public void visitType(TransactionType type) {
			String id = type.getId();

			if ("URL".equals(id) || "Service".equals(id) || "PigeonService".equals(id)) {
				m_count = m_count + type.getTotalCount();
			}
		}

		public Index getIndex() {
			Index index = new Index();

			index.setCount(m_count);
			index.setMachineNumber(m_machineNumber);
			return index;
		}

		public int getMachineNumber() {
			return m_machineNumber;
		}

		public long getCount() {
			return m_count;
		}
	}

	public static class DomainInfo {

		private Map<Long, Index> m_indexs = new LinkedHashMap<Long, Index>();

		public Map<Long, Index> getIndexs() {
			return m_indexs;
		}

		public void setIndexs(Map<Long, Index> indexs) {
			m_indexs = indexs;
		}

		public void add(long time, Index index) {
			Index temp = m_indexs.get(time);

			if (temp == null) {
				m_indexs.put(time, index);
			} else {
				if (index.getMachineNumber() > temp.getMachineNumber()) {
					temp.setMachineNumber(index.getMachineNumber());
				}
				if (index.getCount() > temp.getCount()) {
					temp.setCount(index.getCount());
				}
			}
		}
	}

	public static class Index {

		private int m_machineNumber;

		private long m_count;

		public int getMachineNumber() {
			return m_machineNumber;
		}

		public void setMachineNumber(int machineNumber) {
			m_machineNumber = machineNumber;
		}

		public long getCount() {
			return m_count;
		}

		public void setCount(long count) {
			m_count = count;
		}
	}
}
