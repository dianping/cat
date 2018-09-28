package com.dianping.cat.status.http;

import com.dianping.cat.status.AbstractCollector;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpStatsCollector extends AbstractCollector {

    private Map<String, Number> doClassLoadingCollect() {
        Map<String, Number> map = new LinkedHashMap<String, Number>();
        HttpStats stats = HttpStats.getAndReset();

        map.put("http.count", stats.getHttpCount());
        map.put("http.meantime", stats.getHttpMeantime());
        map.put("http.status400.count", stats.getHttpStatus400Count());
        map.put("http.status500.count", stats.getHttpStatus500Count());
        return map;
    }

    @Override
    public String getId() {
        return "http.status";
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, Number> map = doClassLoadingCollect();

        return convert(map);
    }

}
