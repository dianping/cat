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

import java.util.Arrays;
import java.util.Collection;

public class Joiners {
    public static StringJoiner by(final char delimiter) {
        return new StringJoiner() {
            @Override
            protected void appendDelimiter(StringBuilder sb) {
                sb.append(delimiter);
            }
        };
    }

    public static StringJoiner by(final String delimiter) {
        return new StringJoiner() {
            @Override
            protected void appendDelimiter(StringBuilder sb) {
                sb.append(delimiter);
            }
        };
    }

    public interface IBuilder<T> {
        String asString(T item);
    }

    public static abstract class StringJoiner {
        private boolean prefixDelimiter;

        protected abstract void appendDelimiter(StringBuilder sb);

        public String join(Collection<String> list) {
            return this.<String>join(list, null);
        }

        public <T> String join(Collection<T> list, IBuilder<T> builder) {
            if (list == null) {
                return null;
            }

            StringBuilder sb = new StringBuilder();

            join(sb, list, builder);

            return sb.toString();
        }

        public String join(String... array) {
            return join(Arrays.asList(array), null);
        }

        @SuppressWarnings("unchecked")
        public <T> String join(IBuilder<T> builder, T... array) {
            return join(Arrays.asList(array), builder);
        }

        public <T> void join(StringBuilder sb, Collection<T> list, IBuilder<T> builder) {
            boolean first = true;

            if (list != null) {
                for (T item : list) {
                    if (first) {
                        first = false;

                        if (prefixDelimiter) {
                            appendDelimiter(sb);
                        }
                    } else {
                        appendDelimiter(sb);
                    }

                    if (builder == null) {
                        sb.append(item);
                    } else {
                        sb.append(builder.asString(item));
                    }
                }
            }
        }

        public StringJoiner prefixDelimiter() {
            prefixDelimiter = true;
            return this;
        }
    }
}
