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
package com.dianping.cat.report.page.statistics.task.jar;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.transform.BaseVisitor;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.jar.entity.Domain;
import com.dianping.cat.home.jar.entity.Jar;
import com.dianping.cat.home.jar.entity.JarReport;
import com.dianping.cat.home.jar.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.heartbeat.service.HeartbeatReportService;
import com.dianping.cat.report.page.statistics.service.JarReportService;
import com.dianping.cat.report.task.TaskBuilder;

@Named(type = TaskBuilder.class, value = JarReportBuilder.ID)
public class JarReportBuilder implements TaskBuilder {

	public static final String ID = Constants.REPORT_JAR;

	public static List<String> s_jars = Arrays
							.asList("cat-client", "cat-core", "dpsf-net", "lion-client",	"avatar-cache", "zebra-ds-monitor-client",
													"zebra-api", "swallow-client", "swallow-consumerclient",	"swallow-producerclient", "platform-sdk",
													"squirrel-client");

	@Inject
	private JarReportService m_reportService;

	@Inject
	private HeartbeatReportService m_heartbeatReportService;

	@Inject
	private ServerFilterConfigManager m_configManager;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		throw new RuntimeException(ID + " don't support daily update");
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		Date end = new Date(period.getTime() + TimeHelper.ONE_HOUR);
		Set<String> domains = m_reportService.queryAllDomainNames(period, end, HeartbeatAnalyzer.ID);
		JarReport jarReport = new JarReport();
		HeartbeatReportVisitor visitor = new HeartbeatReportVisitor(jarReport);

		for (String domainName : domains) {
			if (m_configManager.validateDomain(domainName)) {
				HeartbeatReport heartbeatReport = m_heartbeatReportService.queryReport(domainName, period, end);

				visitor.visitHeartbeatReport(heartbeatReport);
			}
		}
		jarReport.setStartTime(period);
		jarReport.setEndTime(end);

		HourlyReport report = new HourlyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(jarReport);
		return m_reportService.insertHourlyReport(report, binaryContent);
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException(ID + " don't support monthly update");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException(ID + " don't support weekly update");
	}

	public class HeartbeatReportVisitor extends BaseVisitor {

		private String m_currentDomain;

		private JarReport m_jarReport;

		public HeartbeatReportVisitor(JarReport jarReport) {
			m_jarReport = jarReport;
		}

		@Override
		public void visitHeartbeatReport(HeartbeatReport heartbeatReport) {
			m_currentDomain = heartbeatReport.getDomain();
			super.visitHeartbeatReport(heartbeatReport);
		}

		@Override
		public void visitMachine(Machine machine) {
			String ip = machine.getIp();
			String classpath = machine.getClasspath();

			if (classpath != null) {
				String[] jars = classpath.split(",");
				Map<String, String> result = new LinkedHashMap<String, String>();

				for (String base : s_jars) {
					result.put(base, "-");
				}

				for (String jar : jars) {
					int lastIndex = jar.lastIndexOf("-");

					if (jar.contains("-SNAPSHOT")) {
						lastIndex = jar.lastIndexOf("-SNAPSHOT");
						lastIndex = jar.lastIndexOf("-", lastIndex - 1);
					}

					if (lastIndex > -1) {
						String jarName = jar.substring(0, lastIndex);
						String version = result.get(jarName);

						if (version != null) {
							String newVersion = jar.substring(lastIndex + 1, jar.lastIndexOf("."));

							result.put(jarName, newVersion);
						}
					}
				}
				Domain domainInfo = m_jarReport.findOrCreateDomain(m_currentDomain);
				com.dianping.cat.home.jar.entity.Machine machineinfo = domainInfo.findOrCreateMachine(ip);

				for (Entry<String, String> entry : result.entrySet()) {
					Jar jar = new Jar();

					jar.setId(entry.getKey()).setVersion(entry.getValue());
					machineinfo.addJar(jar);
				}
			}
		}
	}

}
