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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.Message;

import java.io.Serializable;

@Plugin(name = "CatAppender", category = "Core", elementType = "appender", printObject = true)
public class Log4j2Appender extends AbstractAppender {

    private static final long serialVersionUID = 2705802038361151598L;

    private Log4j2Appender(String name, Filter filter, Layout<? extends Serializable> layout,
                           final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
    }

    @Override
    public void append(LogEvent event) {
        try {
            Level level = event.getLevel();

            if (level.isMoreSpecificThan(Level.ERROR)) {
                logError(event);
            }
        } catch (Exception ex) {
            if (!ignoreExceptions()) {
                throw new AppenderLoggingException(ex);
            }
        }
    }

    @PluginFactory
    public static Log4j2Appender createAppender(@PluginAttribute("name") String name,
                                                @PluginElement("Layout") Layout<? extends Serializable> layout, @PluginElement("Filter") final Filter filter,
                                                @PluginAttribute("otherAttribute") String otherAttribute) {
        if (name == null) {
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new Log4j2Appender(name, filter, layout, true);
    }

    private void logError(LogEvent event) {
        ThrowableProxy info = event.getThrownProxy();

        if (info != null) {
            Throwable exception = info.getThrowable();
            Message message = event.getMessage();

            if (message != null) {
                Cat.logError(message.getFormattedMessage(), exception);
            } else {
                Cat.logError(exception);
            }
        }
    }

}