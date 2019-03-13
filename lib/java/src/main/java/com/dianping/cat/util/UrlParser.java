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
package com.dianping.cat.util;

public class UrlParser {
    private static final char SPLIT = '/';

    public static String format(String url) {
        int length = url.length();
        StringBuilder sb = new StringBuilder(length);

        for (int index = 0; index < length; ) {
            char c = url.charAt(index);

            if (c == SPLIT && index < length - 1) {
                sb.append(c);

                StringBuilder nextSection = new StringBuilder();
                boolean isNumber = false;
                boolean first = true;

                for (int j = index + 1; j < length; j++) {
                    char next = url.charAt(j);

                    if ((first || isNumber) && next != SPLIT) {
                        isNumber = isNumber(next);
                        first = false;
                    }

                    if (next == SPLIT) {
                        if (isNumber) {
                            sb.append("{num}");
                        } else {
                            sb.append(nextSection.toString());
                        }
                        index = j;

                        break;
                    } else if (j == length - 1) {
                        if (isNumber) {
                            sb.append("{num}");
                        } else {
                            nextSection.append(next);
                            sb.append(nextSection.toString());
                        }
                        index = j + 1;
                        break;
                    } else {
                        nextSection.append(next);
                    }
                }
            } else {
                sb.append(c);
                index++;
            }
        }

        return sb.toString();
    }

    private static boolean isNumber(char c) {
        return (c >= '0' && c <= '9') || c == '.' || c == '-' || c == ',';
    }
}