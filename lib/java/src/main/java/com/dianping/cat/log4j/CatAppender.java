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
package com.dianping.cat.log4j;

import com.dianping.cat.Cat;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

public class CatAppender extends AppenderSkeleton {

    @Override
    protected void append(LoggingEvent event) {
        Level level = event.getLevel();

        if (level.isGreaterOrEqual(Level.ERROR)) {
            logError(event);
        }
    }

    @Override
    public void close() {
    }

    private void logError(LoggingEvent event) {
        ThrowableInformation info = event.getThrowableInformation();

        if (info != null) {
            Throwable exception = info.getThrowable();
            Object message = event.getMessage();

            if (message != null) {
                Cat.logError(String.valueOf(message), exception);
            } else {
                Cat.logError(exception);
            }
        }
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
