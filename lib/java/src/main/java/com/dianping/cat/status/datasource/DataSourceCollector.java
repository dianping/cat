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
    private static final Long ERROR_LONG = -1l;
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
