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
package com.dianping.cat.status.datasource;

import com.dianping.cat.Cat;
import com.dianping.cat.util.StringUtils;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

public class DatabaseParserHelper {
    private Map<String, Database> connections = new LinkedHashMap<String, Database>();

    private String find(String con, String key) {
        int index = con.indexOf(key);
        int start = 0;
        int end = 0;
        if (index > -1) {
            for (int i = index + key.length(); i < con.length(); i++) {
                if (con.charAt(i) == '=') {
                    start = i + 1;
                }
                if (con.charAt(i) == ')') {
                    end = i;
                    break;
                }
            }
        }
        return con.substring(start, end);
    }

    public Database parseDatabase(String connection) {
        Database database = connections.get(String.valueOf(connection));

        if (database == null && StringUtils.isNotEmpty(connection)) {
            try {
                if (connection.contains("jdbc:mysql://")) {
                    String con = connection.split("jdbc:mysql://")[1];
                    con = con.split("\\?")[0];
                    int index = con.indexOf(":");
                    String ip = "";

                    if (index < 0) {
                        ip = con.split("/")[0];
                    } else {
                        ip = con.substring(0, index);
                    }

                    String name = con.substring(con.indexOf("/") + 1);
                    database = new Database(name, ip);

                    connections.put(connection, database);
                } else if (connection.contains("jdbc:oracle")) {
                    if (connection.contains("DESCRIPTION")) {
                        String name = find(connection, "SERVICE_NAME");
                        String ip = find(connection, "HOST");

                        database = new Database(name, ip);
                        connections.put(connection, database);
                    } else if (connection.contains("@//")) {
                        String[] tabs = connection.split("/");
                        String name = tabs[tabs.length - 1];
                        String ip = tabs[tabs.length - 2];
                        int index = ip.indexOf(':');

                        if (index > -1) {
                            ip = ip.substring(0, index);
                        }
                        database = new Database(name, ip);
                        connections.put(connection, database);
                    } else {
                        String[] tabs = connection.split(":");
                        String ip = "Default";

                        for (String str : tabs) {
                            int index = str.indexOf("@");

                            if (index > -1) {
                                ip = str.substring(index + 1).trim();
                            }
                        }
                        String name = tabs[tabs.length - 1];
                        int index = name.indexOf('/');

                        if (index > -1) {
                            name = name.substring(index + 1);
                        }

                        database = new Database(name, ip);

                        connections.put(connection, database);
                    }
                } else {
                    return new Database("default", "default");
                }
            } catch (Exception e) {
                Cat.logError(connection, e);
            }
        }
        return database;
    }

    @Data
    public class Database {
        private String name;
        private String ip;

        Database(String name, String ip) {
            this.name = name;
            this.ip = ip;
        }

        public String toString() {
            return name + '_' + ip;
        }
    }
}
