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

import java.util.concurrent.atomic.AtomicBoolean;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.TaggedTransaction;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.NullMessage;
import com.dianping.cat.message.internal.NullMessageManager;
import com.dianping.cat.message.internal.NullMessageProducer;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;

/**
 * The main entry of CAT API.
 * <p>
 * 
 * CAT client can be initialized in following two approaches:
 * <li>Explicitly initialization by calling one of following methods:
 * <ol>
 * <li><code>Cat.getBootstrap().initialize(File configFile)</code></li>
 * <li><code>Cat.getBootstrap().initialize(String... servers)</code></li>
 * <li><code>Cat.getBootstrap().initializeByDomain(String domain, String... servers)</code></li>
 * <li><code>Cat.getBootstrap().initializeByDomain(String domain, int tcpPort, int httpPort, String... servers)</code></li>
 * </ol>
 * </li>
 * <li>Implicitly initialization automatically by calling any CAT API.</li>
 * <p>
 * 
 * Methods starting with 'log' is a simple call API, and methods starting with 'new' is a compound call API, mostly used with
 * try-catch-finally statement.
 * <p>
 * 
 * @author Frankie Wu
 */
public class Cat {
	private static Cat s_instance = new Cat();

	private static AtomicBoolean s_multiInstanceEnabled = new AtomicBoolean();

	private static int m_errors;

	private CatBootstrap m_bootstrap;

	private MessageProducer m_producer = NullMessageProducer.NULL_MESSAGE_PRODUCER;

	private MessageManager m_manager = NullMessageManager.NULL_MESSAGE_MANAGER;

	private ComponentContext m_ctx;

	private Cat() {
		m_bootstrap = new CatBootstrap(this);
	}

	public static String createMessageId() {
		try {
			return Cat.getProducer().createMessageId();
		} catch (Exception e) {
			errorHandler(e);
			return NullMessageProducer.NULL_MESSAGE_PRODUCER.createMessageId();
		}
	}

	public static void destroy() {
		try {
			s_instance.m_ctx.dispose();
			s_instance = new Cat();
		} catch (Exception e) {
			errorHandler(e);
		}
	}

	public static void enableMultiInstances() {
		s_multiInstanceEnabled.set(true);
	}

	private static void errorHandler(Exception e) {
		if (m_errors++ % 100 == 0 || m_errors <= 3) {
			e.printStackTrace();
		}
	}

	public static CatBootstrap getBootstrap() {
		return s_instance.m_bootstrap;
	}

	public static String getCatHome() {
		String catHome = CatPropertyProvider.INST.getProperty("CAT_HOME", CatConstants.CAT_HOME_DEFAULT_DIR);

		if (!catHome.endsWith("/")) {
			catHome = catHome + "/";
		}

		return catHome;
	}

