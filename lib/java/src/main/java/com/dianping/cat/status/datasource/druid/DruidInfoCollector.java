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
package com.dianping.cat.status.datasource.druid;

import com.dianping.cat.util.Properties;
import com.dianping.cat.status.datasource.DataSourceCollector;
import com.dianping.cat.status.datasource.DatabaseParserHelper;

import javax.management.ObjectName;
import java.util.*;

public class DruidInfoCollector extends DataSourceCollector {

    private final static String PREFIX_KEY = "druid";

    private Map<String, Number> doCollect() {
        Map<String, DruidMonitorInfo> druidMonitorInfoMap = getDruidMonitorInfoMap();
        Map<String, Number> map = new HashMap<String, Number>();
        String detail = Properties.forString().fromEnv().fromSystem().getProperty("CAT_DATASOURCE_DETAIL", "false");

        for (Map.Entry<String, DruidMonitorInfo> entry : druidMonitorInfoMap.entrySet()) {
            String dataSourceName = entry.getKey();
            DruidMonitorInfo value = entry.getValue();

            map.put(PREFIX_KEY + SPLIT + dataSourceName + ".busy_connection", value.getActiveCount());
            map.put(PREFIX_KEY + SPLIT + dataSourceName + ".total_connection", value.getPoolingCount());
            map.put(PREFIX_KEY + SPLIT + dataSourceName + ".idle_connection", value.getPoolingCount() - value.getActiveCount());

            if ("true".equals(detail)) {
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".active_count", value.getActiveCount());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".pooling_count", value.getPoolingCount());

                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".connect_count", value.getConnectCount());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".connect_error_count", value.getConnectErrorCount());

                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".close_count", value.getCloseCount());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".close_prepared_statement_count", value.getClosedPreparedStatementCount());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".commit_count", value.getCommitCount());

                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".create_count", value.getCreateCount());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".create_error_count", value.getCreateErrorCount());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".create_timespan_millis", value.getCreateTimeSpanMillis());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".avg_create_timespan_millis", value.getAvgCreateTimeSpanMillis());

                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".destroy_count", value.getDestroyCount());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".discard_count", value.getDiscardCount());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".error_count", value.getErrorCount());

                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".lock_queue_length", value.getLockQueueLength());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".max_active", value.getMaxActive());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".max_idle", value.getMaxIdle());

                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".max_open_prepared_statements", value.getMaxOpenPreparedStatements());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".max_pool_prepared_statement_pre_connection_size", entry.getValue().getMaxPoolPreparedStatementPerConnectionSize());

                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".not_empty_wait_count", value.getNotEmptyWaitCount());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".not_empty_wait_millis", value.getNotEmptyWaitMillis());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".avg_not_empty_wait_millis", value.getAvgNotEmptyWaitMillis());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".not_empty_wait_thread_count", value.getNotEmptyWaitThreadCount());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".wait_thread_count", value.getWaitThreadCount());

                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".recycle_count", value.getRecycleCount());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".rollback_count", value.getRollbackCount());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".start_transaction_count", value.getStartTransactionCount());

                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".max_wait", value.getMaxWait());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".max_wait_thread_count", value.getMaxWaitThreadCount());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".min_idle", value.getMinIdle());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".query_timeout", value.getQueryTimeout());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".dup_close_count", value.getDupCloseCount());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".initial_size", value.getInitialSize());
                map.put(PREFIX_KEY + SPLIT + dataSourceName + ".remove_abandoned_count", value.getRemoveAbandonedCount());
            }
        }

        return map;
    }

    private DruidMonitorInfo getDruidMonitorInfo(ObjectName objectName) {
        DruidMonitorInfo druidMonitorInfo = new DruidMonitorInfo();
        String jdbcUrl = getStringAttribute(objectName, "Url");

        druidMonitorInfo.setJdbcUrl(jdbcUrl);
        druidMonitorInfo.setActiveCount(getIntegerAttribute(objectName, "ActiveCount", false));
        druidMonitorInfo.setConnectCount(getLongAttribute(objectName, "ConnectCount", true));
        druidMonitorInfo.setConnectErrorCount(getLongAttribute(objectName, "ConnectErrorCount", true));

        druidMonitorInfo.setCloseCount(getLongAttribute(objectName, "CloseCount", true));
        druidMonitorInfo.setClosedPreparedStatementCount(getLongAttribute(objectName, "ClosedPreparedStatementCount",
                true));
        druidMonitorInfo.setCommitCount(getLongAttribute(objectName, "CommitCount", true));
        druidMonitorInfo.setCreateCount(getLongAttribute(objectName, "CreateCount", true));
        druidMonitorInfo.setCreateErrorCount(getLongAttribute(objectName, "CreateErrorCount", true));
        druidMonitorInfo.setCreateTimeSpanMillis(getLongAttribute(objectName, "CreateTimespanMillis", true));

        druidMonitorInfo.setDestroyCount(getLongAttribute(objectName, "DestroyCount", true));
        druidMonitorInfo.setDiscardCount(getLongAttribute(objectName, "DiscardCount", true));
        druidMonitorInfo.setErrorCount(getLongAttribute(objectName, "ErrorCount", true));
        druidMonitorInfo.setLockQueueLength(getIntegerAttribute(objectName, "LockQueueLength", false));
        druidMonitorInfo.setMaxActive(getIntegerAttribute(objectName, "MaxActive", false));
        druidMonitorInfo.setMaxIdle(getIntegerAttribute(objectName, "MaxIdle", false));

        druidMonitorInfo.setMaxOpenPreparedStatements(getIntegerAttribute(objectName, "MaxOpenPreparedStatements", false));
        druidMonitorInfo.setMaxPoolPreparedStatementPerConnectionSize(getIntegerAttribute(objectName, "MaxPoolPreparedStatementPerConnectionSize", false));

        druidMonitorInfo.setNotEmptyWaitCount(getLongAttribute(objectName, "NotEmptyWaitCount", true));
        druidMonitorInfo.setNotEmptyWaitMillis(getLongAttribute(objectName, "NotEmptyWaitMillis", true));
        druidMonitorInfo.setNotEmptyWaitThreadCount(getIntegerAttribute(objectName, "NotEmptyWaitThreadCount", false));
        druidMonitorInfo.setWaitThreadCount(getIntegerAttribute(objectName, "WaitThreadCount", false));

        druidMonitorInfo.setPoolingCount(getIntegerAttribute(objectName, "PoolingCount", false));
        druidMonitorInfo.setRecycleCount(getLongAttribute(objectName, "RecycleCount", true));
        druidMonitorInfo.setRollbackCount(getLongAttribute(objectName, "RollbackCount", true));
        druidMonitorInfo.setStartTransactionCount(getLongAttribute(objectName, "StartTransactionCount", true));

        druidMonitorInfo.setMaxWait(getLongAttribute(objectName, "MaxWait", false));
        druidMonitorInfo.setMaxWaitThreadCount(getIntegerAttribute(objectName, "MaxWaitThreadCount", false));
        druidMonitorInfo.setMinIdle(getIntegerAttribute(objectName, "MinIdle", false));
        druidMonitorInfo.setQueryTimeout(getIntegerAttribute(objectName, "QueryTimeout", false));
        druidMonitorInfo.setDupCloseCount(getLongAttribute(objectName, "DupCloseCount", true));
        druidMonitorInfo.setInitialSize(getIntegerAttribute(objectName, "InitialSize", false));
        druidMonitorInfo.setRemoveAbandonedCount(getLongAttribute(objectName, "RemoveAbandonedCount", true));

        return druidMonitorInfo;
    }

    private Map<String, DruidMonitorInfo> getDruidMonitorInfoMap() {
        Map<String, DruidMonitorInfo> dataSourceInfoMap = new HashMap<String, DruidMonitorInfo>();
        try {
            Hashtable<String, String> table = new Hashtable<String, String>();

            table.put("type", "DruidDataSource");
            table.put("id", "*");

            ObjectName pooledDataSourceObjectName = new ObjectName("com.alibaba.druid", table);
            Set<ObjectName> objectNameSet = mbeanServer.queryNames(pooledDataSourceObjectName, null);

            if (objectNameSet == null || objectNameSet.isEmpty()) {
                return dataSourceInfoMap;
            }

            Map<String, Integer> datasources = new LinkedHashMap<String, Integer>();

            for (ObjectName objectName : objectNameSet) {
                DruidMonitorInfo info = getDruidMonitorInfo(objectName);
                String url = info.getJdbcUrl();
                DatabaseParserHelper.Database datasource = databaseParser.parseDatabase(url);
                String key = getConnection(datasources, datasource.toString());

                dataSourceInfoMap.put(key, info);
            }
        } catch (Exception e) {
            // ignore
        }
        return dataSourceInfoMap;
    }

    @Override
    public String getId() {
        return "datasource.druid";
    }

    @Override
    public Map<String, String> getProperties() {
        return convert(doCollect());
    }

}
