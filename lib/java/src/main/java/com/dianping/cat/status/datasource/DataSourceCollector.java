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
package com.dianping.cat.status.datasource;

import com.dianping.cat.status.AbstractCollector;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

public abstract class DataSourceCollector extends AbstractCollector {
    private Map<String, Object> lastValueMap = new HashMap<String, Object>();
    protected MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
    protected DatabaseParserHelper databaseParser = new DatabaseParserHelper();

    protected static final char SPLIT = '.';
    private static final Integer ERROR_INT = -1;
    private static final Long ERROR_LONG = -1L;
    private static final String ERROR_ATTRIBUTE = "unknown";

    private Integer diffLast(String key, Integer value) {
        Object lastValue = lastValueMap.get(key);

        if (lastValue != null) {
            lastValueMap.put(key, value);
            return value - (Integer) lastValue;
        } else {
            lastValueMap.put(key, value);
            return value;
        }
    }

    private Long diffLast(String key, Long value) {
        Object lastValue = lastValueMap.get(key);
        if (lastValue != null) {
            lastValueMap.put(key, value);
            return value - (Long) lastValue;
        } else {
            lastValueMap.put(key, value);
            return value;
        }
    }

    protected String getConnection(Map<String, Integer> datasources, String key) {
        Integer index = datasources.get(key);

        if (index == null) {
            datasources.put(key, 0);

            return key;
        } else {
            index++;

            datasources.put(key, index);
            return key + '[' + index + ']';
        }
    }

    @Override
    public String getDescription() {
        return "datasource.c3p0";
    }

    @Override
    public String getId() {
        return "datasource.c3p0";
    }

    protected Integer getIntegerAttribute(ObjectName objectName, String attribute, Boolean isDiff) {
        try {
            Integer value = (Integer) mbeanServer.getAttribute(objectName, attribute);
            if (isDiff) {
                return diffLast(objectName.getCanonicalName() + attribute, value);
            } else {
                return value;
            }
        } catch (Exception e) {
            return ERROR_INT;
        }
    }

    protected Long getLongAttribute(ObjectName objectName, String attribute, Boolean isDiff) {
        try {
            Long value = (Long) mbeanServer.getAttribute(objectName, attribute);
            if (isDiff) {
                return diffLast(objectName.getCanonicalName() + attribute, value);
            } else {
                return value;
            }
        } catch (Exception e) {
            return ERROR_LONG;
        }
    }

    protected String getStringAttribute(ObjectName objectName, String attribute) {
        try {
            return (String) mbeanServer.getAttribute(objectName, attribute);
        } catch (Exception e) {
            return ERROR_ATTRIBUTE;
        }
    }

    protected Boolean isRandomName(String name) {
        return name != null && name.length() > 30;
    }

}
