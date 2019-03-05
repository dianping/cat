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
package com.dianping.cat.status.jvm;

import com.dianping.cat.Cat;
import com.dianping.cat.status.AbstractCollector;
import com.dianping.cat.status.StatusExtensionRegister;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class JvmInfoCollector {
    private static JvmInfoCollector collector = new JvmInfoCollector();
    private boolean hasOldGc = false;
    private long lastGcCount = 0;
    private long lastGcTime = 0;
    private long lastFullGcTime = 0;
    private long lastFullGcCount = 0;
    private long lastYoungGcTime = 0;
    private long lastYoungGcCount = 0;

    private Set<String> youngGcAlgorithm = new LinkedHashSet<String>() {
        private static final long serialVersionUID = -2953196532584721351L;

        {
            add("Copy");
            add("ParNew");
            add("PS Scavenge");
            add("G1 Young Generation");
        }
    };

    private Set<String> oldGcAlgorithm = new LinkedHashSet<String>() {
        private static final long serialVersionUID = -8267829533109860610L;

        {
            add("MarkSweepCompact");
            add("PS MarkSweep");
            add("ConcurrentMarkSweep");
            add("G1 Old Generation");
        }
    };

    public static Map<String, String> convert(Map<String, Number> map) {
        Map<String, String> result = new LinkedHashMap<String, String>();

        for (Entry<String, Number> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toString());
        }
        return result;
    }

    public static JvmInfoCollector getInstance() {
        return collector;
    }

    private JvmInfoCollector() {
    }

    private Map<String, Number> doGcCollect() {
        long gcCount = 0;
        long gcTime = 0;
        long oldGCount = 0;
        long oldGcTime = 0;
        long youngGcCount = 0;
        long youngGcTime = 0;
        Map<String, Number> map = new LinkedHashMap<String, Number>();

        for (final GarbageCollectorMXBean garbageCollector : ManagementFactory.getGarbageCollectorMXBeans()) {
            gcTime += garbageCollector.getCollectionTime();
            gcCount += garbageCollector.getCollectionCount();
            String gcAlgorithm = garbageCollector.getName();

            if (youngGcAlgorithm.contains(gcAlgorithm)) {
                youngGcTime += garbageCollector.getCollectionTime();
                youngGcCount += garbageCollector.getCollectionCount();
            } else if (oldGcAlgorithm.contains(gcAlgorithm)) {
                oldGcTime += garbageCollector.getCollectionTime();
                oldGCount += garbageCollector.getCollectionCount();
            } else {
                Cat.logEvent("UnknownGcAlgorithm", gcAlgorithm);
            }
        }

        map.put("jvm.gc.count", gcCount - lastGcCount);
        map.put("jvm.gc.time", gcTime - lastGcTime);
        final long value = oldGCount - lastFullGcCount;

        if (value > 0) {
            hasOldGc = true;
        }

        map.put("jvm.fullgc.count", value);
        map.put("jvm.fullgc.time", oldGcTime - lastFullGcTime);
        map.put("jvm.younggc.count", youngGcCount - lastYoungGcCount);
        map.put("jvm.younggc.time", youngGcTime - lastYoungGcTime);

        if (youngGcCount > lastYoungGcCount) {
            map.put("jvm.younggc.meantime", (youngGcTime - lastYoungGcTime) / (youngGcCount - lastYoungGcCount));
        } else {
            map.put("jvm.younggc.meantime", 0);
        }

        lastGcCount = gcCount;
        lastGcTime = gcTime;
        lastYoungGcCount = youngGcCount;
        lastYoungGcTime = youngGcTime;
        lastFullGcCount = oldGCount;
        lastFullGcTime = oldGcTime;

        return map;
    }

    private Map<String, Number> doMemoryCollect() {
        MemoryInformation memInfo = new MemoryInformation();
        Map<String, Number> map = new LinkedHashMap<String, Number>();

        map.put("jvm.memory.used", memInfo.getUsedMemory());
        map.put("jvm.memory.used.percent", memInfo.getUsedMemoryPercentage());
        map.put("jvm.memory.nonheap.used", memInfo.getUsedNonHeapMemory());
        map.put("jvm.memory.nonheap.used.percent", memInfo.getUsedNonHeapPercentage());
        map.put("jvm.memory.oldgen.used", memInfo.getUsedOldGen());
        map.put("jvm.memory.oldgen.used.percent", memInfo.getUsedOldGenPercentage());

        if (hasOldGc) {
            map.put("jvm.memory.oldgen.used.percent.after.fullgc", memInfo.getUsedOldGenPercentage());
            hasOldGc = false;
        } else {
            map.put("jvm.memory.oldgen.used.percent.after.fullgc", 0);
        }

        map.put("jvm.memory.eden.used", memInfo.getUsedEdenSpace());
        map.put("jvm.memory.eden.used.percent", memInfo.getUsedEdenSpacePercentage());
        map.put("jvm.memory.survivor.used", memInfo.getUsedSurvivorSpace());
        map.put("jvm.memory.survivor.used.percent", memInfo.getUsedSurvivorSpacePercentage());
        map.put("jvm.memory.perm.used", memInfo.getUsedPermGen());
        map.put("jvm.memory.perm.used.percent", memInfo.getUsedPermGenPercentage());
        map.put("jvm.memory.metaspace.used", memInfo.getUsedMetaSpace());
        map.put("jvm.memory.metaspace.used.percent", memInfo.getUsedMetaSpacePercentage());
        map.put("jvm.memory.codecache.used", memInfo.getUsedCodeCache());
        map.put("jvm.memory.codecache.used.percent", memInfo.getUsedCodeCachePercentage());
        map.put("jvm.nio.directbuffer.used", memInfo.getUsedDirectBufferSize());
        map.put("jvm.nio.mapped.used", memInfo.getUsedMappedSize());

        return map;
    }

    public void registerJVMCollector() {
        final StatusExtensionRegister instance = StatusExtensionRegister.getInstance();

        instance.register(new AbstractCollector() {

            @Override
            public String getId() {
                return "jvm.gc";
            }

            @Override
            public Map<String, String> getProperties() {
                Map<String, Number> map = collector.doGcCollect();

                return convert(map);
            }
        });

        instance.register(new AbstractCollector() {


            @Override
            public String getId() {
                return "jvm.memory";
            }

            @Override
            public Map<String, String> getProperties() {
                Map<String, Number> map = collector.doMemoryCollect();

                return convert(map);
            }
        });
    }

}
