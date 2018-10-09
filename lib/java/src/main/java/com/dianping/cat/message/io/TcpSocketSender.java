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
package com.dianping.cat.message.io;

import com.dianping.cat.analyzer.LocalAggregator;
import com.dianping.cat.configuration.ClientConfigService;
import com.dianping.cat.configuration.DefaultClientConfigService;
import com.dianping.cat.configuration.MessageType;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.log.CatLogger;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.queue.DefaultMessageQueue;
import com.dianping.cat.message.queue.PriorityMessageQueue;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.NativeMessageCodec;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageStatistics;
import com.dianping.cat.status.AbstractCollector;
import com.dianping.cat.status.StatusExtensionRegister;
import com.dianping.cat.util.Threads;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TcpSocketSender implements Threads.Task, MessageSender {
    private MessageCodec nativeCodec = new NativeMessageCodec();
    private MessageStatistics statistics = new DefaultMessageStatistics();
    private ClientConfigService configService = DefaultClientConfigService.getInstance();
    private MessageQueue messageQueue = new PriorityMessageQueue(SIZE);
    private MessageIdFactory factory = MessageIdFactory.getInstance();
    private AtomicMessageManager atomicQueueManager = new AtomicMessageManager(SIZE);
    private ChannelManager channelManager = ChannelManager.getInstance();

    private boolean active;
    private static final int SIZE = 5000;
    private static final long HOUR = 1000 * 60 * 60L;
    private static CatLogger LOGGER = CatLogger.getInstance();
    private static TcpSocketSender INSTANCE = new TcpSocketSender();

    public static TcpSocketSender getInstance() {
        return INSTANCE;
    }

    private TcpSocketSender() {
        List<Server> servers = configService.getServers();
        List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();

        for (Server server : servers) {
            if (server.isEnabled()) {
                addresses.add(new InetSocketAddress(server.getIp(), server.getPort()));
            }
        }

        initialize(addresses);
    }

    @Override
    public String getName() {
        return "netty-tcp-data-sender";
    }

    private void initialize(List<InetSocketAddress> addresses) {
        Threads.forGroup("cat").start(channelManager);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.info("shut down cat client in runtime shut down hook!");
                shutdown();
            }
        });

        StatusExtensionRegister.getInstance().register(new AbstractCollector() {

            @Override
            public String getId() {
                return "cat.status";
            }

            @Override
            public Map<String, String> getProperties() {
                Map<String, String> map = new LinkedHashMap<String, String>();

                map.put("cat.status.send.sample.ratio", String.valueOf(configService.getSamplingRate() * 100));
                map.put("cat.status.send.queue.size", String.valueOf(messageQueue.size()));
                map.put("cat.status.send.atomic.queue.size", String.valueOf(atomicQueueManager.getQueueSize()));

                Map<String, Long> values = statistics.getStatistics();

                for (Entry<String, Long> entry : values.entrySet()) {
                    map.put(entry.getKey(), String.valueOf(entry.getValue()));
                }

                return map;
            }
        });
    }

    private void logMessageDiscard(MessageTree tree) {
        statistics.onOverflowed(tree);
    }

    private void offer(MessageTree tree) {
        MessageType type = configService.parseMessageType(tree);
        boolean result = true;

        switch (type) {
            case NORMAL_MESSAGE:
                result = messageQueue.offer(tree);
                break;
            case SMALL_TRANSACTION:
                result = atomicQueueManager.offerToQueue(tree);
                break;
            case STAND_ALONE_EVENT:
                processTreeInClient(tree);
                break;
        }

        if (!result) {
            processTreeInClient(tree);

            if (!tree.canDiscard()) {
                logMessageDiscard(tree);
            }
        }
    }

    private void processMessage() {
        ChannelFuture channel = channelManager.channel();

        if (channel != null) {
            MessageTree tree = null;

            try {
                tree = messageQueue.poll();

                if (tree != null) {
                    sendInternal(channel, tree);
                    tree.setMessage(null);
                } else {
                    try {
                        Thread.sleep(5);
                    } catch (Exception e) {
                        active = false;
                    }
                }
            } catch (Throwable t) {
                LOGGER.error(PlainTextMessageCodec.encodeTree(tree));
                LOGGER.error("Error when sending message over TCP socket!", t);
            }
        } else {
            long current = System.currentTimeMillis();
            long oldTimestamp = current - HOUR;

            while (true) {
                try {
                    MessageTree tree = messageQueue.peek();

                    if (tree != null && tree.getMessage().getTimestamp() < oldTimestamp) {
                        MessageTree discardTree = messageQueue.poll();

                        if (discardTree != null) {
                            statistics.onOverflowed(discardTree);
                        }
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    break;
                }
            }

            try {
                Thread.sleep(5);
            } catch (Exception e) {
                active = false;
            }
        }
    }

    private void processTreeInClient(MessageTree tree) {
        LocalAggregator.aggregate(tree);
    }

    @Override
    public void run() {
        active = true;

        while (active) {
            processMessage();
            atomicQueueManager.processAtomicMessage();
        }

        atomicQueueManager.processAtomicMessage();

        while (true) {
            MessageTree tree = messageQueue.poll();

            if (tree != null) {
                ChannelFuture channel = channelManager.channel();

                if (channel != null) {
                    sendInternal(channel, tree);
                } else {
                    offer(tree);
                }
            } else {
                break;
            }
        }
    }

    @Override
    public void send(MessageTree tree) {
        if (!configService.isMessageBlock()) {
            double sampleRatio = configService.getSamplingRate();

            if (tree.canDiscard() && sampleRatio < 1.0 && (!tree.isHitSample())) {
                processTreeInClient(tree);
            } else {
                offer(tree);
            }
        }
    }

    private void sendInternal(ChannelFuture channel, MessageTree tree) {
        if (tree.getMessageId() == null) {
            tree.setMessageId(factory.getNextId());
        }

        ByteBuf buf = nativeCodec.encode(tree);
        int size = buf.readableBytes();

        channel.channel().writeAndFlush(buf);

        if (statistics != null) {
            statistics.onBytes(size);
        }
    }

    @Override
    public void shutdown() {
        active = false;
        channelManager.shutdown();
    }

    public class AtomicMessageManager {
        private MessageQueue smallMessages;
        private static final long HOUR = 1000 * 60 * 60L;
        private static final int MAX_CHILD_NUMBER = 200;
        private static final int MAX_DURATION = 1000 * 30;

        public AtomicMessageManager(int size) {
            smallMessages = new DefaultMessageQueue(size);
        }

        public int getQueueSize() {
            return smallMessages.size();
        }

        private boolean isSameHour(long time1, long time2) {
            int hour1 = (int) (time1 / HOUR);
            int hour2 = (int) (time2 / HOUR);

            return hour1 == hour2;
        }

        private MessageTree mergeTree(MessageQueue queue) {
            int max = MAX_CHILD_NUMBER;
            DefaultTransaction t = new DefaultTransaction("System", "AtomicAggregator");
            MessageTree first = queue.poll();
            final Message message = first.getMessage();
            final long timestamp = message.getTimestamp();

            t.setStatus(Transaction.SUCCESS);
            t.setCompleted(true);
            t.setDurationStart(timestamp);
            t.setTimestamp(timestamp);
            t.setDurationInMicros(0);
            t.addChild(message);

            while (max >= 0) {
                MessageTree tree = queue.peek();

                if (tree != null) {
                    long nextTimestamp = tree.getMessage().getTimestamp();

                    if (isSameHour(timestamp, nextTimestamp)) {
                        tree = queue.poll();

                        if (tree == null) {
                            break;
                        }
                        t.addChild(tree.getMessage());
                        max--;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
            first.setMessage(t);
            return first;
        }

        public boolean offerToQueue(MessageTree tree) {
            return smallMessages.offer(tree);
        }

        public void processAtomicMessage() {
            processNormalAtomicMessage();
        }

        void processNormalAtomicMessage() {
            while (true) {
                if (shouldMerge(smallMessages)) {
                    MessageTree tree = mergeTree(smallMessages);

                    offer(tree);
                } else {
                    break;
                }
            }
        }

        private boolean shouldMerge(MessageQueue queue) {
            MessageTree tree = queue.peek();

            if (tree != null) {
                long firstTime = tree.getMessage().getTimestamp();
                return System.currentTimeMillis() - firstTime > MAX_DURATION || queue.size() >= MAX_CHILD_NUMBER;
            }
            return false;
        }
    }
}
