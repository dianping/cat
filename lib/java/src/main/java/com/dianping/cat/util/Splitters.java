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

import java.util.ArrayList;
import java.util.List;

public class Splitters {
    public static StringSplitter by(char delimiter) {
        return new StringSplitter(delimiter);
    }

    public static StringSplitter by(String delimiter) {
        return new StringSplitter(delimiter);
    }

    public static class StringSplitter {
        private char charDelimiter;
        private String stringDelimiter;
        private boolean trimmed;
        private boolean noEmptyItem;

        StringSplitter(char delimiter) {
            charDelimiter = delimiter;
        }

        StringSplitter(String delimiter) {
            stringDelimiter = delimiter;
        }

        List<String> doCharSplit(String str, List<String> list) {
            char delimiter = charDelimiter;
            int len = str.length();
            StringBuilder sb = new StringBuilder(len);

            for (int i = 0; i < len + 1; i++) {
                char ch = i == len ? delimiter : str.charAt(i);

                if (ch == delimiter) {
                    String item = sb.toString();

                    sb.setLength(0);

                    if (trimmed) {
                        item = item.trim();
                    }

                    if (noEmptyItem && item.length() == 0) {
                        continue;
                    }

                    list.add(item);
                } else {
                    sb.append(ch);
                }
            }

            return list;
        }

        List<String> doStringSplit(String source, List<String> list) {
            String delimiter = stringDelimiter;
            int len = delimiter.length();
            int offset = 0;
            int index = source.indexOf(delimiter, offset);

            while (true) {
                String part;

                if (index == -1) { // last part
                    part = source.substring(offset);
                } else {
                    part = source.substring(offset, index);
                }

                if (trimmed) {
                    part = part.trim();
                }

                if (!noEmptyItem || part.length() > 0) {
                    list.add(part);
                }

                if (index == -1) { // last part
                    break;
                } else {
                    offset = index + len;
                    index = source.indexOf(delimiter, offset);
                }
            }

            return list;
        }

        public StringSplitter noEmptyItem() {
            noEmptyItem = true;
            return this;
        }

        public List<String> split(String str) {
            return split(str, new ArrayList<String>());
        }

        public List<String> split(String str, List<String> list) {
            if (str == null) {
                return null;
            }

            if (charDelimiter > 0) {
                return doCharSplit(str, list);
            } else if (stringDelimiter != null) {
                return doStringSplit(str, list);
            }

            throw new UnsupportedOperationException();
        }

        public StringSplitter trim() {
            trimmed = true;
            return this;
        }
    }
}
