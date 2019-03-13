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
package com.dianping.cat.status.system;

import com.dianping.cat.Cat;
import com.dianping.cat.status.AbstractCollector;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProcessorInfoCollector extends AbstractCollector {

    private long lastProcessCputime = 0;

    private static boolean isSunOsMBean(OperatingSystemMXBean operatingSystem) {
        final String className = operatingSystem.getClass().getName();
        return "com.sun.management.OperatingSystem".equals(className)
                || "com.sun.management.UnixOperatingSystem".equals(className);
    }

    private Map<String, Number> doProcessCollect() {
        Map<String, Number> map = new LinkedHashMap<String, Number>();
        OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();

        map.put("system.load.average", operatingSystem.getSystemLoadAverage());

        if (operatingSystem instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean osBean = (com.sun.management.OperatingSystemMXBean) operatingSystem;
            Method[] methods = com.sun.management.OperatingSystemMXBean.class.getMethods();
            try {
                for (Method method : methods) {
                    if (method.getName().equals("getSystemCpuLoad")) {
                        Double systemCpuLoad;
                        systemCpuLoad = (Double) method.invoke(osBean, null);
                        map.put("cpu.system.load.percent", systemCpuLoad * 100);
                    }
                    if (method.getName().equals("getProcessCpuLoad")) {
                        Double processCpuLoad;
                        processCpuLoad = (Double) method.invoke(osBean, null);
                        map.put("cpu.jvm.load.percent", processCpuLoad * 100);
                    }
                }
            } catch (Exception e) {
                Cat.logError(e);
            }
            map.put("system.process.used.phyical.memory",
                    osBean.getTotalPhysicalMemorySize() - osBean.getFreePhysicalMemorySize());
            map.put("system.process.used.swap.size", osBean.getTotalSwapSpaceSize() - osBean.getFreeSwapSpaceSize());
        }

        if (isSunOsMBean(operatingSystem)) {
            if (operatingSystem instanceof com.sun.management.UnixOperatingSystemMXBean) {
                final com.sun.management.UnixOperatingSystemMXBean unixOsBean = (com.sun.management.UnixOperatingSystemMXBean) operatingSystem;
                try {
                    map.put("jvm.process.filedescriptors", unixOsBean.getOpenFileDescriptorCount());
                } catch (Exception e) {
                    // pour issue 16 (using jsvc on ubuntu or debian)
                }
            }

            if (operatingSystem instanceof com.sun.management.OperatingSystemMXBean) {
                final com.sun.management.OperatingSystemMXBean osBean = (com.sun.management.OperatingSystemMXBean) operatingSystem;
                long processCpuTime = osBean.getProcessCpuTime() / 1000000;
                map.put("jvm.process.cputime", processCpuTime - lastProcessCputime);

                lastProcessCputime = processCpuTime;
            }
        }

        return map;
    }

    @Override
    public String getId() {
        return "system.process";
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, Number> map = doProcessCollect();

        return convert(map);
    }

}
