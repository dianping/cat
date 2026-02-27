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

import com.dianping.cat.configuration.ApplicationEnvironment;
import com.dianping.cat.status.AbstractCollector;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import java.util.Map;

public class StaticInfoCollector extends AbstractCollector {
    private String jars;
    private static final String CLASSPATH = "system.java.classpath";

    private void buildClasspath() {
        ClassLoader loader = StaticInfoCollector.class.getClassLoader();
        StringBuilder sb = new StringBuilder();

        buildClasspath(loader, sb);

        if (sb.length() > 0) {
            jars = sb.substring(0, sb.length() - 1);
        }
    }

    private void buildClasspath(ClassLoader loader, StringBuilder sb) {
        if (loader instanceof URLClassLoader) {
            URL[] urLs = ((URLClassLoader) loader).getURLs();
            for (URL url : urLs) {
                String jar = parseJar(url.toExternalForm());

                if (jar != null) {
                    sb.append(jar).append(',');
                }
            }
            ClassLoader parent = loader.getParent();

            buildClasspath(parent, sb);
        }
    }

    @Override
    public String getId() {
        return "system.static";
    }

    @Override
    public Map<String, String> getProperties() {
        if (jars == null) {
            buildClasspath();
        }

        Map<String, String> map = new LinkedHashMap<String, String>();

        map.put(CLASSPATH, jars);
        map.put("system.java.version", System.getProperty("java.version"));
        map.put("system.user.name", System.getProperty("user.name"));
        map.put("java.cat.version", ApplicationEnvironment.VERSION);

        return map;
    }

    private String parseJar(String path) {
        if (path.endsWith(".jar")) {
            int index = path.lastIndexOf('/');

            if (index > -1) {
                return path.substring(index + 1);
            }
        }
        return null;
    }
}
