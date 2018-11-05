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
package com.dianping.cat;

import com.dianping.cat.analyzer.MetricTagAggregator;
import com.dianping.cat.analyzer.TransactionAggregator;
import com.dianping.cat.configuration.ApplicationEnvironment;
import com.dianping.cat.configuration.ClientConfigProvider;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultSaxParser;
import com.dianping.cat.log.CatLogger;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.internal.*;
import com.dianping.cat.util.Properties;
import com.dianping.cat.util.StringUtils;
import com.dianping.cat.util.Threads;
import com.dianping.cat.analyzer.EventAggregator;
import com.dianping.cat.analyzer.LocalAggregator;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.io.TcpSocketSender;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.status.StatusUpdateTask;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

public class Cat {
    private static MessageProducer producer;
    private static MessageManager manager;
    private static int errorCount;
    private static final Cat instance = new Cat();
    private static volatile boolean init = false;
    private static volatile boolean enabled = true;
    private static volatile boolean JSTACK_ENABLED = true;
    private static volatile boolean MULTI_INSTANCES = false;
    private static volatile boolean DATASOURCE_MONITOR_ENABLED = true;
    public final static String CLIENT_CONFIG = "cat-client-config";
    public final static String UNKNOWN = "unknown";

    public static boolean isJstackEnabled() {
        String enable = Properties.forString().fromEnv().fromSystem().getProperty("jstack_enable", "true");

        return JSTACK_ENABLED && Boolean.valueOf(enable);
    }

    private static void checkAndInitialize() {
        try {
            if (!init) {
            	ClientConfig clientConfig = getSpiClientConfig();
				if (clientConfig == null) {
					initializeInternal();
				} else {
					initializeInternal(clientConfig);
				}
            }
        } catch (Exception e) {
            errorHandler(e);
        }
    }
    
    private static ClientConfig getSpiClientConfig() {
		ServiceLoader<ClientConfigProvider> clientConfigProviders = ServiceLoader.load(ClientConfigProvider.class);
		if (clientConfigProviders == null) {
			return null;
		}
		
		Iterator<ClientConfigProvider> iterator = clientConfigProviders.iterator();
		if (iterator.hasNext()){
			//只支持一个ClientConfigProvider的实现，默认取查询结果第一个
			ClientConfigProvider clientConfigProvider = (ClientConfigProvider)iterator.next();
			return clientConfigProvider.getClientConfig();
		} else {
			return null;
		}
	}

    public static String createMessageId() {
        if (isEnabled()) {
            try {
                return Cat.getProducer().createMessageId();
            } catch (Exception e) {
                errorHandler(e);
                return NullMessageProducer.NULL_MESSAGE_PRODUCER.createMessageId();
            }
        } else {
            return NullMessageProducer.NULL_MESSAGE_PRODUCER.createMessageId();
        }
    }

    public static void enable() {
        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }

    /**
     * disable datasource auto monitor
     */
    public static void disableDataSourceMonitor() {
        DATASOURCE_MONITOR_ENABLED = false;
    }

    public static void disableJstack() {
        JSTACK_ENABLED = false;
    }

    public static void disableMultiInstances() {
        MULTI_INSTANCES = false;
    }

    public static void enableMultiInstances() {
        MULTI_INSTANCES = true;
    }

    private static void errorHandler(Exception e) {
        if (isEnabled() && errorCount < 3) {
            errorCount++;

            CatLogger.getInstance().error(e.getMessage(), e);
        }
    }

    public static String getCatHome() {
        return Properties.forString().fromEnv().fromSystem().getProperty("CAT_HOME", "/data/appdatas/cat/");
    }

    public static String getCurrentMessageId() {
        if (isEnabled()) {
            try {
                MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

                if (tree != null) {
                    String messageId = tree.getMessageId();

                    if (messageId == null) {
                        messageId = Cat.getProducer().createMessageId();
                        tree.setMessageId(messageId);
                    }
                    return messageId;
                } else {
                    return null;
                }
            } catch (Exception e) {
                errorHandler(e);
                return NullMessageProducer.NULL_MESSAGE_PRODUCER.createMessageId();
            }
        } else {
            return NullMessageProducer.NULL_MESSAGE_PRODUCER.createMessageId();
        }
    }

