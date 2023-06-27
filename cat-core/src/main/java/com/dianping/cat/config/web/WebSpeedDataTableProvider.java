/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dianping.cat.config.web;

import com.dianping.cat.web.WebSpeedData;
import org.unidal.dal.jdbc.QueryEngine;
import org.unidal.dal.jdbc.mapping.TableProvider;
import org.unidal.lookup.annotation.Named;

import java.util.Map;

@Named(type = TableProvider.class, value = WebSpeedDataTableProvider.LOGIC_TABLE_NAME)
public class WebSpeedDataTableProvider implements TableProvider {
	public final static String LOGIC_TABLE_NAME = "web-speed-data";

	protected String m_logicalTableName = "web-speed-data";

	private String m_physicalTableName = "web_speed_data";

	private String m_dataSourceName = "web";

	@Override
	public String getDataSourceName(Map<String, Object> hints, String logicalTableName) {
		return m_dataSourceName;
	}

	@Override
	public String getPhysicalTableName(Map<String, Object> hints, String logicalTableName) {
		WebSpeedData webSpeedData = (WebSpeedData) hints.get(QueryEngine.HINT_DATA_OBJECT);

		return m_physicalTableName + "_" + webSpeedData.getSpeedId();
	}

	public void setDataSourceName(String dataSourceName) {
		m_dataSourceName = dataSourceName;
	}

	public void setLogicalTableName(String logicalTableName) {
		m_logicalTableName = logicalTableName;
	}
}
