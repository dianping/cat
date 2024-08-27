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
package com.dianping.cat.alarm.spi;

public enum AlertType {

	Business("Business", "业务告警",
		"http://{0}:{1}/cat/r/t?domain={2}&date={3}&ip=All&type={4}",
		"http://{0}:{1}/cat/s/business?op=add&domain={2}&key={3}"), // key=BusinessKey

	Network("Network", "网络告警", "", ""),

	DataBase("Database", "数据库告警", "", ""),

	System("System", "系统告警", "", ""),

	Exception("Exception", "异常告警",
		"http://{0}:{1}/cat/r/p?domain={2}&date={3}&ip=All&type=RuntimeException,Exception&metric={4}",
		"http://{0}:{1}/cat/s/config?op=exceptionThresholdUpdate&domain={2}&exception={3}"),

	HeartBeat("Heartbeat", "心跳告警",
		"http://{0}:{1}/cat/r/h?domain={2}&date={3}&ip=All&reportType=day&op=view",
		"http://{0}:{1}/cat/s/config?op=heartbeatRuleConfigList"),

	ThirdParty("ThirdParty", "第三方告警", "", ""),

	FrontEndException("FrontEnd", "前端告警", "", ""),

	JS("Js", "JS错误告警", "", ""),

	App("App", "APP接口告警", "", ""),

	Ajax("Ajax", "Ajax访问告警", "", ""),

	Transaction("Transaction", "Transaction告警",
		"http://{0}:{1}/cat/r/t?domain={2}&date={3}&ip=All&type={4}",
		"http://{0}:{1}/cat/s/config?op=transactionRuleUpdate&ruleId={2};{3}"),

	Event("Event", "Event告警",
		"http://{0}:{1}/cat/r/e?domain={2}&date={3}&ip=All&type={4}",
		"http://{0}:{1}/cat/s/config?op=eventRuleUpdate&ruleId={2};{3}"),

	STORAGE_SQL("SQL", "数据库大盘告警",
		"http://{0}:{1}/cat/r/storage?op=dashboard&domain={2}&date={3}&fullScreen=false&refresh=false&frequency=10&type=SQL",
		"http://{0}:{1}/cat/s/config?op=storageRuleUpdate&type=SQL&ruleId={2};{3}"),

	STORAGE_CACHE("Cache", "缓存大盘告警",
		"http://{0}:{1}/cat/r/storage?op=dashboard&domain={2}&date={3}&fullScreen=false&refresh=false&frequency=10&type=Cache",
		"http://{0}:{1}/cat/s/config?op=storageRuleUpdate&type=Cache&ruleId={2};{3}"),

	STORAGE_RPC("RPC", "服务大盘告警",
		"http://{0}:{1}/cat/r/storage?op=dashboard&domain={2}&date={3}&fullScreen=false&refresh=false&frequency=10&type=RPC",
		"http://{0}:{1}/cat/s/config?op=storageRuleUpdate&type=RPC&ruleId={2};{3}"),

	SERVER_NETWORK("ServerNetwork", "网络告警", "", ""),

	SERVER_SYSTEM("ServerSystem", "系统告警", "", ""),

	SERVER_DATABASE("ServerDatabase", "数据库告警", "", ""),

	CRASH("Crash", "Crash告警", "", "");

	private String m_name;

	private String m_title;

	private final String viewLink;

	private final String settingsLink;

	AlertType(String name, String title, String viewLink, String settingsLink) {
		this.m_name = name;
		this.m_title = title;
		this.viewLink = viewLink;
		this.settingsLink = settingsLink;
	}

	public static AlertType getTypeByName(String name) {
		for (AlertType type : AlertType.values()) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		return null;
	}

	public static String parseViewLink(String name) {
		for (AlertType alertType: AlertType.values()) {
			if (alertType.getName().equalsIgnoreCase(name)) {
				return alertType.getViewLink();
			}
		}
		throw new UnsupportedOperationException();
	}

	public static String parseSettingsLink(String name) {
		for (AlertType alertType: AlertType.values()) {
			if (alertType.getName().equalsIgnoreCase(name)) {
				return alertType.getSettingsLink();
			}
		}
		throw new UnsupportedOperationException();
	}

	public String getName() {
		return m_name;
	}

	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public String getViewLink() {
		return viewLink;
	}

	public String getSettingsLink() {
		return settingsLink;
	}
}
