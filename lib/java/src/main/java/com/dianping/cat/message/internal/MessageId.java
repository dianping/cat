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
package com.dianping.cat.message.internal;

import com.dianping.cat.util.StringUtils;

public class MessageId {
    private String domain;
    private String ipAddressInHex;
    private int hour;
    private int index;

    public static MessageId parse(String messageId) {
        int index = -1;
        int hour = -1;
        String ipAddressInHex = null;
        String domain = null;
        int len = messageId.length();
        int part = 4;
        int end = len;

        for (int i = len - 1; i >= 0; i--) {
            char ch = messageId.charAt(i);

            if (ch == '-') {
                switch (part) {
                    case 4:
                        index = Integer.parseInt(messageId.substring(i + 1, end));
                        end = i;
                        part--;
                        break;
                    case 3:
                        hour = Integer.parseInt(messageId.substring(i + 1, end));
                        end = i;
                        part--;
                        break;
                    case 2:
                        ipAddressInHex = messageId.substring(i + 1, end);
                        domain = messageId.substring(0, i);
                        part--;
                        break;
                    default:
                        break;
                }
            }
        }

        if (domain == null || hour < 0 || index < 0) {
            throw new RuntimeException("Invalid message ID format: " + messageId);
        } else {
            return new MessageId(domain, ipAddressInHex, hour, index);
        }
    }

    public MessageId(String domain, String ipAddressInHex, int hour, int index) {
        this.domain = domain;
        this.ipAddressInHex = ipAddressInHex;
        this.hour = hour;
        this.index = index;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MessageId) {
            MessageId o = (MessageId) obj;

            if (!domain.equals(o.domain)) {
                return false;
            }

            if (!ipAddressInHex.equals(o.ipAddressInHex)) {
                return false;
            }

            if (hour != o.hour) {
                return false;
            }

            return index == o.index;
        }

        return false;
    }

    public String getDomain() {
        return domain;
    }

    public int getHour() {
        return hour;
    }

    public int getIndex() {
        return index;
    }

    public String getIpAddress() {
        StringBuilder sb = new StringBuilder();
        String local = ipAddressInHex;
        int index = local.lastIndexOf(".");

        if (index > 0) {
            local = local.substring(0, index);
        }

        int length = local.length();

        for (int i = 0; i < length; i += 2) {
            char ch1 = local.charAt(i);
            char ch2 = local.charAt(i + 1);
            int value = 0;

            if (ch1 >= '0' && ch1 <= '9') {
                value += (ch1 - '0') << 4;
            } else {
                value += ((ch1 - 'a') + 10) << 4;
            }

            if (ch2 >= '0' && ch2 <= '9') {
                value += ch2 - '0';
            } else {
                value += (ch2 - 'a') + 10;
            }

            if (sb.length() > 0) {
                sb.append('.');
            }

            sb.append(value);
        }

        return sb.toString();
    }

    public String getIpAddressInHex() {
        return ipAddressInHex;
    }

    public int getIpAddressValue() {
        String local = ipAddressInHex;
        int length = local.length();

        if (length > 8) {
            int index = ipAddressInHex.lastIndexOf(".");

            if (index < 0) {
                return getIpHexValue(local);
            } else {
                local = ipAddressInHex.substring(0, index);
                String pidStr = ipAddressInHex.substring(index + 1);

                if (StringUtils.isEmpty(pidStr)) {
                    return getIpHexValue(local);
                }

                if (pidStr.length() > 5) {
                    pidStr = pidStr.substring(0, 5);
                }
                int pid = Integer.parseInt(pidStr);

                return (pid << 17) ^ (getIpHexValue(local));
            }
        }

        return getIpHexValue(local);
    }

    private int getIpHexValue(String ipHex) {
        int ip = 0;
        int length = ipHex.length();

        for (int i = 0; i < length; i += 2) {
            char ch1 = ipHex.charAt(i);
            char ch2 = ipHex.charAt(i + 1);
            int value = 0;

            if (ch1 >= '0' && ch1 <= '9') {
                value += (ch1 - '0') << 4;
            } else {
                value += ((ch1 - 'a') + 10) << 4;
            }

            if (ch2 >= '0' && ch2 <= '9') {
                value += ch2 - '0';
            } else {
                value += (ch2 - 'a') + 10;
            }

            ip = (ip << 8) + value;
        }

        return ip;
    }

    public long getTimestamp() {
        return hour * 3600 * 1000L;
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + ((domain == null) ? 0 : domain.hashCode());
        result = 31 * result + ((ipAddressInHex == null) ? 0 : ipAddressInHex.hashCode());
        result = 31 * result + hour;
        result = 31 * result + index;

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(domain.length() + 30);

        sb.append(domain);
        sb.append('-');
        sb.append(ipAddressInHex);
        sb.append('-');
        sb.append(hour);
        sb.append('-');
        sb.append(index);

        return sb.toString();
    }

}
