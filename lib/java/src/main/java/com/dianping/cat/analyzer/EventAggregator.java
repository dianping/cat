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
package com.dianping.cat.analyzer;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class EventAggregator {
    private static EventAggregator instance = new EventAggregator();
    private volatile ConcurrentHashMap<String, ConcurrentHashMap<String, EventData>> events = new ConcurrentHashMap<String, ConcurrentHashMap<String, EventData>>();

    public static EventAggregator getInstance() {
        return instance;
    }

    private EventData createEventData(String type, String name) {
        return new EventData(type, name);
    }

    private ConcurrentHashMap<String, ConcurrentHashMap<String, EventData>> getAndResetEvents() {
        ConcurrentHashMap<String, ConcurrentHashMap<String, EventData>> cloned = events;

        events = new ConcurrentHashMap<String, ConcurrentHashMap<String, EventData>>();

        for (Map.Entry<String, ConcurrentHashMap<String, EventData>> entry : cloned.entrySet()) {
            String type = entry.getKey();

            events.putIfAbsent(type, new ConcurrentHashMap<String, EventData>());
        }

        return cloned;
    }

    public String getDomain() {
        return Cat.getManager().getDomain();
    }

    public void logBatchEvent(String type, String name, int total, int fail) {
        makeSureEventExist(type, name).add(total, fail);
    }

    public void logEvent(Event e) {
        makeSureEventExist(e.getType(), e.getName()).add(e);
    }

    private EventData makeSureEventExist(String type, String name) {
        ConcurrentHashMap<String, EventData> item = events.get(type);

        if (null == item) {
            item = new ConcurrentHashMap<String, EventData>();

            ConcurrentHashMap<String, EventData> oldValue = events.putIfAbsent(type, item);

            if (oldValue != null) {
                item = oldValue;
            }
        }

        EventData data = item.get(name);

        if (null == data) {
            data = createEventData(type, name);

            EventData oldValue = item.putIfAbsent(name, data);

            if (oldValue == null) {
                return data;
            } else {
                return oldValue;
            }
        }

        return data;
    }

    void sendEventData() {
        ConcurrentHashMap<String, ConcurrentHashMap<String, EventData>> events = getAndResetEvents();
        boolean hasData = false;

        for (Map<String, EventData> entry : events.values()) {
            for (EventData data : entry.values()) {
                if (data.getCount() > 0) {
                    hasData = true;
                    break;
                }
            }
        }

        if (hasData) {
            Transaction t = Cat.newTransaction(CatConstants.CAT_SYSTEM, this.getClass().getSimpleName());
            MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

            tree.setDomain(getDomain());
            tree.setDiscardPrivate(false);

            for (Map<String, EventData> entry : events.values()) {
                for (EventData data : entry.values()) {
                    if (data.getCount() > 0) {
                        Event tmp = Cat.newEvent(data.getType(), data.getName());
                        StringBuilder sb = new StringBuilder(32);

                        sb.append(CatConstants.BATCH_FLAG).append(data.getCount()).append(CatConstants.SPLIT)
                                .append(data.getError());
                        tmp.addData(sb.toString());
                        tmp.setSuccessStatus();
                        tmp.complete();
                    }
                }
            }

            t.setSuccessStatus();
            t.complete();
        }
    }

    public class EventData {

        private String type;

        private String name;

        private AtomicInteger count = new AtomicInteger();

        private AtomicInteger error = new AtomicInteger();

        EventData(String type, String name) {
            this.type = type;
            this.name = name;
        }

        public EventData add(Event e) {
            count.incrementAndGet();

            if (!e.isSuccess()) {
                error.incrementAndGet();
            }
            return this;
        }

        public EventData add(int count, int fail) {
            this.count.addAndGet(count);
            error.addAndGet(fail);
            return this;
        }

        public int getCount() {
            return count.get();
        }

        public int getError() {
            return error.get();
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }

}
