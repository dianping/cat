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

import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum NetworkInterfaceManager {
    INSTANCE;

    private InetAddress local;
    private String ip;
    private String hostName;
    public final static String LOOPBACK = "127.0.0.1";

    NetworkInterfaceManager() {
        System.setProperty("java.net.preferIPv4Stack", "true");
        load();

        ip = local.getHostAddress().intern();

        String hostname;

        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostname = local.getHostName();
        }

        if (hostname == null) {
            hostname = "unknown";
        } else if (hostname.contains(".sankuai.com") || hostname.contains(".office.mos")) {
            hostname = hostname.substring(0, hostname.indexOf("."));
        }

        hostName = hostname;
    }

    public InetAddress findValidateIp(List<InetAddress> addresses) {
        InetAddress local = null;
        for (InetAddress address : addresses) {
            if (address instanceof Inet4Address) {
                if (address.isLoopbackAddress() || address.isSiteLocalAddress()) {
                    if (local == null) {
                        local = address;
                    } else if (address.isSiteLocalAddress() && !address.isLoopbackAddress()) {
                        // site local address has higher priority than other address
                        local = address;
                    } else if (local.isSiteLocalAddress() && address.isSiteLocalAddress()) {
                        // site local address with a host name has higher
                        // priority than one without host name
                        if (local.getHostName().equals(local.getHostAddress())
                                && !address.getHostName().equals(address.getHostAddress())) {
                            local = address;
                        }
                    }
                } else {
                    if (local == null || local.isLoopbackAddress()) {
                        local = address;
                    }
                }
            }
        }
        return local;
    }

    public String getLocalHostAddress() {
        return ip;
    }

    public String getLocalHostName() {
        return hostName;
    }

    private String getProperty(String name) {
        String value = System.getProperty(name);

        if (value == null) {
            value = System.getenv(name);
        }

        return value;
    }

    private void load() {
        String ip = getProperty("host.ip");

        if (ip != null) {
            try {
                local = InetAddress.getByName(ip);
                return;
            } catch (Exception ignored) {
                // ignore
            }
        }

        try {
            List<NetworkInterface> nis = Collections.list(NetworkInterface.getNetworkInterfaces());
            List<InetAddress> addresses = new ArrayList<InetAddress>();
            InetAddress local = null;

            try {
                for (NetworkInterface ni : nis) {
                    if (ni.isUp()) {
                        addresses.addAll(Collections.list(ni.getInetAddresses()));
                    }
                }
                local = findValidateIp(addresses);
            } catch (Exception e) {
                // ignore
            }
            this.local = local;
        } catch (SocketException e) {
            // ignore
        }
    }
}
