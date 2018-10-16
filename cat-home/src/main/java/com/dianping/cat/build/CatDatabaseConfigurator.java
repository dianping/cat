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
package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.lookup.configuration.Component;

final class CatDatabaseConfigurator extends AbstractJdbcResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		// all.add(defineJdbcDataSourceComponent("cat", "com.mysql.jdbc.Driver", "jdbc:mysql://127.0.0.1:3306/cat", "root", "***", "<![CDATA[useUnicode=true&autoReconnect=true]]>"));

		defineSimpleTableProviderComponents(all, "cat", com.dianping.cat.home.dal.report._INDEX.getEntityClasses());
		defineDaoComponents(all, com.dianping.cat.home.dal.report._INDEX.getDaoClasses());

		return all;
	}
}
