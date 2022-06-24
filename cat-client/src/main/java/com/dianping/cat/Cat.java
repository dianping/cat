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

import java.io.File;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.MessageTree;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.context.MetricContextHelper;
import com.dianping.cat.message.context.TraceContextHelper;
import com.dianping.cat.message.internal.NullMessage;

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
	private static Cat CAT = new Cat();

	private static int m_errors;

	private CatBootstrap m_bootstrap;

	private Cat() {
		m_bootstrap = new CatBootstrap();
	}

	public static void destroy() {
		try {
			CAT.m_bootstrap.reset();
			CAT = new Cat();
		} catch (Exception e) {
			errorHandler(e);
		}
	}

	private static void errorHandler(Exception e) {
		if (m_errors++ % 100 == 0 || m_errors <= 3) {
			e.printStackTrace();
		}
	}

	public static CatBootstrap getBootstrap() {
		return CAT.m_bootstrap;
	}

	public static File getCatHome() {
		return CAT.m_bootstrap.getCatHome();
	}

	public static void logError(String message, Throwable cause) {
		try {
			Event event = TraceContextHelper.threadLocal().newEvent(message, cause);

			event.complete();
		} catch (Exception e) {
			errorHandler(e);
		}
	}

	public static void logError(Throwable cause) {
		logError(null, cause);
	}

	public static void logEvent(String type, String name) {
		try {
			Event event = TraceContextHelper.threadLocal().newEvent(type, name);

			event.success().complete();
		} catch (Exception e) {
			errorHandler(e);
		}
	}

	public static void logEvent(String type, String name, String status, String nameValuePairs) {
		try {
			Event event = TraceContextHelper.threadLocal().newEvent(type, name);

			event.addData(nameValuePairs);
			event.setStatus(status);
			event.complete();
		} catch (Exception e) {
			errorHandler(e);
		}
	}

	/**
	 * Increase the counter specified by <code>name</code> by one.
	 *
	 * @param name
	 *           the name of the metric default count value is 1
	 */
	public static void logMetricForCount(String name) {
		logMetricForCount(name, 1);
	}

	/**
	 * Increase the counter specified by <code>name</code> by one.
	 *
	 * @param name
	 *           the name of the metric
	 */
	public static void logMetricForCount(String name, int quantity) {
		try {
			Metric metric = MetricContextHelper.context().newMetric(name);

			metric.count(quantity);
		} catch (Exception e) {
			errorHandler(e);
		}
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
		try {
			Metric metric = MetricContextHelper.context().newMetric(name);

			metric.duration(1, durationInMillis);
		} catch (Exception e) {
			errorHandler(e);
		}
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
		logMetricForSum(name, value, 1);
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
		try {
			Metric metric = MetricContextHelper.context().newMetric(name);

			metric.sum(quantity, sum);
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
		logRemoteCallClient(ctx, null);
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
			MessageTree tree = TraceContextHelper.threadLocal().getMessageTree();
			String messageId = tree.getMessageId();
			String childId = TraceContextHelper.createMessageId(domain);
			
			Cat.logEvent(CatClientConstants.TYPE_REMOTE_CALL, "", Event.SUCCESS, childId);

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
			MessageTree tree = TraceContextHelper.threadLocal().getMessageTree();
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
			Trace trace = TraceContextHelper.threadLocal().newTrace(type, name);

			trace.success().complete();
		} catch (Exception e) {
			errorHandler(e);
		}
	}

	public static void logTrace(String type, String name, String status, String nameValuePairs) {
		try {
			Trace trace = TraceContextHelper.threadLocal().newTrace(type, name);

			trace.addData(nameValuePairs);
			trace.setStatus(status);
			trace.complete();
		} catch (Exception e) {
			errorHandler(e);
		}
	}

	public static Event newEvent(String type, String name) {
		try {
			return TraceContextHelper.threadLocal().newEvent(type, name);
		} catch (Exception e) {
			errorHandler(e);
			return NullMessage.EVENT;
		}
	}

	public static Heartbeat newHeartbeat(String type, String name) {
		try {
			return TraceContextHelper.threadLocal().newHeartbeat(type, name);
		} catch (Exception e) {
			errorHandler(e);
			return NullMessage.HEARTBEAT;
		}
	}

	public static Trace newTrace(String type, String name) {
		try {
			return TraceContextHelper.threadLocal().newTrace(type, name);
		} catch (Exception e) {
			errorHandler(e);
			return NullMessage.TRACE;
		}
	}

	public static Transaction newTransaction(String type, String name) {
		try {
			return TraceContextHelper.threadLocal().newTransaction(type, name);
		} catch (Exception e) {
			errorHandler(e);
			return NullMessage.TRANSACTION;
		}
	}

	public static interface Context {
		public final String ROOT = "_catRootMessageId";

		public final String PARENT = "_catParentMessageId";

		public final String CHILD = "_catChildMessageId";

		public void addProperty(String key, String value);

		public String getProperty(String key);
	}
}
