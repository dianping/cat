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
package com.dianping.cat.report.page.statistics;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.heavy.entity.HeavyCache;
import com.dianping.cat.home.heavy.entity.HeavyCall;
import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.heavy.entity.HeavySql;
import com.dianping.cat.home.heavy.entity.Service;
import com.dianping.cat.home.heavy.entity.Url;
import com.dianping.cat.home.jar.entity.JarReport;
import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.alert.summary.AlertSummaryExecutor;
import com.dianping.cat.report.page.statistics.service.ClientReportService;
import com.dianping.cat.report.page.statistics.service.HeavyReportService;
import com.dianping.cat.report.page.statistics.service.JarReportService;
import com.dianping.cat.report.page.statistics.service.ServiceReportService;
import com.dianping.cat.report.page.statistics.service.UtilizationReportService;
import com.dianping.cat.report.page.statistics.task.heavy.HeavyReportMerger.ServiceComparator;
import com.dianping.cat.report.page.statistics.task.heavy.HeavyReportMerger.UrlComparator;
import com.dianping.cat.report.page.statistics.task.jar.JarReportBuilder;
import com.dianping.cat.service.ProjectService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private HeavyReportService m_heavyReportService;

	@Inject
	private UtilizationReportService m_utilizationReportService;

	@Inject
	private ServiceReportService m_serviceReportService;

	@Inject
	private ClientReportService m_clientReportService;

	@Inject
	private JarReportService m_jarReportService;

	@Inject
	private ProjectService m_projectService;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private AlertSummaryExecutor m_executor;

	private void buildHeavyInfo(Model model, Payload payload) {
		HeavyReport heavyReport = queryHeavyReport(payload);

		model.setHeavyReport(heavyReport);
		buildSortedHeavyInfo(model, heavyReport);
	}

	private void buildJarInfo(Model model, Payload payload) {
		JarReport jarReport = queryJarReport(payload);

		model.setJars(JarReportBuilder.s_jars);
		model.setJarReport(jarReport);
	}

	private void buildServiceInfo(Model model, Payload payload) {
		ServiceReport serviceReport = queryServiceReport(payload);
		List<com.dianping.cat.home.service.entity.Domain> dHisList = sort(serviceReport, payload.getSortBy());
		model.setServiceList(dHisList);
		model.setServiceReport(serviceReport);
	}

	private void buildSortedHeavyInfo(Model model, HeavyReport heavyReport) {
		HeavyCall heavyCall = heavyReport.getHeavyCall();
		if (heavyCall != null) {

			List<Url> callUrls = new ArrayList<Url>(heavyCall.getUrls().values());
			List<Service> callServices = new ArrayList<Service>(heavyCall.getServices().values());
			Collections.sort(callUrls, new UrlComparator());
			Collections.sort(callServices, new ServiceComparator());
			model.setCallUrls(callUrls);
			model.setCallServices(callServices);
		}

		HeavySql heavySql = heavyReport.getHeavySql();

		if (heavySql != null) {
			List<Url> sqlUrls = new ArrayList<Url>(heavySql.getUrls().values());
			List<Service> sqlServices = new ArrayList<Service>(heavySql.getServices().values());
			Collections.sort(sqlUrls, new UrlComparator());
			Collections.sort(sqlServices, new ServiceComparator());
			model.setSqlUrls(sqlUrls);
			model.setSqlServices(sqlServices);
		}

		HeavyCache heavyCache = heavyReport.getHeavyCache();
		if (heavyCache != null) {
			List<Url> cacheUrls = new ArrayList<Url>(heavyCache.getUrls().values());
			List<Service> cacheServices = new ArrayList<Service>(heavyCache.getServices().values());
			Collections.sort(cacheUrls, new UrlComparator());
			Collections.sort(cacheServices, new ServiceComparator());
			model.setCacheUrls(cacheUrls);
			model.setCacheServices(cacheServices);
		}
	}

	private void buildUtilizationInfo(Model model, Payload payload) {
		UtilizationReport utilizationReport = queryUtilizationReport(payload);
		Collection<com.dianping.cat.home.utilization.entity.Domain> dUList = utilizationReport.getDomains().values();
		List<com.dianping.cat.home.utilization.entity.Domain> dUWebList = new LinkedList<com.dianping.cat.home.utilization.entity.Domain>();
		List<com.dianping.cat.home.utilization.entity.Domain> dUServiceList = new LinkedList<com.dianping.cat.home.utilization.entity.Domain>();

		for (com.dianping.cat.home.utilization.entity.Domain d : dUList) {
			if (d.findApplicationState("URL") != null) {
				dUWebList.add(d);
			}
			if (d.findApplicationState("PigeonService") != null) {
				dUServiceList.add(d);
			}
		}
		model.setUtilizationWebList(dUWebList);
		model.setUtilizationServiceList(dUServiceList);
		model.setUtilizationReport(utilizationReport);
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "statistics")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "statistics")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		m_normalizePayload.normalize(model, payload);
		model.setAction(payload.getAction());

		Action action = payload.getAction();

		switch (action) {
		case SERVICE_REPORT:
		case SERVICE_HISTORY_REPORT:
			buildServiceInfo(model, payload);
			break;
		case HEAVY_HISTORY_REPORT:
		case HEAVY_REPORT:
			buildHeavyInfo(model, payload);
			break;
		case UTILIZATION_REPORT:
		case UTILIZATION_HISTORY_REPORT:
			buildUtilizationInfo(model, payload);
			break;
		case ALERT_SUMMARY:
			String domain = payload.getSummarydomain();

			if (StringUtils.isNotEmpty(domain)) {
				String summaryContent = m_executor.execute(domain, payload.getSummarytime(), payload.getSummaryemails());
				model.setSummaryContent(summaryContent);
			}
			break;
		case JAR_REPORT:
			buildJarInfo(model, payload);
			break;
		case CLIENT_REPORT:
			buildClientReport(model, payload);
			break;
		}
		model.setPage(ReportPage.STATISTICS);
		m_jspViewer.view(ctx, model);
	}

	private void buildClientReport(Model model, Payload payload) {
		Date startDate = payload.getDay();
		Date endDate = TimeHelper.addDays(startDate, 1);
		ClientReport report = m_clientReportService.queryReport(Constants.CAT, startDate, endDate);

		model.setClientReport(report);
	}

	private HeavyReport queryHeavyReport(Payload payload) {
		Pair<Date, Date> pair = queryStartEndTime(payload);

		return m_heavyReportService.queryReport(Constants.CAT, pair.getKey(), pair.getValue());
	}

	private JarReport queryJarReport(Payload payload) {
		Pair<Date, Date> pair = queryStartEndTime(payload);

		return m_jarReportService.queryReport(Constants.CAT, pair.getKey(), pair.getValue());
	}

	private ServiceReport queryServiceReport(Payload payload) {
		Pair<Date, Date> pair = queryStartEndTime(payload);

		return m_serviceReportService.queryReport(Constants.CAT, pair.getKey(), pair.getValue());
	}

	private Pair<Date, Date> queryStartEndTime(Payload payload) {
		Date start = null;
		Date end = null;
		Action action = payload.getAction();
		String name = action.getName();
		if (!name.startsWith("history")) {
			if (payload.getPeriod().isCurrent()) {
				start = new Date(payload.getDate() - TimeHelper.ONE_HOUR);
				end = new Date(start.getTime() + TimeHelper.ONE_HOUR);
			} else {
				start = new Date(payload.getDate());
				end = new Date(start.getTime() + TimeHelper.ONE_HOUR);
			}
		} else {
			start = payload.getHistoryStartDate();
			end = payload.getHistoryEndDate();
		}
		return new Pair<Date, Date>(start, end);
	}

	private UtilizationReport queryUtilizationReport(Payload payload) {
		Pair<Date, Date> pair = queryStartEndTime(payload);
		UtilizationReport report = m_utilizationReportService.queryReport(Constants.CAT, pair.getKey(), pair.getValue());
		Collection<com.dianping.cat.home.utilization.entity.Domain> domains = report.getDomains().values();

		for (com.dianping.cat.home.utilization.entity.Domain d : domains) {
			String domain = d.getId();
			Project project = m_projectService.findByDomain(domain);

			if (project != null) {
				d.setCmdbId(project.getCmdbDomain());
			}
		}
		return report;
	}

	private List<com.dianping.cat.home.service.entity.Domain> sort(ServiceReport serviceReport, final String sortBy) {
		List<com.dianping.cat.home.service.entity.Domain> result = new ArrayList<com.dianping.cat.home.service.entity.Domain>(
								serviceReport.getDomains().values());
		Collections.sort(result, new Comparator<com.dianping.cat.home.service.entity.Domain>() {
			public int compare(com.dianping.cat.home.service.entity.Domain d1,	com.dianping.cat.home.service.entity.Domain d2) {
				if (sortBy.equals("failure")) {
					return (int) (d2.getFailureCount() - d1.getFailureCount());
				} else if (sortBy.equals("total")) {
					long value = d2.getTotalCount() - d1.getTotalCount();

					if (value > 0) {
						return 1;
					} else {
						return -1;
					}
				} else if (sortBy.equals("failurePercent")) {
					return (int) (100000 * d2.getFailurePercent() - 100000 * d1.getFailurePercent());
				} else if (sortBy.equals("availability")) {
					return (int) (100000 * d2.getFailurePercent() - 100000 * d1.getFailurePercent());
				} else {
					return (int) (d2.getAvg() - d1.getAvg());
				}
			}
		});
		return result;
	}

}
