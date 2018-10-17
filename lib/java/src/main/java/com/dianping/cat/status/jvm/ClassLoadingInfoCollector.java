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
