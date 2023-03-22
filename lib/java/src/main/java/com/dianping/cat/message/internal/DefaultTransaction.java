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
import com.dianping.cat.configuration.ClientConfigService;
import com.dianping.cat.configuration.ProblemLongType;
import com.dianping.cat.message.ForkableTransaction;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultTransaction extends AbstractMessage implements Transaction {
    private long durationInMicro = -1; // must be less than 0
    private long durationStart;
    private List<Message> children;
    private MessageManager manager;
    private static Map<String, Integer> map = new ConcurrentHashMap<String, Integer>();
    private static ConcurrentMap<Long, ConcurrentHashMap<String, AtomicInteger>> count = new ConcurrentHashMap<Long, ConcurrentHashMap<String, AtomicInteger>>();

    public static void clearCache() {
        long time = System.currentTimeMillis() / 1000 / 60 - 3;
        Set<Long> removeKeys = new HashSet<Long>();

        for (Long l : count.keySet()) {
            if (l <= time) {
                removeKeys.add(l);
            }
        }

        for (Long l : removeKeys) {
            count.remove(l);
        }
    }

    public DefaultTransaction(String type, String name) {
        super(type, name);

        durationStart = System.nanoTime();
    }

    public DefaultTransaction(String type, String name, MessageManager manager) {
        super(type, name);

        this.manager = manager;
        durationStart = System.nanoTime();
    }

    @Override
    public DefaultTransaction addChild(Message message) {
        if (children == null) {
            children = new ArrayList<Message>();
        }

        children.add(message);
        return this;
    }

    @Override
    public void complete() {
        try {
            if (isCompleted()) {
                // DefaultEvent event = new DefaultEvent("cat", "BadInstrument");
                //
                // event.setStatus("TransactionAlreadyCompleted");
                // event.complete();
                // addChild(event);
            } else {
                if (durationInMicro == -1) {
                    durationInMicro = (System.nanoTime() - durationStart) / 1000L;
                }
                setCompleted(true);

                if (manager != null && isProblem(this, manager, type, durationInMicro / 1000L)) {
                    manager.getThreadLocalMessageTree().setDiscardPrivate(false);
                }

                if (manager != null) {
                    manager.end(this);
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public ForkableTransaction forFork() {
        MessageTree tree = manager.getThreadLocalMessageTree();
        String rootMessageId = tree.getRootMessageId();
        String messageId = tree.getMessageId();

        if (messageId == null) {
            messageId = Cat.createMessageId();
            tree.setMessageId(messageId);
        }

        if (rootMessageId == null) {
            rootMessageId = messageId;
        }

        ForkableTransaction forkable = new DefaultForkableTransaction(rootMessageId, messageId);
        addChild(forkable);

        tree.addForkableTransaction(forkable);

        // manager.getContext().addForkableTransaction(forkable);
        return forkable;
    }

    @Override
    public List<Message> getChildren() {
        if (children == null) {
            return new ArrayList<Message>();
        }

        return children;
    }

    @Override
    public long getDurationInMicros() {
        if (durationInMicro >= 0) {
            return durationInMicro;
        } else { // if it's not completed explicitly
            long duration = 0;
            int len = children == null ? 0 : children.size();

            if (len > 0) {
                Message lastChild = children.get(len - 1);

                if (lastChild instanceof Transaction) {
                    Transaction trx = (Transaction) lastChild;

                    duration = (trx.getTimestamp() - getTimestamp()) * 1000L + trx.getRawDurationInMicros();
                } else {
                    duration = (lastChild.getTimestamp() - getTimestamp()) * 1000L;
                }
            }

            return duration;
        }
    }

    @Override
    public long getDurationInMillis() {
        return getDurationInMicros() / 1000L;
    }

    protected MessageManager getManager() {
        return manager;
    }

    public long getRawDurationInMicros() {
        return durationInMicro;
    }

    @Override
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    private boolean isProblem(Transaction t, MessageManager manager, String type, long duration) {
        boolean problem;

        if (t.isSuccess()) {
            Integer value = map.get(type);

            if (value != null) {
                problem = duration > value;
            } else {
                int threshold = Integer.MAX_VALUE;
                ClientConfigService config = manager.getConfigService();

                ProblemLongType longType = ProblemLongType.findByMessageType(type);

                if (longType != null) {
                    switch (longType) {
                        case LONG_CACHE:
                            threshold = config.getLongConfigThreshold(ProblemLongType.LONG_CACHE.getName());
                            break;
                        case LONG_CALL:
                            threshold = config.getLongConfigThreshold(ProblemLongType.LONG_CALL.getName());
                            break;
                        case LONG_SERVICE:
                            threshold = config.getLongConfigThreshold(ProblemLongType.LONG_SERVICE.getName());
                            break;
                        case LONG_SQL:
                            threshold = config.getLongConfigThreshold(ProblemLongType.LONG_SQL.getName());
                            break;
                        case LONG_URL:
                            threshold = config.getLongConfigThreshold(ProblemLongType.LONG_URL.getName());
                            break;
                        case LONG_MQ:
                            threshold = config.getLongConfigThreshold(ProblemLongType.LONG_MQ.getName());
                            break;
                    }
                }

                map.put(type, threshold);

                problem = duration > threshold && recordProblem(t.getTimestamp(), t.getType() + t.getName());
            }
        } else {
            problem = recordProblem(t.getTimestamp(), t.getType() + t.getName());
        }

        return problem;
    }

    private boolean recordProblem(long time, String id) {
        try {
            long minute = time / 1000 / 60;
            ConcurrentHashMap<String, AtomicInteger> count = DefaultTransaction.count.get(minute);

            if (count == null) {
                count = new ConcurrentHashMap<String, AtomicInteger>();

                ConcurrentHashMap<String, AtomicInteger> oldCount = DefaultTransaction.count.putIfAbsent(minute, count);

                if (oldCount != null) {
                    count = oldCount;
                }
            }

            AtomicInteger value = count.get(id);

            if (value == null) {
                value = new AtomicInteger(0);

                AtomicInteger oldValue = count.putIfAbsent(id, value);

                if (oldValue != null) {
                    value = oldValue;
                }
            }

            return value.incrementAndGet() <= 60;
        } catch (Exception e) {
            return false;
        }
    }

    public void setDurationInMicros(long duration) {
        durationInMicro = duration;
    }

    public void setDurationInMillis(long duration) {
        durationInMicro = duration * 1000L;
    }

    public void setDurationStart(long durationStart) {
        this.durationStart = durationStart;
    }

}
