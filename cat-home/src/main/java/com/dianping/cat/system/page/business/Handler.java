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
package com.dianping.cat.system.page.business;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;
import org.unidal.web.mvc.annotation.PreInboundActionMeta;

import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.rule.transform.DefaultJsonBuilder;
import com.dianping.cat.alarm.spi.decorator.RuleFTLDecorator;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.config.business.ConfigItem;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;
import com.dianping.cat.report.alert.business.BusinessRuleConfigManager;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;
import com.dianping.cat.system.page.config.ConfigHtmlParser;

public class Handler implements PageHandler<Context> {

	@Inject
	protected RuleFTLDecorator m_ruleDecorator;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ProjectService m_projectService;

	@Inject
	private BusinessConfigManager m_configManager;

	@Inject
	private BusinessTagConfigManager m_tagConfigManger;

	@Inject
	private BusinessRuleConfigManager m_alertConfigManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	@Override
	@PreInboundActionMeta("login")
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "business")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@PreInboundActionMeta("login")
	@OutboundActionMeta(name = "business")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();
		String domain = payload.getDomain();

		model.setPage(SystemPage.BUSINESS);
		model.setAction(action);
		model.setDomains(m_projectService.findAllDomains());

		switch (action) {
		case LIST:
			listConfigs(domain, model);
			break;
		case ADD:
			BusinessReportConfig config = m_configManager.queryConfigByDomain(domain);

			if (config != null) {
				BusinessItemConfig itemConfig = config.findBusinessItemConfig(payload.getKey());
				if (itemConfig != null) {
					model.setBusinessItemConfig(itemConfig);
				}
			}
			break;
		case AddSubmit:
			updateConfig(model, payload, domain);
			listConfigs(domain, model);
			break;
		case DELETE:
			String key = payload.getKey();

			m_configManager.deleteBusinessItem(domain, key);
			listConfigs(domain, model);
			break;
		case CustomDelete:
			key = payload.getKey();

			m_configManager.deleteCustomItem(domain, key);
			listConfigs(domain, model);
			break;
		case TagConfig:
			String tagConfig = payload.getContent();

			if (!StringUtils.isEmpty(tagConfig)) {
				model.setOpState(m_tagConfigManger.store(tagConfig));
			}
			model.setContent(m_configHtmlParser.parse(m_tagConfigManger.getConfig().toString()));
			break;
		case AlertRuleAdd:
			alertRuleAdd(payload, model);
			break;
		case AlertRuleAddSubmit:
			alertRuleAddSubmit(payload, model);
			listConfigs(domain, model);
			break;
		case CustomAdd:
			config = m_configManager.queryConfigByDomain(domain);

			if (config != null) {
				CustomConfig itemConfig = config.findCustomConfig(payload.getKey());

				if (itemConfig != null) {
					model.setCustomConfig(itemConfig);
				}
			}
			break;
		case CustomAddSubmit:
			updateCustomConfig(model, payload, domain);
			listConfigs(domain, model);
			break;
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void alertRuleAddSubmit(Payload payload, Model model) {
		String domain = payload.getDomain();
		String key = payload.getKey();
		String configs = payload.getContent();
		String type = payload.getAttributes();

		m_alertConfigManager.updateRule(domain, key, configs, type);
	}

	private void alertRuleAdd(Payload payload, Model model) {
		String ruleId = "";
		String configsStr = "";
		String key = payload.getKey();
		String domain = payload.getDomain();
		String type = payload.getAttributes();
		Rule rule = m_alertConfigManager.queryRule(domain, key, type);

		if (rule != null) {
			ruleId = rule.getId();
			configsStr = new DefaultJsonBuilder(true).buildArray(rule.getConfigs());
		}
		String content = m_ruleDecorator.generateConfigsHtml(configsStr);

		model.setId(ruleId);
		model.setContent(content);
	}

	private void listConfigs(String domain, Model model) {
		BusinessReportConfig config = m_configManager.queryConfigByDomain(domain);
		Map<String, Set<String>> tags = m_tagConfigManger.findTagByDomain(domain);
		List<BusinessItemConfig> configs = new ArrayList<BusinessItemConfig>(config.getBusinessItemConfigs().values());

		Collections.sort(configs, new Comparator<BusinessItemConfig>() {

			@Override
			public int compare(BusinessItemConfig m1, BusinessItemConfig m2) {
				return (int) ((m1.getViewOrder() - m2.getViewOrder()) * 100);
			}
		});

		List<CustomConfig> customConfigs = new ArrayList<CustomConfig>(config.getCustomConfigs().values());

		Collections.sort(customConfigs, new Comparator<CustomConfig>() {

			@Override
			public int compare(CustomConfig m1, CustomConfig m2) {
				return (int) ((m1.getViewOrder() - m2.getViewOrder()) * 100);
			}
		});

		model.setConfigs(configs);
		model.setCustomConfigs(customConfigs);
		model.setTags(tags);
	}

	private void updateConfig(Model model, Payload payload, String domain) {
		BusinessReportConfig config;
		BusinessItemConfig itemConfig = payload.getBusinessItemConfig();
		String key = itemConfig.getId();
		config = m_configManager.queryConfigByDomain(domain);
		boolean isModify = false;
		boolean result = false;

		if (config != null) {
			Map<String, BusinessItemConfig> itemConfigs = config.getBusinessItemConfigs();
			BusinessItemConfig origin = itemConfigs.get(key);

			if (origin != null) {
				isModify = true;
				config.addBusinessItemConfig(itemConfig);
				result = m_configManager.updateConfigByDomain(config);
			}
		}

		if (!isModify) {
			ConfigItem item = new ConfigItem();

			item.setShowAvg(itemConfig.getShowAvg());
			item.setShowCount(itemConfig.getShowCount());
			item.setShowSum(itemConfig.getShowSum());
			item.setTitle(itemConfig.getTitle());
			item.setViewOrder(itemConfig.getViewOrder());

			result = m_configManager.insertBusinessConfigIfNotExist(domain, key, item);
		}

		model.setOpState(result);
	}

	private void updateCustomConfig(Model model, Payload payload, String domain) {
		CustomConfig itemConfig = payload.getCustomConfig();
		BusinessReportConfig config = m_configManager.queryConfigByDomain(domain);
		boolean result = false;

		if (StringUtils.isNotEmpty(itemConfig.getId())) {
			if (config.getId() != null) {
				config.addCustomConfig(itemConfig);
				result = m_configManager.updateConfigByDomain(config);
			} else {
				config.setId(domain);
				config.addCustomConfig(itemConfig);
				result = m_configManager.insertConfigByDomain(config);
			}
		}

		model.setOpState(result);
	}

}
