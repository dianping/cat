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

import com.dianping.cat.Cat;
import com.dianping.cat.message.*;
import com.dianping.cat.message.spi.MessageManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultMessageProducer implements MessageProducer {
    private MessageManager manager = DefaultMessageManager.getInstance();
    private MessageIdFactory factory = MessageIdFactory.getInstance();
    private static ConcurrentMap<Long, ConcurrentHashMap<String, AtomicInteger>> stack = new ConcurrentHashMap<Long, ConcurrentHashMap<String, AtomicInteger>>();
    private static final String ERROR = "ERROR";
    private static MessageProducer INSTANCE = new DefaultMessageProducer();

    public static void clearCache() {
        long time = System.currentTimeMillis() / 1000 / 60 - 3;
        Set<Long> removeKeys = new HashSet<Long>();

        for (Long l : stack.keySet()) {
            if (l <= time) {
                removeKeys.add(l);
            }
        }

        for (Long l : removeKeys) {
            stack.remove(l);
        }
    }

    public static MessageProducer getInstance() {
        return INSTANCE;
    }

    private String buildStackInfo(String message, Throwable cause) {
        StringWriter writer = new StringWriter(2048);

        if (message != null) {
            writer.write(message);
            writer.write(' ');
        }

        if (recordStackTrace(cause.getClass().getName())) {
            // when build stack, cat will report the message tree.
            Cat.getManager().getThreadLocalMessageTree().setDiscardPrivate(false);

            cause.printStackTrace(new PrintWriter(writer));
        } else {
            writer.write("The exception with same name will print stack eighty times in one minute, discard exception stack to avoid abnormal performance bottleneck");
        }

        return writer.toString();
    }

    @Override
    public String createMessageId() {
        return factory.getNextId();
    }

    @Override
    public String createRpcServerId(String domain) {
        return factory.getNextId(domain);
    }

    @Override
    public void logError(String message, Throwable cause) {
        if (notExsitCause(cause)) {
            String detailMessage = buildStackInfo(message, cause);
            final String name = cause.getClass().getName();

            if (cause instanceof Error) {
                logEvent("Error", name, ERROR, detailMessage);
            } else if (cause instanceof RuntimeException) {
                logEvent("RuntimeException", name, ERROR, detailMessage);
            } else {
                logEvent("Exception", name, ERROR, detailMessage);
            }
        }
    }

    @Override
    public void logError(Throwable cause) {
        logError(null, cause);
    }

    @Override
    public void logErrorWithCategory(String category, String message, Throwable cause) {
        if (notExsitCause(cause)) {
            String detailMessage = buildStackInfo(message, cause);

            if (cause instanceof Error) {
                logEvent("Error", category, ERROR, detailMessage);
            } else if (cause instanceof RuntimeException) {
                logEvent("RuntimeException", category, ERROR, detailMessage);
            } else {
                logEvent("Exception", category, ERROR, detailMessage);
            }
        }
    }

    @Override
    public void logErrorWithCategory(String category, Throwable cause) {
        logErrorWithCategory(category, null, cause);
    }

    @Override
    public void logEvent(String type, String name) {
        logEvent(type, name, Message.SUCCESS, null);
    }

    @Override
    public void logEvent(String type, String name, String status, String nameValuePairs) {
        Event event = newEvent(type, name);

        if (nameValuePairs != null && nameValuePairs.length() > 0) {
            event.addData(nameValuePairs);
        }

        event.setStatus(status);
        event.complete();
    }

    @Override
    public void logHeartbeat(String type, String name, String status, String nameValuePairs) {
        Heartbeat heartbeat = newHeartbeat(type, name);

        heartbeat.addData(nameValuePairs);
        heartbeat.setStatus(status);
        heartbeat.complete();
    }

    @Override
    public void logMetric(String name, String status, String nameValuePairs) {
        String type = "";
        Metric metric = newMetric(type, name);

        if (nameValuePairs != null && nameValuePairs.length() > 0) {
            metric.addData(nameValuePairs);
        }

        metric.setStatus(status);
        metric.complete();
    }

    @Override
    public Event newEvent(String type, String name) {
        if (!manager.hasContext()) {
            manager.setup();
        }

        return new DefaultEvent(type, name, manager);
    }

    @Override
    public Heartbeat newHeartbeat(String type, String name) {
        if (!manager.hasContext()) {
            manager.setup();
        }

        DefaultHeartbeat heartbeat = new DefaultHeartbeat(type, name, manager);

        manager.getThreadLocalMessageTree().setDiscardPrivate(false);
        return heartbeat;
    }

    @Override
    public Metric newMetric(String type, String name) {
        if (!manager.hasContext()) {
            manager.setup();
        }

        DefaultMetric metric = new DefaultMetric(type == null ? "" : type, name, manager);

        manager.getThreadLocalMessageTree().setDiscardPrivate(false);
        return metric;
    }

    @Override
    public Trace newTrace(String type, String name) {
        if (!manager.hasContext()) {
            manager.setup();
        }

        return new DefaultTrace(type, name, manager);
    }

    @Override
    public Transaction newTransaction(String type, String name) {
        // this enable CAT client logging cat message without explicit setup
        if (!manager.hasContext()) {
            manager.setup();
        }

        DefaultTransaction transaction = new DefaultTransaction(type, name, manager);

        manager.start(transaction, false);
        return transaction;
    }

    private boolean notExsitCause(Throwable e) {
        if (manager instanceof DefaultMessageManager) {
            return ((DefaultMessageManager) manager).notExsitCause(e);
        } else {
            return true;
        }
    }

    private boolean recordStackTrace(String exception) {
        try {
            long minute = System.currentTimeMillis() / 1000 / 60;
            ConcurrentHashMap<String, AtomicInteger> stack = DefaultMessageProducer.stack.get(minute);

            if (stack == null) {
                stack = new ConcurrentHashMap<String, AtomicInteger>();

                ConcurrentHashMap<String, AtomicInteger> oldStack = DefaultMessageProducer.stack.putIfAbsent(minute, stack);

                if (oldStack != null) {
                    stack = oldStack;
                }
            }

            AtomicInteger value = stack.get(exception);

            if (value == null) {
                value = new AtomicInteger(0);

                AtomicInteger oldValue = stack.putIfAbsent(exception, value);

                if (oldValue != null) {
                    value = oldValue;
                }
            }

            return value.incrementAndGet() <= 80;
        } catch (Exception e) {
            return true;
        }
    }
}
