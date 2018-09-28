package com.dianping.cat.status.jvm;

import com.dianping.cat.status.AbstractCollector;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClassLoadingInfoCollector extends AbstractCollector {

    private Map<String, Number> doClassLoadingCollect() {
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        Map<String, Number> map = new LinkedHashMap<String, Number>();

        map.put("jvm.classloading.loaded.count", classLoadingMXBean.getLoadedClassCount());
        map.put("jvm.classloading.totalloaded.count", classLoadingMXBean.getTotalLoadedClassCount());
        map.put("jvm.classloading.unloaded.count", classLoadingMXBean.getUnloadedClassCount());

        return map;
    }

    @Override
    public String getId() {
        return "jvm.classingloading";
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, Number> map = doClassLoadingCollect();

        return convert(map);
    }

}