    private static String getCustomDomain() {
        String config = System.getProperty(Cat.CLIENT_CONFIG);

        if (StringUtils.isNotEmpty(config)) {
            try {
                ClientConfig clientConfig = DefaultSaxParser.parse(config);

                return clientConfig.getDomain();
            } catch (Exception e) {
                // ignore
            }
        }
        return null;
    }

    public static Cat getInstance() {
        return instance;
    }

    public static MessageManager getManager() {
        try {
            checkAndInitialize();

            if (manager != null) {
                return manager;
            } else {
                return NullMessageManager.NULL_MESSAGE_MANAGER;
            }
        } catch (Exception e) {
            errorHandler(e);
            return NullMessageManager.NULL_MESSAGE_MANAGER;
        }
    }

    public static MessageProducer getProducer() {
        try {
            checkAndInitialize();

            if (producer != null) {
                return producer;
            } else {
                return NullMessageProducer.NULL_MESSAGE_PRODUCER;
            }
        } catch (Exception e) {
            errorHandler(e);
            return NullMessageProducer.NULL_MESSAGE_PRODUCER;
        }
    }

    public static void initialize() {
        checkAndInitialize();
    }

    public static void initialize(String... servers) {
        if (isEnabled() && !init) {
            try {
                ClientConfig config = new ClientConfig();

                for (String server : servers) {
                    config.addServer(new Server(server));
                }
                final String domain = ApplicationEnvironment.loadAppName(UNKNOWN);
                config.setDomain(domain);

                initializeInternal(config);
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    public static void initializeByDomain(String domain) {
        if (isEnabled() && !init) {
            try {
                String domainName = ApplicationEnvironment.loadAppName(domain);
                ClientConfig config = ApplicationEnvironment.loadClientConfig(domainName);

                initializeInternal(config);
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    public static void initializeByDomain(String domain, int port, int httpPort, String... servers) {
        if (isEnabled() && !init) {
            try {
                ClientConfig config = new ClientConfig();

                config.setDomain(ApplicationEnvironment.loadAppName(domain));

                for (String server : servers) {
                    Server serverObj = new Server(server);

                    serverObj.setHttpPort(httpPort);
                    serverObj.setPort(port);
                    config.addServer(serverObj);
                }

                initializeInternal(config);
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    public static void initializeByDomain(String domain, String... servers) {
        if (isEnabled() && !init) {
            try {
                initializeByDomain(domain, 2280, 8080, servers);
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    public static void initializeByDomainForce(String domain) {
        if (isEnabled() && !init) {
            try {
                ClientConfig config = ApplicationEnvironment.loadClientConfig(domain);

                initializeInternal(config);
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    private static void initializeInternal() {
        validate();

        if (isEnabled()) {
            try {
                if (!init) {
                    synchronized (instance) {
                        if (!init) {
                            producer = DefaultMessageProducer.getInstance();
                            manager = DefaultMessageManager.getInstance();

                            StatusUpdateTask heartbeatTask = new StatusUpdateTask();
                            TcpSocketSender messageSender = TcpSocketSender.getInstance();

                            Threads.forGroup("cat").start(heartbeatTask);
                            Threads.forGroup("cat").start(messageSender);
                            Threads.forGroup("cat").start(new LocalAggregator.DataUploader());

                            CatLogger.getInstance().info("Cat is lazy initialized!");
                            init = true;
                        }
                    }
                }
            } catch (Exception e) {
                errorHandler(e);
                disable();
            }
        }
    }

    private static void initializeInternal(ClientConfig config) {
        if (isEnabled()) {
            System.setProperty(Cat.CLIENT_CONFIG, config.toString());
            CatLogger.getInstance().info("init cat with config:" + config.toString());

            initializeInternal();
        }
    }

    public static boolean isDataSourceMonitorEnabled() {
        return DATASOURCE_MONITOR_ENABLED;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isInitialized() {
        return init;
    }

    public static boolean isMultiInstanceEnable() {
        return MULTI_INSTANCES;
    }

    /**
     * Log batch event in one shot with SUCCESS status.
     *
     * @param type  event type
     * @param name  event name
     * @param error error count
     * @param count total count , failure% = error/total
     */
    public static void logBatchEvent(String type, String name, int count, int error) {
        if (isEnabled()) {
            try {
                EventAggregator.getInstance().logBatchEvent(type, name, count, error);
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    /**
     * log batch transaction with type name
     *
     * @param type  transaction type
     * @param name  transaction name
     * @param error error count
     * @param count total count, failure% = error/total
     * @param sum   avg = sum/total sum in milliseconds
     */
    public static void logBatchTransaction(String type, String name, int count, int error, long sum) {
        if (isEnabled()) {
            TransactionAggregator.getInstance().logBatchTransaction(type, name, count, error, sum);
        }
    }

    public static void logError(String message, Throwable cause) {
        if (isEnabled()) {
            try {
                Cat.getProducer().logError(message, cause);
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    public static void logError(Throwable cause) {
        if (isEnabled()) {
            try {
                Cat.getProducer().logError(cause);
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    public static void logErrorWithCategory(String category, String message, Throwable cause) {
        if (isEnabled()) {
            try {
                Cat.getProducer().logErrorWithCategory(category, message, cause);
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    public static void logErrorWithCategory(String category, Throwable cause) {
        if (isEnabled()) {
            try {
                Cat.getProducer().logErrorWithCategory(category, cause);
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    public static void logEvent(String type, String name) {
        if (isEnabled()) {
            try {
                Cat.getProducer().logEvent(type, name);
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    /**
     * Log an event in one shot.
     *
     * @param type           event type
     * @param name           event name
     * @param status         "0" means success, otherwise means error code
     * @param nameValuePairs name value pairs in the format of "a=1&b=2&..."
     */
    public static void logEvent(String type, String name, String status, String nameValuePairs) {
        if (isEnabled()) {
            try {
                Cat.getProducer().logEvent(type, name, status, nameValuePairs);
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    public static void logMetricForCount(String name) {
        logMetricForCount(name, null);
    }

    public static void logMetricForCount(String name, int quantity) {
        logMetricForCount(name, quantity, null);
    }

    public static void logMetricForCount(String name, int quantity, Map<String, String> tags) {
        if (isEnabled()) {
            checkAndInitialize();

            try {
                MetricTagAggregator.getInstance().addCountMetric(name, quantity, tags);
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    public static void logMetricForCount(String name, Map<String, String> tags) {
        if (isEnabled()) {
            checkAndInitialize();

            try {
                MetricTagAggregator.getInstance().addCountMetric(name, 1, tags);
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    public static void logMetricForDuration(String name, long durationInMillis) {
        logMetricForDuration(name, durationInMillis, null);
    }

    public static void logMetricForDuration(String name, long durationInMillis, Map<String, String> tags) {
        if (isEnabled()) {
            checkAndInitialize();

            try {
                MetricTagAggregator.getInstance().addTimerMetric(name, durationInMillis, tags);
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    public static void logRemoteCallClient(Context ctx) {
        if (isEnabled()) {
            try {
                logRemoteCallClient(ctx, "default");
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    public static void logRemoteCallClient(Context ctx, String domain) {
        if (isEnabled()) {
            try {
                MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
                String messageId = tree.getMessageId();

                if (messageId == null) {
                    messageId = Cat.getProducer().createMessageId();
                    tree.setMessageId(messageId);
                }

                String childId = Cat.getProducer().createRpcServerId(domain);
                Cat.logEvent(CatConstants.TYPE_REMOTE_CALL, "", Event.SUCCESS, childId);

                String root = tree.getRootMessageId();

                if (root == null) {
                    root = messageId;
                }

                ctx.addProperty(Context.ROOT, root);
                ctx.addProperty(Context.PARENT, messageId);
                ctx.addProperty(Context.CHILD, childId);
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    public static void logRemoteCallServer(Context ctx) {
        if (isEnabled()) {
            try {
                MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
                String childId = ctx.getProperty(Context.CHILD);
                String rootId = ctx.getProperty(Context.ROOT);
                String parentId = ctx.getProperty(Context.PARENT);

                if (parentId != null) {
                    tree.setParentMessageId(parentId);
                }
                if (rootId != null) {
                    tree.setRootMessageId(rootId);
                }
                if (childId != null) {
                    tree.setMessageId(childId);
                }
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    public static void newCompletedTransactionWithDuration(String type, String name, long duration) {
        if (isEnabled()) {
            try {
                final Transaction transaction = Cat.getProducer().newTransaction(type, name);

                try {
                    transaction.setDurationInMillis(duration);

                    if (duration > 0 && duration < 60 * 1000) {
                        transaction.setTimestamp(System.currentTimeMillis() - duration);
                    }
                    transaction.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    transaction.setStatus(e);
                } finally {
                    transaction.complete();
                }
            } catch (Exception e) {
                errorHandler(e);
            }
        }
    }

    public static Event newEvent(String type, String name) {
        if (isEnabled()) {
            try {
                return Cat.getProducer().newEvent(type, name);
            } catch (Exception e) {
                errorHandler(e);
                return NullMessage.EVENT;
            }
        } else {
            return NullMessage.EVENT;
        }
    }

    public static Trace newTrace(String type, String name) {
        if (isEnabled()) {
            try {
                return Cat.getProducer().newTrace(type, name);
            } catch (Exception e) {
                errorHandler(e);
                return NullMessage.TRACE;
            }
        } else {
            return NullMessage.TRACE;
        }
    }

    /**
     * Create a new transaction with given type and name.
     *
     * @param type transaction type
     * @param name transaction name
     */
    public static Transaction newTransaction(String type, String name) {
        if (isEnabled()) {
            try {
                return Cat.getProducer().newTransaction(type, name);
            } catch (Exception e) {
                errorHandler(e);
                return NullMessage.TRANSACTION;
            }
        } else {
            return NullMessage.TRANSACTION;
        }
    }

    /**
     * Create a new transaction with given type and name and duration, duration time in millisecond
     *
     * @param type transaction type
     * @param name transaction name
     */
    public static Transaction newTransactionWithDuration(String type, String name, long duration) {
        if (isEnabled()) {
            try {
                final Transaction transaction = Cat.getProducer().newTransaction(type, name);

                transaction.setDurationInMillis(duration);

                if (duration < 60 * 1000) {
                    transaction.setTimestamp(System.currentTimeMillis() - duration);
                }
                return transaction;
            } catch (Exception e) {
                errorHandler(e);
                return NullMessage.TRANSACTION;
            }
        } else {
            return NullMessage.TRANSACTION;
        }
    }

    private static void validate() {
        String enable = Properties.forString().fromEnv().fromSystem().getProperty("CAT_ENABLED", "true");

        if ("false".equals(enable)) {
            CatLogger.getInstance().info("CAT is disable due to system environment CAT_ENABLED is false.");

            enabled = false;
        } else {
            String customDomain = getCustomDomain();

            if (customDomain == null && UNKNOWN.equals(ApplicationEnvironment.loadAppName(UNKNOWN))) {
                CatLogger.getInstance().info("CAT is disable due to no app name in resource file /META-INF/app.properties");
                enabled = false;
            }
        }
    }

    private Cat() {
    }

    public interface Context {

        String ROOT = "_catRootMessageId";

        String PARENT = "_catParentMessageId";

        String CHILD = "_catChildMessageId";

        String DISCARD = "_catDiscard";

        void addProperty(String key, String value);

        String getProperty(String key);
    }

}
