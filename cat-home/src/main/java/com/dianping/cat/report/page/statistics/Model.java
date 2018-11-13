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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.heavy.entity.Service;
import com.dianping.cat.home.heavy.entity.Url;
import com.dianping.cat.home.jar.entity.JarReport;
import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.entity.Domain;
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;

@ModelMeta("statistics")
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	private String m_browserChart;

	private String m_osChart;

	private String m_summaryContent;

	@EntityMeta
	private ServiceReport m_serviceReport;

	@EntityMeta
	private HeavyReport m_heavyReport;

	@EntityMeta
	private JarReport m_jarReport;

	@EntityMeta
	private ClientReport m_clientReport;

	@EntityMeta
	private UtilizationReport m_utilizationReport;

	private List<String> m_jars;

	private List<String> m_keys;

	private List<Domain> m_serviceList;

	private List<Url> m_callUrls;

	private List<Service> m_callServices;

	private List<Url> m_sqlUrls;

	private List<Service> m_sqlServices;

	private List<Url> m_cacheUrls;

	private List<Service> m_cacheServices;

	private List<com.dianping.cat.home.utilization.entity.Domain> m_utilizationWebList;

	private List<com.dianping.cat.home.utilization.entity.Domain> m_utilizationServiceList;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getBrowserChart() {
		return m_browserChart;
	}

	public void setBrowserChart(String browserChart) {
		m_browserChart = browserChart;
	}

	public List<Service> getCacheServices() {
		return m_cacheServices;
	}

	public void setCacheServices(List<Service> cacheServices) {
		m_cacheServices = cacheServices;
	}

	public List<Url> getCacheUrls() {
		return m_cacheUrls;
	}

	public void setCacheUrls(List<Url> cacheUrls) {
		m_cacheUrls = cacheUrls;
	}

	public List<Service> getCallServices() {
		return m_callServices;
	}

	public void setCallServices(List<Service> callServices) {
		m_callServices = callServices;
	}

	public List<Url> getCallUrls() {
		return m_callUrls;
	}

	public void setCallUrls(List<Url> callUrls) {
		m_callUrls = callUrls;
	}

	public ClientReport getClientReport() {
		return m_clientReport;
	}

	public void setClientReport(ClientReport clientReport) {
		m_clientReport = clientReport;
	}

	@Override
	public Action getDefaultAction() {
		return Action.SERVICE_REPORT;
	}

	@Override
	public String getDomain() {
		return Constants.CAT;
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}

	public HeavyReport getHeavyReport() {
		return m_heavyReport;
	}

	public void setHeavyReport(HeavyReport heavyReport) {
		m_heavyReport = heavyReport;
	}

	public JarReport getJarReport() {
		return m_jarReport;
	}

	public void setJarReport(JarReport jarReport) {
		m_jarReport = jarReport;
	}

	public List<String> getJars() {
		return m_jars;
	}

	public void setJars(List<String> jars) {
		m_jars = jars;
	}

	public List<String> getKeys() {
		return m_keys;
	}

	public void setKeys(List<String> keys) {
		m_keys = keys;
	}

	public String getOsChart() {
		return m_osChart;
	}

	public void setOsChart(String osChart) {
		m_osChart = osChart;
	}

	public List<com.dianping.cat.home.service.entity.Domain> getServiceList() {
		return m_serviceList;
	}

	public void setServiceList(List<com.dianping.cat.home.service.entity.Domain> serviceList) {
		this.m_serviceList = serviceList;
	}

	public ServiceReport getServiceReport() {
		return m_serviceReport;
	}

	public void setServiceReport(ServiceReport serviceReport) {
		m_serviceReport = serviceReport;
	}

	public List<Service> getSqlServices() {
		return m_sqlServices;
	}

	public void setSqlServices(List<Service> sqlServices) {
		m_sqlServices = sqlServices;
	}

	public List<Url> getSqlUrls() {
		return m_sqlUrls;
	}

	public void setSqlUrls(List<Url> sqlUrls) {
		m_sqlUrls = sqlUrls;
	}

	public String getSummaryContent() {
		return m_summaryContent;
	}

	public void setSummaryContent(String summaryContent) {
		m_summaryContent = summaryContent;
	}

	public UtilizationReport getUtilizationReport() {
		return m_utilizationReport;
	}

	public void setUtilizationReport(UtilizationReport utilizationReport) {
		m_utilizationReport = utilizationReport;
	}

	public List<com.dianping.cat.home.utilization.entity.Domain> getUtilizationServiceList() {
		return m_utilizationServiceList;
	}

	public void setUtilizationServiceList(List<com.dianping.cat.home.utilization.entity.Domain> utilizationServiceList) {
		m_utilizationServiceList = utilizationServiceList;
	}

	public List<com.dianping.cat.home.utilization.entity.Domain> getUtilizationWebList() {
		return m_utilizationWebList;
	}

	public void setUtilizationWebList(List<com.dianping.cat.home.utilization.entity.Domain> utilizationWebList) {
		m_utilizationWebList = utilizationWebList;
	}

}
