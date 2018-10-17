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
package com.dianping.cat.configuration;

import com.dianping.cat.log.CatLogger;
import com.dianping.cat.util.Files;
import com.dianping.cat.util.NetworkHelper;
import com.dianping.cat.util.Splitters;
import com.dianping.cat.util.StringUtils;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultSaxParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Properties;

public class ApplicationEnvironment {
    private static final String HOST = "org.cat";
    private static final String PROPERTIES_FILE = "/META-INF/app.properties";
    private static final String CACHE_FILE = "client_cache.xml";
    private static final String CLIENT_FILE = "client.xml";
    public static final String ENVIRONMENT;
    public static final String CELL;
    public static final String VERSION = "2.0.1";

    static {
        String env;
        String cell;

        try {
            String file = "/data/webapps/appenv";
            Properties pro = new Properties();

            pro.load(new FileInputStream(new File(file)));
            env = pro.getProperty("env");

            if (StringUtils.isEmpty(env)) {
                env = pro.getProperty("deployenv");
            }

            cell = pro.getProperty("cell");
        } catch (Exception e) {
            env = Cat.UNKNOWN;
            cell = "";
        }

        if (env == null) {
            env = Cat.UNKNOWN;
        }

        if (cell == null) {
            cell = "";
        }

        ENVIRONMENT = env.trim();
        CELL = cell.trim();
    }

    private static boolean isDevMode() {
        String devMode = com.dianping.cat.util.Properties.forString().fromEnv().fromSystem().getProperty("devMode", "false");

        return "true".equals(devMode);
    }

    public static String loadAppName(String defaultDomain) {
        String appName = null;
        InputStream in = null;

        try {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE);

            if (in == null) {
                in = Cat.class.getResourceAsStream(PROPERTIES_FILE);
            }
            if (in != null) {
                Properties prop = new Properties();

                prop.load(in);

                appName = prop.getProperty("app.name");

                if (appName != null) {
                    return appName;
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignored) {
                }
            }
        }
        return defaultDomain;
    }

    public static ClientConfig loadClientConfig(String domain) {
        String xml = null;

        try {
            File cacheFile = new File(Cat.getCatHome() + CACHE_FILE);
            File configFile = new File(Cat.getCatHome() + CLIENT_FILE);

            if (cacheFile.exists() && !isDevMode()) {
                xml = Files.forIO().readFrom(cacheFile, "utf-8");
            } else if (configFile.exists()) {
                xml = Files.forIO().readFrom(configFile, "utf-8");
            } else {
                xml = ApplicationEnvironment.loadRemoteClientConfig();
            }

            ClientConfig config = DefaultSaxParser.parse(xml);

            config.setDomain(domain);

            return config;
        } catch (Exception e) {
            CatLogger.getInstance().info("load client config error: " + xml, e);

            File cacheFile = new File(Cat.getCatHome() + CACHE_FILE);

            if (cacheFile.exists()) {
                cacheFile.delete();

                return loadClientConfig(domain);
            }

            throw new RuntimeException("Error when get cat router service, please contact cat support team for help!", e);
        }
    }

    public static String loadRemoteClientConfig() throws Exception {
        String host = com.dianping.cat.util.Properties.forString().fromEnv().fromSystem().getProperty("CAT_HOST", HOST);
        String path = String.format("http://%s/cat/s/launch", host);
        String hostName = NetworkInterfaceManager.INSTANCE.getLocalHostName();

        try {
            hostName = URLEncoder.encode(hostName, "utf-8");
        } catch (UnsupportedEncodingException ignored) {
        }

        String url = path + "?ip=" + NetworkInterfaceManager.INSTANCE.getLocalHostAddress() + "&env=" + ENVIRONMENT
                + "&hostname=" + hostName;

        return NetworkHelper.readFromUrlWithRetry(url);
    }

    public static void storeServers(String servers, int httpPort) {
        try {
            ClientConfig config = new ClientConfig();
            List<String> strs = Splitters.by(";").noEmptyItem().split(servers);

            for (String str : strs) {
                List<String> items = Splitters.by(":").noEmptyItem().split(str);

                config.addServer(new Server().setIp(items.get(0)).setHttpPort(httpPort).setPort(Integer.parseInt(items.get(1))));
            }

            config.setDomain(null);

            Files.forIO().writeTo(new File(Cat.getCatHome() + CACHE_FILE), config.toString());
        } catch (Exception e) {
            // ignore
        }
    }

}
