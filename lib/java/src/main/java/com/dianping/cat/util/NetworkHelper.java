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

import com.dianping.cat.log.CatLogger;

import java.io.InputStream;

public class NetworkHelper {

    private static final int TIMEOUT = 2000;

    public static String readFromUrlWithRetry(String url) throws Exception {
        try {
            InputStream input = Urls.forIO().readTimeout(TIMEOUT).connectTimeout(TIMEOUT).openStream(url);

            return Files.forIO().readFrom(input, "utf-8");
        } catch (Exception e) {
            try {
                InputStream in = Urls.forIO().connectTimeout(TIMEOUT).readTimeout(TIMEOUT).openStream(url);

                return Files.forIO().readFrom(in, "utf-8");
            } catch (Exception retryException) {
                CatLogger.getInstance().error("error when read url:" + url + ",exception is " + retryException.getMessage());

                throw retryException;
            }
        }

    }
}
