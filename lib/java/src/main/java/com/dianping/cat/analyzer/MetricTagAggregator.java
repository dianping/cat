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

import com.dianping.cat.CatConstants;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MetricTagAggregator {
    private static final MetricTagAggregator instance = new MetricTagAggregator();
    public static int MAX_KEY_SIZE = 1000;
    private static final String OTHERS = "others";
    private static final String EMPTY = "empty";
    private volatile ConcurrentHashMap<String, Map<String, MetricTagItem>> metrics = new ConcurrentHashMap<String, Map<String, MetricTagItem>>();
    private ConcurrentHashMap<String, Integer> metricThresholds = new ConcurrentHashMap<String, Integer>();

    public static MetricTagAggregator getInstance() {
        return instance;
    }

    public void addCountMetric(String name, int quantity, Map<String, String> tags) {
        MetricTagItem metricTagItem = makeSureMetricExist(name, tags, metrics);
        addCountMetric(quantity, metricTagItem);
    }

    private void addCountMetric(int quantity, MetricTagItem metricTagItem) {
        metricTagItem.count.addAndGet(quantity);
    }

    public void addTimerMetric(String name, long durationInMillis, Map<String, String> tags) {
        MetricTagItem item = makeSureMetricExist(name, tags, metrics);

        addTimerMetric(durationInMillis, item);
    }

    private void addTimerMetric(long durationInMillis, MetricTagItem item) {
        item.count.incrementAndGet();
        item.sum.addAndGet(durationInMillis);

        if (item.slowThreshold > 0 && durationInMillis > item.slowThreshold) {
            item.slowCount.incrementAndGet();
        }
    }

    private void buildMetricMessage(Long time, Map<String, Map<String, MetricTagItem>> datas) {
        Transaction transaction = Cat.newTransaction(CatConstants.CAT_SYSTEM, this.getClass().getSimpleName());

        for (Entry<String, Map<String, MetricTagItem>> entry : datas.entrySet()) {
            String key = entry.getKey();
            Map<String, MetricTagItem> items = entry.getValue();

            for (Entry<String, MetricTagItem> tagItem : items.entrySet()) {
                String tagKey = tagItem.getKey();
                MetricTagItem item = tagItem.getValue();
                int count = item.getCount().get();
                long sum = item.getSum().get();

                if (EMPTY.equals(tagKey)) {
                    int slowCount = item.getSlowCount();

                    if (sum > 0) {
                        logMetric(key, "S,C", String.format("%s,%s", count, sum), time);
                    } else if (count > 0) {
                        logMetric(key, "C", String.valueOf(count), time);
                    }

                    if (slowCount > 0) {
                        logMetric(key + ".slowCount", "C", String.valueOf(item.getSlowCount()), time);
                    }
                } else {
                    if (count > 0) {
                        if (sum > 0) {
                            logMetric(key, "TD", String.format("%s,%s,%s", count, sum, tagKey), time);
                        } else {
                            logMetric(key, "TC", String.format("%s,%s,%s", count, count, tagKey), time);
                        }
                    }
                }
            }
        }

        MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

        tree.setDomain(getDomain());
        tree.setDiscardPrivate(false);

        transaction.setStatus(Transaction.SUCCESS);
        transaction.complete();
    }

    private MetricTagItem createMetricItem(String key) {
        MetricTagItem item = new MetricTagItem();
        item.setKey(key);
        Integer threshold = metricThresholds.get(key);

        if (threshold != null) {
            item.setSlowThreshold(threshold);
        }
        return item;
    }

    private String buildTagKey(Map<String, String> tags) {
        if (tags == null || tags.size() == 0) {
            return EMPTY;
        } else {
            StringBuilder sb = new StringBuilder();
            boolean first = true;

            for (Entry<String, String> tag : tags.entrySet()) {
                if (!first) {
                    sb.append('&');
                } else {
                    first = false;
                }
                sb.append(tag.getKey()).append('=').append(tag.getValue());
            }
            return sb.toString();
        }
    }

    public void setMetricSlowThreshold(String key, int slow) {
        metricThresholds.put(key, slow);
    }

    private Map<String, Map<String, MetricTagItem>> getAndResetMetrics() {
        Map<String, Map<String, MetricTagItem>> cloned = metrics;

        synchronized (this) {
            metrics = new ConcurrentHashMap<String, Map<String, MetricTagItem>>();

            for (Entry<String, Map<String, MetricTagItem>> entry : cloned.entrySet()) {
                String key = entry.getKey();
                Map<String, MetricTagItem> items = entry.getValue();
                Map<String, MetricTagItem> newItem = new ConcurrentHashMap<String, MetricTagItem>();

                for (Entry<String, MetricTagItem> tagItem : items.entrySet()) {
                    if (tagItem.getValue().getCount().get() > 0) {
                        String itemKey = tagItem.getKey();
                        newItem.put(itemKey, createMetricItem(itemKey));
                    }
                }
                metrics.put(key, newItem);
            }
        }

        return cloned;
    }

    protected String getDomain() {
        return Cat.getManager().getDomain();
    }

    private void logMetric(String name, String status, String keyValuePairs, Long time) {
        try {
            Metric metric = Cat.getProducer().newMetric("", name);

            if (keyValuePairs != null && keyValuePairs.length() > 0) {
                metric.addData(keyValuePairs);
            }

            if (time != null) {
                metric.setTimestamp(time);
            }

            metric.setStatus(status);
            metric.complete();
        } catch (Exception e) {
            // ignore
        }
    }

    private MetricTagItem makeSureMetricExist(String key, Map<String, String> tags, Map<String, Map<String, MetricTagItem>> metrics) {
        Map<String, MetricTagItem> items = metrics.get(key);

        if (null == items) {
            synchronized (this) {
                items = metrics.get(key);
                if (null == items) {
                    items = new ConcurrentHashMap<String, MetricTagItem>();

                    metrics.put(key, items);
                }
            }
        }

        String tagKey = buildTagKey(tags);
        MetricTagItem item = items.get(tagKey);

        if (null == item) {
            if (items.size() >= MAX_KEY_SIZE) {
                Cat.logEvent("cat.TooManyTagValuesForMetric", key);
                tagKey = OTHERS;
            }

            item = items.get(tagKey);

            if (null == item) {
                synchronized (this) {
                    item = items.get(tagKey);

                    if (null == item) {
                        item = createMetricItem(tagKey);
                        items.put(tagKey, item);
                    }
                }
            }
        }

        return item;
    }

    void sendMetricTagData() {
        Map<String, Map<String, MetricTagItem>> items = getAndResetMetrics();
        sendMetricMessage(null, items);
    }

    private void sendMetricMessage(Long time, Map<String, Map<String, MetricTagItem>> items) {
        boolean hasData = false;

        for (Map<String, MetricTagItem> entry : items.values()) {
            for (Entry<String, MetricTagItem> item : entry.entrySet()) {
                if (item.getValue().getCount().get() > 0) {
                    hasData = true;
                    break;
                }
            }
        }

        if (hasData) {
            buildMetricMessage(time, items);
        }
    }

    public class MetricTagItem {

        private String key;

        private int slowThreshold;

        private AtomicInteger count = new AtomicInteger();

        private AtomicInteger slowCount = new AtomicInteger();

        private AtomicLong sum = new AtomicLong();

        AtomicInteger getCount() {
            return count;
        }

        String getKey() {
            return key;
        }

        AtomicLong getSum() {
            return sum;
        }

        int getSlowCount() {
            return slowCount.get();
        }

        int getSlowThreshold() {
            return slowThreshold;
        }

        void setCount(AtomicInteger count) {
            this.count = count;
        }

        void setKey(String key) {
            this.key = key;
        }

        void setSlowThreshold(int slowThreshold) {
            this.slowThreshold = slowThreshold;
        }

    }

}
