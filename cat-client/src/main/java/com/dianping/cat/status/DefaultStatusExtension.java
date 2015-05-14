package com.dianping.cat.status;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yj.huang on 15-4-7.
 */
public class DefaultStatusExtension implements StatusExtension {
    private String id;
    private Map<String, String> properties = new HashMap<String, String>();

    public DefaultStatusExtension (String id, String key, String value) {
        this.id = id;
        if (key != null) {
            this.properties.put(key, value);
        }
    }

    public DefaultStatusExtension (String id, Map<String, String> properties) {
        this.id = id;
        if (properties != null) {
            this.properties = properties;
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperty(String key, String value) {
        this.properties.put(key, value);
    }
}