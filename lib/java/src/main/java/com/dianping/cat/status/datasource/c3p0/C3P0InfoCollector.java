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
package com.dianping.cat.status.datasource.c3p0;

import com.dianping.cat.status.datasource.DataSourceCollector;
import com.dianping.cat.status.datasource.DatabaseParserHelper;
import com.dianping.cat.util.Properties;

import javax.management.ObjectName;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class C3P0InfoCollector extends DataSourceCollector {

    private static final String PREFIX_KEY = "c3p0";

    private Map<String, Number> doCollect() {
        Map<String, Number> map = new LinkedHashMap<String, Number>();
        Map<String, C3P0MonitorInfo> c3P0MonitorInfoMap = getC3P0MonitorInfoMap();
        String detail = Properties.forString().fromEnv().fromSystem().getProperty("CAT_DATASOURCE_DETAIL", "false");

        for (Map.Entry<String, C3P0MonitorInfo> entry : c3P0MonitorInfoMap.entrySet()) {
            String dataSourceName = entry.getKey();
            C3P0MonitorInfo value = entry.getValue();

            map.put(PREFIX_KEY + SPLIT + dataSourceName + ".busy_connection", value.getNumBusyConnections());
            map.put(PREFIX_KEY + SPLIT + dataSourceName + ".total_connection", value.getNumConnections());
            map.put(PREFIX_KEY + SPLIT + dataSourceName + ".idle_connection", value.getNumIdleConnections());

            if ("true".equals(detail)) {
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".failed_checkin", value.getNumFailedCheckIns());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".failed_checkout", value.getNumFailedCheckOuts());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".failed_test", value.getNumFailedIdleTests());
            }
        }

        return map;
    }

    private C3P0MonitorInfo getC3P0MonitorInfo(ObjectName objectName) {
        C3P0MonitorInfo c3P0MonitorInfo = new C3P0MonitorInfo();
        String jdbcUrl = getStringAttribute(objectName, "jdbcUrl");

        c3P0MonitorInfo.setJdbcUrl(jdbcUrl);
        c3P0MonitorInfo.setNumBusyConnections(getIntegerAttribute(objectName, "numBusyConnections", false));
        c3P0MonitorInfo.setNumConnections(getIntegerAttribute(objectName, "numConnections", false));
        c3P0MonitorInfo.setNumIdleConnections(getIntegerAttribute(objectName, "numIdleConnections", false));

        c3P0MonitorInfo.setNumFailedCheckIns((getLongAttribute(objectName, "numFailedCheckinsDefaultUser", false)));
        c3P0MonitorInfo.setNumFailedCheckOuts((getLongAttribute(objectName, "numFailedCheckoutsDefaultUser", false)));
        c3P0MonitorInfo.setNumFailedIdleTests((getLongAttribute(objectName, "numFailedIdleTestsDefaultUser", false)));

        return c3P0MonitorInfo;
    }

    private Map<String, C3P0MonitorInfo> getC3P0MonitorInfoMap() {
        Map<String, C3P0MonitorInfo> dataSourceInfoMap = new LinkedHashMap<String, C3P0MonitorInfo>();

        try {
            ObjectName pooledDataSourceObjectName = new ObjectName("com.mchange.v2.c3p0", "type", "PooledDataSource*");
            Set<ObjectName> objectNameSet = mbeanServer.queryNames(pooledDataSourceObjectName, null);

            if (objectNameSet == null || objectNameSet.isEmpty()) {
                return dataSourceInfoMap;
            }

            Map<String, Integer> datasources = new LinkedHashMap<String, Integer>();

            for (ObjectName objectName : objectNameSet) {
                C3P0MonitorInfo info = getC3P0MonitorInfo(objectName);
                String url = info.getJdbcUrl();
                DatabaseParserHelper.Database datasource = databaseParser.parseDatabase(url);
                String key = getConnection(datasources, datasource.toString());

                dataSourceInfoMap.put(key, info);
            }
        } catch (Exception ignored) {
        }
        return dataSourceInfoMap;
    }

    @Override
    public String getId() {
        return "datasource.c3p0";
    }

    @Override
    public Map<String, String> getProperties() {
        return convert(doCollect());
    }

}
