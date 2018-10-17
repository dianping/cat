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

import com.dianping.cat.configuration.ClientConfigService;
import com.dianping.cat.configuration.DefaultClientConfigService;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.log.CatLogger;
import com.dianping.cat.message.ForkableTransaction;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.io.TcpSocketSender;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultMessageManager implements MessageManager {
    private long throttleTimes;
    private String domain;
    private String hostName;
    private String ip;
    private boolean firstMessage = true;
    private static final int SIZE = 2000;
    private static final long HOUR = 3600 * 1000L;
    private ClientConfigService configService = DefaultClientConfigService.getInstance();
    private TcpSocketSender sender = TcpSocketSender.getInstance();
    private MessageIdFactory factory = MessageIdFactory.getInstance();
    private AtomicInteger sampleCount = new AtomicInteger();
    private ThreadLocal<Context> context = new ThreadLocal<Context>();
    private static CatLogger LOGGER = CatLogger.getInstance();
    private TransactionHelper validator = new TransactionHelper();
    private static MessageManager INSTANCE = new DefaultMessageManager();

    public static MessageManager getInstance() {
        return INSTANCE;
    }

    private DefaultMessageManager() {
        initialize();
    }

    @Override
    public void add(Message message) {
        Context ctx = getContext();

        if (ctx != null) {
            ctx.add(message);
        }
    }

    @Override
    public void end(Transaction transaction) {
        Context ctx = getContext();

        if (ctx != null) {
            ctx.end(this, transaction);
        }
    }

    public void flush(MessageTree tree, boolean clearContext) {
        if (sender != null) {
            sender.send(tree);

            if (clearContext) {
                reset();
            }
        } else {
            throttleTimes++;

            if (throttleTimes % 10000 == 0 || throttleTimes == 1) {
                LOGGER.info("Cat Message is throttled! Times:" + throttleTimes);
            }
        }
    }

    public ClientConfigService getConfigService() {
        return configService;
    }

    public Context getContext() {
        Context ctx = context.get();

        if (ctx != null) {
            return ctx;
        } else {
            ctx = new Context(domain, hostName, ip);

            context.set(ctx);
            return ctx;
        }
    }

    @Override
    public String getDomain() {
        return domain;
    }

    public String getMetricType() {
        return "";
    }

    @Override
    public Transaction getPeekTransaction() {
        Context ctx = getContext();

        if (ctx != null) {
            return ctx.peekTransaction();
        } else {
            return null;
        }
    }

    @Override
    public MessageTree getThreadLocalMessageTree() {
        Context ctx = context.get();

        if (ctx == null) {
            setup();
        }
        ctx = context.get();

        return ctx.tree;
    }

    @Override
    public boolean hasContext() {
        Context context = this.context.get();

        return context != null;
    }

    private boolean hitSample(double sampleRatio) {
        int count = sampleCount.incrementAndGet();

        return count % ((int) (1.0 / sampleRatio)) == 0;
    }

    private void initialize() {
        domain = String.valueOf(configService.getDomain()).intern();
        hostName = NetworkInterfaceManager.INSTANCE.getLocalHostName().intern();
        ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress().intern();

        // initialize domain and IP address
        try {
            factory.initialize(domain);
        } catch (Exception e) {
            LOGGER.error("error when create mark file", e);
        }
    }

    @Deprecated
    @Override
    public boolean isCatEnabled() {
        return true;
    }

    @Deprecated
    @Override
    public boolean isMessageEnabled() {
        return true;
    }

    public boolean isTraceMode() {
        Context content = getContext();

        if (content != null) {
            return content.isTraceMode();
        } else {
            return false;
        }
    }

    private String nextMessageId() {
        return factory.getNextId();
    }

    public boolean notExsitCause(Throwable e) {
        Context ctx = context.get();

        if (ctx != null) {
            return ctx.notExsitCause(e);
        } else {
            return true;
        }
    }

    @Override
    public void reset() {
        Context ctx = context.get();

        if (ctx != null) {
            ctx.reset();
        }
    }

    public void setMetricType(String metricType) {
    }

    public void setTraceMode(boolean traceMode) {
        Context context = getContext();

        if (context != null) {
            context.setTraceMode(traceMode);
        }
    }

    @Override
    public void setup() {
        Context ctx = new Context(domain, hostName, ip);
        double samplingRate = configService.getSamplingRate();

        if (samplingRate < 1.0 && hitSample(samplingRate)) {
            ctx.tree.setHitSample(true);
        }
        context.set(ctx);
    }

    @Override
    public void start(Transaction transaction, boolean forked) {
        Context ctx = getContext();

        if (ctx != null) {
            ctx.start(transaction, forked);
        } else if (firstMessage) {
            firstMessage = false;
            LOGGER.error("CAT client is not enabled because it's not initialized yet");
        }
    }

    public class Context {
        private MessageTree tree;
        private Stack<Transaction> stack;
        private int length;
        private boolean traceMode;
        private long totalDurationInMicros;
        private Set<Throwable> knownExceptions;

        public Context(String domain, String hostName, String ipAddress) {
            tree = new DefaultMessageTree();
            stack = new Stack<Transaction>();

            Thread thread = Thread.currentThread();
            String groupName = thread.getThreadGroup().getName();

            tree.setThreadGroupName(groupName);
            tree.setThreadId(String.valueOf(thread.getId()));
            tree.setThreadName(thread.getName());

            tree.setDomain(domain);
            tree.setHostName(hostName);
            tree.setIpAddress(ipAddress);
            length = 1;
        }

        public void add(Message message) {
            if (stack.isEmpty()) {
                MessageTree tree = this.tree.copy();

                tree.setMessage(message);
                flush(tree, true);
            } else {
                Transaction parent = stack.peek();

                addTransactionChild(message, parent);
            }
        }

        public void addForkableTransaction(ForkableTransaction forkableTransaction) {
            tree.addForkableTransaction(forkableTransaction);
        }

        private void addTransactionChild(Message message, Transaction transaction) {
            long treePeriod = tree.getMessage().getTimestamp() / HOUR;
            long messagePeriod = (message.getTimestamp() - 10 * 1000L) / HOUR; // 10 seconds extra time allowed

            if (treePeriod < messagePeriod || length >= SIZE) {
                validator.truncateAndFlush(this, message.getTimestamp());
            }

            transaction.addChild(message);
            length++;
        }

        public void attach(ForkedTransaction forked, String rootMessageId, String parentMessageId) {
            if (stack.isEmpty()) {
                stack.push(forked);
                tree.setMessage(forked);
                tree.setRootMessageId(rootMessageId);
                tree.setParentMessageId(parentMessageId);
            }
        }

        public void detach(String rootMessageId, String parentMessageId) {
            if (stack.firstElement() instanceof ForkedTransaction) {
                ForkedTransaction forked = (ForkedTransaction) stack.pop();

                if (parentMessageId != null) {
                    MessageTree tree = this.tree.copy();

                    if (rootMessageId == null) {
                        rootMessageId = parentMessageId;
                    }

                    tree.setRootMessageId(rootMessageId);
                    tree.setParentMessageId(parentMessageId);
                    tree.setMessageId(forked.getMessageId());
                    tree.setMessage(forked);

                    flush(tree, true);
                } else {
                    length = 1;

                    if (knownExceptions != null) {
                        knownExceptions.clear();
                    }

                    List<ForkableTransaction> transactions = tree.getForkableTransactions();

                    if (transactions != null) {
                        transactions.clear();
                    }
                }
            }
        }

        /**
         * return true means the transaction has been flushed.
         *
         * @param manager
         * @param transaction
         * @return true if message is flushed, false otherwise
         */
        public boolean end(DefaultMessageManager manager, Transaction transaction) {
            if (!stack.isEmpty()) {
                Transaction current = stack.pop();

                if (transaction == current) {
                    // validator.validate(stack.isEmpty() ? null : stack.peek(), current);
                } else {
                    while (transaction != current && !stack.empty()) {
                        // validator.validate(stack.peek(), current);

                        current = stack.pop();
                    }
                }

                if (stack.isEmpty()) {
                    final List<ForkableTransaction> forkableTransactions = tree.getForkableTransactions();

                    if (forkableTransactions != null && !forkableTransactions.isEmpty()) {
                        for (ForkableTransaction forkableTransaction : forkableTransactions) {
                            forkableTransaction.complete();
                        }
                    }

                    MessageTree tree = this.tree.copy();

                    this.tree.setMessageId(null);
                    this.tree.setMessage(null);

                    if (totalDurationInMicros > 0) {
                        tree.setDiscardPrivate(false);
                    }

                    manager.flush(tree, true);
                    return true;
                }
            }

            return false;
        }

        public Set<Throwable> getKnownExceptions() {
            return knownExceptions;
        }

        public int getLength() {
            return length;
        }

        public boolean isTraceMode() {
            return traceMode;
        }

        public boolean notExsitCause(Throwable e) {
            if (knownExceptions == null) {
                knownExceptions = new HashSet<Throwable>();
            }

            if (knownExceptions.contains(e)) {
                return false;
            } else {
                knownExceptions.add(e);
                return true;
            }
        }

        public Transaction peekTransaction() {
            if (stack.isEmpty()) {
                return null;
            } else {
                return stack.peek();
            }
        }

        public void reset() {
            if (knownExceptions != null) {
                knownExceptions.clear();
            }
            stack.clear();

            tree.setDomain(domain);
            tree.setIpAddress(ip);
            tree.setHostName(hostName);
            tree.setMessage(null);
            tree.setMessageId(null);
            tree.setRootMessageId(null);
            tree.setParentMessageId(null);
            tree.setSessionToken(null);
            tree.setDiscardPrivate(true);
            traceMode = false;
            totalDurationInMicros = 0;
            length = 1;

            List<ForkableTransaction> forked = tree.getForkableTransactions();

            if (forked != null) {
                forked.clear();
            }

            double samplingRate = configService.getSamplingRate();

            if (samplingRate < 1.0 && hitSample(samplingRate)) {
                tree.setHitSample(true);
            } else {
                tree.setHitSample(false);
            }
        }

        public void setTraceMode(boolean traceMode) {
            this.traceMode = traceMode;
        }

        public void start(Transaction transaction, boolean forked) {
            if (!stack.isEmpty()) {
                // Do NOT make strong reference from parent transaction to forked transaction.
                // Instead, we create a "soft" reference to forked transaction later, via linkAsRunAway()
                // By doing so, there is no need for synchronization between parent and child threads.
                // Both threads can complete() anytime despite the other thread.

                if (!(transaction instanceof ForkedTransaction)) {
                    Transaction parent = stack.peek();
                    addTransactionChild(transaction, parent);
                }
            } else {
                tree.setMessage(transaction);
            }

            if (!forked) {
                stack.push(transaction);
            }
        }
    }

    class TransactionHelper {

        private void migrateMessage(Stack<Transaction> stack, Transaction source, Transaction target, int level) {
            Transaction current = level < stack.size() ? stack.get(level) : null;
            boolean shouldKeep = false;
            final List<Message> childs = source.getChildren();

            for (Message child : childs) {
                if (child != current) {
                    target.addChild(child);
                } else {
                    DefaultTransaction cloned = new DefaultTransaction(current.getType(), current.getName(),
                            DefaultMessageManager.this);

                    cloned.setTimestamp(current.getTimestamp());
                    cloned.setDurationInMicros(current.getDurationInMicros());
                    // cloned.addData(current.getData().toString());
                    cloned.setStatus(Message.SUCCESS);

                    target.addChild(cloned);
                    migrateMessage(stack, current, cloned, level + 1);
                    shouldKeep = true;
                }
            }

            source.getChildren().clear();

            if (shouldKeep) { // add it back
                source.addChild(current);
            }
        }

        public void truncateAndFlush(Context ctx, long timestamp) {
            MessageTree tree = ctx.tree;
            List<ForkableTransaction> forkableTransactions = tree.getForkableTransactions();
            Stack<Transaction> stack = ctx.stack;
            Message message = tree.getMessage();

            if (message instanceof DefaultTransaction || message instanceof DefaultForkedTransaction) {

                if (forkableTransactions != null) {
                    for (ForkableTransaction forkableTransaction : forkableTransactions) {
                        forkableTransaction.complete();
                    }
                }

                String id = tree.getMessageId();

                if (id == null) {
                    id = nextMessageId();
                    tree.setMessageId(id);
                }

                String rootId = tree.getRootMessageId();
                String childId = nextMessageId();
                Transaction source = (Transaction) message;
                DefaultTransaction target = new DefaultTransaction(source.getType(), source.getName(),
                        DefaultMessageManager.this);

                target.setTimestamp(source.getTimestamp());
                target.setDurationInMicros(source.getDurationInMicros());
                // target.addData(source.getData().toString());
                target.setStatus(Message.SUCCESS);

                migrateMessage(stack, source, target, 1);

                if (message instanceof DefaultTransaction) {
                    // 增加到子logview的下一个链接，向下关联
                    DefaultEvent next = new DefaultEvent("RemoteCall", "Next");

                    next.addData(childId);
                    next.setStatus(Message.SUCCESS);
                    target.addChild(next);
                } else {
                    // 在forked情况下，最后一个messageTree需要和父亲线程上下文保持关联，在堆栈里面加入已经发送的logview的链接，这个地方是向上关联
                    DefaultForkedTransaction t = (DefaultForkedTransaction) stack.get(0);
                    List<Message> temp = new ArrayList<Message>();

                    DefaultEvent parent = new DefaultEvent("RemoteCall", "Next");

                    parent.addData(id);
                    parent.setStatus(Message.SUCCESS);

                    temp.add(parent);
                    temp.addAll(t.getChildren());

                    t.getChildren().clear();
                    t.getChildren().addAll(temp);
                }

                // tree is the parent, and tree is the child.
                MessageTree t = tree.copy();

                t.setMessage(target);

                ctx.tree.setMessageId(childId);
                ctx.tree.setParentMessageId(id);
                ctx.tree.setRootMessageId(rootId != null ? rootId : id);

                if (ctx.knownExceptions != null) {
                    ctx.knownExceptions.clear();
                }

                List<ForkableTransaction> forkables = ctx.tree.getForkableTransactions();

                if (forkables != null) {
                    forkables.clear();
                }

                ctx.length = stack.size();
                ctx.totalDurationInMicros = ctx.totalDurationInMicros + target.getDurationInMicros();

                flush(t, false);
            }
        }
    }

}
