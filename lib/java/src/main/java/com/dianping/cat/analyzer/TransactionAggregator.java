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
import com.dianping.cat.configuration.ClientConfigService;
import com.dianping.cat.configuration.ProblemLongType;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TransactionAggregator {
    private static TransactionAggregator instance = new TransactionAggregator();
    private volatile ConcurrentHashMap<String, ConcurrentHashMap<String, TransactionData>> transactions = new ConcurrentHashMap<String, ConcurrentHashMap<String, TransactionData>>();

    public static TransactionAggregator getInstance() {
        return instance;
    }

    private TransactionData createTransactionData(String type, String name) {
        return new TransactionData(type, name);
    }

    private ConcurrentHashMap<String, ConcurrentHashMap<String, TransactionData>> getAndResetTransactions() {
        ConcurrentHashMap<String, ConcurrentHashMap<String, TransactionData>> cloned = transactions;

        transactions = new ConcurrentHashMap<String, ConcurrentHashMap<String, TransactionData>>();

        for (Entry<String, ConcurrentHashMap<String, TransactionData>> entry : cloned.entrySet()) {
            String type = entry.getKey();

            transactions.putIfAbsent(type, new ConcurrentHashMap<String, TransactionData>());
        }

        return cloned;
    }

    public String getDomain() {
        return Cat.getManager().getDomain();
    }

    public void logBatchTransaction(String type, String name, int count, int error, long sum) {
        makeSureTransactionExist(type, name).add(count, error, sum);
    }

    void logTransaction(Transaction t) {
        makeSureTransactionExist(t.getType(), t.getName()).add(t);
    }

    private TransactionData makeSureTransactionExist(String type, String name) {
        ConcurrentHashMap<String, TransactionData> item = transactions.get(type);

        if (null == item) {
            item = new ConcurrentHashMap<String, TransactionData>();

            ConcurrentHashMap<String, TransactionData> oldValue = transactions.putIfAbsent(type, item);

            if (oldValue != null) {
                item = oldValue;
            }
        }

        TransactionData data = item.get(name);

        if (null == data) {
            data = createTransactionData(type, name);

            TransactionData oldValue = item.putIfAbsent(name, data);

            if (oldValue == null) {
                return data;
            } else {
                return oldValue;
            }
        }

        return data;
    }

    void sendTransactionData() {
        ConcurrentHashMap<String, ConcurrentHashMap<String, TransactionData>> transactions = getAndResetTransactions();
        boolean hasData = false;

        for (Map<String, TransactionData> entry : transactions.values()) {
            for (TransactionData data : entry.values()) {
                if (data.getCount().get() > 0) {
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

            for (Map<String, TransactionData> entry : transactions.values()) {
                for (TransactionData data : entry.values()) {
                    if (data.getCount().get() > 0) {
                        Transaction tmp = Cat.newTransaction(data.getType(), data.getName());
                        StringBuilder sb = new StringBuilder(32);

                        sb.append(CatConstants.BATCH_FLAG).append(data.getCount().get()).append(CatConstants.SPLIT);
                        sb.append(data.getFail().get()).append(CatConstants.SPLIT);
                        sb.append(data.getSum().get()).append(CatConstants.SPLIT);
                        sb.append(data.getDurationString()).append(CatConstants.SPLIT).append(data.getLongDurationString());

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

    private int checkAndGetLongThreshold(String type, int duration) {
        ClientConfigService config = Cat.getManager().getConfigService();

        ProblemLongType longType = ProblemLongType.findByMessageType(type);

        if (longType != null) {
            switch (longType) {
                case LONG_CACHE:
                    return config.getLongThresholdByDuration(ProblemLongType.LONG_CACHE.getName(), duration);
                case LONG_CALL:
                    return config.getLongThresholdByDuration(ProblemLongType.LONG_CALL.getName(), duration);
                case LONG_SERVICE:
                    return config.getLongThresholdByDuration(ProblemLongType.LONG_SERVICE.getName(), duration);
                case LONG_SQL:
                    return config.getLongThresholdByDuration(ProblemLongType.LONG_SQL.getName(), duration);
                case LONG_URL:
                    return config.getLongThresholdByDuration(ProblemLongType.LONG_URL.getName(), duration);
                case LONG_MQ:
                    return config.getLongThresholdByDuration(ProblemLongType.LONG_MQ.getName(), duration);
            }
        }

        return -1;
    }

    public class TransactionData {

        private String type;
        private String name;
        private AtomicInteger count = new AtomicInteger();
        private AtomicInteger fail = new AtomicInteger();
        private AtomicLong sum = new AtomicLong();
        private ConcurrentHashMap<Integer, AtomicInteger> durations = new ConcurrentHashMap<Integer, AtomicInteger>();
        private ConcurrentHashMap<Integer, AtomicInteger> longDurations = new ConcurrentHashMap<Integer, AtomicInteger>();

        TransactionData(String type, String name) {
            this.type = type;
            this.name = name;
        }

        public TransactionData add(int count, int error, long sum) {
            this.count.addAndGet(count);
            this.sum.addAndGet(sum);
            fail.addAndGet(error);

            if (count == 1) {
                int duration = DurationComputer.computeDuration((int) sum);
                AtomicInteger durationCount = durations.get(duration);

                if (durationCount == null) {
                    durations.put(duration, new AtomicInteger(1));
                } else {
                    durationCount.incrementAndGet();
                }
            }

            return this;
        }

        public TransactionData add(Transaction t) {
            count.incrementAndGet();
            sum.getAndAdd(t.getDurationInMillis());

            if (!t.isSuccess()) {
                fail.incrementAndGet();
            }

            int duration = DurationComputer.computeDuration((int) t.getDurationInMillis());
            AtomicInteger count = durations.get(duration);

            if (count == null) {
                count = new AtomicInteger(0);

                AtomicInteger oldCount = durations.putIfAbsent(duration, count);

                if (oldCount != null) {
                    count = oldCount;
                }
            }
            count.incrementAndGet();

            int longDuration = checkAndGetLongThreshold(t.getType(), duration);

            if (longDuration > 0) {
                AtomicInteger longCount = longDurations.get(longDuration);

                if (longCount == null) {
                    longCount = new AtomicInteger(0);

                    AtomicInteger oldLongCount = longDurations.putIfAbsent(longDuration, longCount);

                    if (oldLongCount != null) {
                        longCount = oldLongCount;
                    }
                }
                longCount.incrementAndGet();
            }
            return this;
        }

        AtomicInteger getCount() {
            return count;
        }

        String getDurationString() {
            return buildDurationString(durations);
        }

        private String buildDurationString(ConcurrentHashMap<Integer, AtomicInteger> durations) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;

            for (Entry<Integer, AtomicInteger> entry : durations.entrySet()) {
                Integer key = entry.getKey();
                AtomicInteger value = entry.getValue();

                if (first) {
                    sb.append(key).append(',').append(value);
                    first = false;
                } else {
                    sb.append('|').append(key).append(',').append(value);
                }
            }

            return sb.toString();
        }

        String getLongDurationString() {
            return buildDurationString(longDurations);
        }

        AtomicInteger getFail() {
            return fail;
        }

        public String getName() {
            return name;
        }

        public AtomicLong getSum() {
            return sum;
        }

        public String getType() {
            return type;
        }
    }

}