	public static String getCurrentMessageId() {
		try {
			MessageTree tree = getManager().getThreadLocalMessageTree();

			if (tree != null) {
				String messageId = tree.getMessageId();

				if (messageId == null) {
					messageId = Cat.createMessageId();
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
	}

	public static MessageManager getManager() {
		try {
			s_instance.m_bootstrap.initialize(new ClientConfig());

			MessageManager manager = s_instance.m_manager;

			if (manager != null) {
				return manager;
			}
		} catch (Exception e) {
			errorHandler(e);
		}

		return NullMessageManager.NULL_MESSAGE_MANAGER;
	}

	public static MessageProducer getProducer() {
		try {
			s_instance.m_bootstrap.initialize(new ClientConfig());

			MessageProducer producer = s_instance.m_producer;

			if (producer != null) {
				return producer;
			}
		} catch (Exception e) {
			errorHandler(e);
		}

		return NullMessageProducer.NULL_MESSAGE_PRODUCER;
	}

	public static boolean isInitialized() {
		return s_instance.m_bootstrap.isInitialized();
	}

	public static boolean isMultiInstanceEnabled() {
		return s_multiInstanceEnabled.get();
	}

	public static void logError(String message, Throwable cause) {
		try {
			Cat.getProducer().logError(message, cause);
		} catch (Exception e) {
			errorHandler(e);
		}
	}

	public static void logError(Throwable cause) {
		try {
			Cat.getProducer().logError(cause);
		} catch (Exception e) {
			errorHandler(e);
		}
	}

	public static void logEvent(String type, String name) {
		try {
			Cat.getProducer().logEvent(type, name);
		} catch (Exception e) {
			errorHandler(e);
		}
	}

	public static void logEvent(String type, String name, String status, String nameValuePairs) {
		try {
			Cat.getProducer().logEvent(type, name, status, nameValuePairs);
		} catch (Exception e) {
			errorHandler(e);
		}
	}

	public static void logHeartbeat(String type, String name, String status, String nameValuePairs) {
		try {
			Cat.getProducer().logHeartbeat(type, name, status, nameValuePairs);
		} catch (Exception e) {
			errorHandler(e);
		}
	}

	public static void logMetric(String name, Object... keyValues) {
		// TO BE REMOVED
	}

	/**
	 * Increase the counter specified by <code>name</code> by one.
	 *
	 * @param name
	 *           the name of the metric default count value is 1
	 */
	public static void logMetricForCount(String name) {
		logMetricInternal(name, "C", "1");
	}

	/**
	 * Increase the counter specified by <code>name</code> by one.
	 *
	 * @param name
	 *           the name of the metric
	 */
	public static void logMetricForCount(String name, int quantity) {
		logMetricInternal(name, "C", String.valueOf(quantity));
	}

	/**
	 * Increase the metric specified by <code>name</code> by <code>durationInMillis</code>.
	 *
	 * @param name
	 *           the name of the metric
	 * @param durationInMillis
	 *           duration in milli-second added to the metric
	 */
	public static void logMetricForDuration(String name, long durationInMillis) {
		logMetricInternal(name, "T", String.valueOf(durationInMillis));
	}

	/**
	 * Increase the sum specified by <code>name</code> by <code>value</code> only for one item.
	 *
	 * @param name
	 *           the name of the metric
	 * @param value
	 *           the value added to the metric
	 */
	public static void logMetricForSum(String name, double value) {
		logMetricInternal(name, "S", String.format("%.2f", value));
	}

	/**
	 * Increase the metric specified by <code>name</code> by <code>sum</code> for multiple items.
	 *
	 * @param name
	 *           the name of the metric
	 * @param sum
	 *           the sum value added to the metric
	 * @param quantity
	 *           the quantity to be accumulated
	 */
	public static void logMetricForSum(String name, double sum, int quantity) {
		logMetricInternal(name, "S,C", String.format("%s,%.2f", quantity, sum));
	}

	private static void logMetricInternal(String name, String status, String keyValuePairs) {
		try {
			Cat.getProducer().logMetric(name, status, keyValuePairs);
		} catch (Exception e) {
			errorHandler(e);
		}
	}

	/**
	 * logRemoteCallClient is used in rpc client
	 *
	 * @param ctx
	 *           ctx is rpc context ,such as duboo context , please use rpc context implement Context
	 * @param domain
	 *           domain is default, if use default config, the performance of server storage is bad。
	 */
	public static void logRemoteCallClient(Context ctx) {
		logRemoteCallClient(ctx, "default");
	}

	/**
	 * logRemoteCallClient is used in rpc client
	 *
	 * @param ctx
	 *           ctx is rpc context ,such as duboo context , please use rpc context implement Context
	 * @param domain
	 *           domain is project name of rpc server name
	 */
	public static void logRemoteCallClient(Context ctx, String domain) {
		try {
			MessageTree tree = getManager().getThreadLocalMessageTree();
			String messageId = tree.getMessageId();

			if (messageId == null) {
				messageId = Cat.createMessageId();
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

	/**
	 * used in rpc server，use clild id as server message tree id.
	 *
	 * @param ctx
	 *           ctx is rpc context ,such as duboo context , please use rpc context implement Context
	 */
	public static void logRemoteCallServer(Context ctx) {
		try {
			MessageTree tree = getManager().getThreadLocalMessageTree();
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

	public static void logTrace(String type, String name) {
		try {
			Cat.getProducer().logTrace(type, name);
		} catch (Exception e) {
			errorHandler(e);
		}
	}

	public static void logTrace(String type, String name, String status, String nameValuePairs) {
		try {
			Cat.getProducer().logTrace(type, name, status, nameValuePairs);
		} catch (Exception e) {
			errorHandler(e);
		}
	}

	public static Event newEvent(String type, String name) {
		try {
			return Cat.getProducer().newEvent(type, name);
		} catch (Exception e) {
			errorHandler(e);
			return NullMessage.EVENT;
		}
	}

	public static ForkedTransaction newForkedTransaction(String type, String name) {
		try {
			return Cat.getProducer().newForkedTransaction(type, name);
		} catch (Exception e) {
			errorHandler(e);
			return NullMessage.TRANSACTION;
		}
	}

	public static Heartbeat newHeartbeat(String type, String name) {
		try {
			return Cat.getProducer().newHeartbeat(type, name);
		} catch (Exception e) {
			errorHandler(e);
			return NullMessage.HEARTBEAT;
		}
	}

	public static TaggedTransaction newTaggedTransaction(String type, String name, String tag) {
		try {
			return Cat.getProducer().newTaggedTransaction(type, name, tag);
		} catch (Exception e) {
			errorHandler(e);
			return NullMessage.TRANSACTION;
		}
	}

	public static Trace newTrace(String type, String name) {
		try {
			return Cat.getProducer().newTrace(type, name);
		} catch (Exception e) {
			errorHandler(e);
			return NullMessage.TRACE;
		}
	}

	public static Transaction newTransaction(String type, String name) {
		try {
			return Cat.getProducer().newTransaction(type, name);
		} catch (Exception e) {
			errorHandler(e);
			return NullMessage.TRANSACTION;
		}
	}

	// this should be called when a thread ends to clean some thread local data
	public static void reset() {
		// remove me
	}

	// this should be called when a thread starts to create some thread local data
	public static void setup(String sessionToken) {
		try {
			Cat.getManager().setup();
		} catch (Exception e) {
			errorHandler(e);
		}
	}

	void setup(ComponentContext ctx) {
		m_ctx = ctx;
		m_manager = ctx.lookup(MessageManager.class);
		m_producer = ctx.lookup(MessageProducer.class);
	}

	public static interface Context {
		public final String ROOT = "_catRootMessageId";

		public final String PARENT = "_catParentMessageId";

		public final String CHILD = "_catChildMessageId";

		public void addProperty(String key, String value);

		public String getProperty(String key);
	}
}
