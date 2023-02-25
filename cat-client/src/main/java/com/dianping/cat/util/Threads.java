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
package com.dianping.cat.util;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class Threads {
	private static volatile Manager s_manager = new Manager();

	public static void addListener(ThreadListener listener) {
		s_manager.addListener(listener);
	}

	public static ThreadGroupManager forGroup() {
		return s_manager.getThreadGroupManager("Background");
	}

	public static ThreadGroupManager forGroup(String name) {
		return s_manager.getThreadGroupManager(name);
	}

	public static ThreadPoolManager forPool() {
		return s_manager.getThreadPoolManager();
	}

	public static String getCallerClass() {
		return RunnableThread.m_callerThreadLocal.get();
	}

	public static void removeListener(ThreadListener listener) {
		s_manager.removeListener(listener);
	}

	public static interface Task extends Runnable {
		public String getName();

		public void shutdown();
	}

	public static interface ThreadListener {
		public void onThreadGroupCreated(ThreadGroup group, String name);

		/**
			* Triggered when a thread pool (ExecutorService) has been created.
			*
			* @param pool    thread pool
			* @param pattern thread pool name pattern
			*/
		public void onThreadPoolCreated(ExecutorService pool, String pattern);

		/**
			* Triggered when a thread is starting.
			*
			* @param thread thread which is starting
			* @param name   thread name
			*/
		public void onThreadStarting(Thread thread, String name);

		public void onThreadStopping(Thread thread, String name);

		/**
			* Triggered when an uncaught exception thrown from within a thread.
			*
			* @param thread thread which has an uncaught exception thrown
			* @param e      the exception uncaught
			* @return true means the exception is handled, it will be not handled again other listeners, false otherwise.
			*/
		public boolean onUncaughtException(Thread thread, Throwable e);
	}

	public static abstract class AbstractThreadListener implements ThreadListener {
		@Override
		public void onThreadGroupCreated(ThreadGroup group, String name) {
			// to be override
		}

		@Override
		public void onThreadPoolCreated(ExecutorService pool, String name) {
			// to be override
		}

		@Override
		public void onThreadStarting(Thread thread, String name) {
			// to be override
		}

		@Override
		public void onThreadStopping(Thread thread, String name) {
			// to be override
		}

		@Override
		public boolean onUncaughtException(Thread thread, Throwable e) {
			// to be override
			return false;
		}
	}

	static class DefaultThreadFactory implements ThreadFactory {
		private ThreadGroup m_threadGroup;

		private String m_name;

		private AtomicInteger m_index = new AtomicInteger();

		private UncaughtExceptionHandler m_handler;

		public DefaultThreadFactory(String name) {
			m_threadGroup = new ThreadGroup(name);
			m_name = name;
		}

		public DefaultThreadFactory(ThreadGroup threadGroup) {
			m_threadGroup = threadGroup;
			m_name = threadGroup.getName();
		}

		public String getName() {
			return m_name;
		}

		@Override
		public Thread newThread(Runnable r) {
			int nextIndex = m_index.getAndIncrement(); // always increase by one
			String threadName;

			if (r instanceof Task) {
				threadName = m_name + "-" + ((Task) r).getName();
			} else {
				threadName = m_name + "-" + nextIndex;
			}

			return new RunnableThread(m_threadGroup, r, threadName, m_handler);
		}

		public void setUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
			m_handler = handler;
		}
	}

	static class Manager implements UncaughtExceptionHandler {
		private Map<String, ThreadGroupManager> m_groupManagers = new LinkedHashMap<String, ThreadGroupManager>();

		private List<ThreadListener> m_listeners = new ArrayList<ThreadListener>();

		private ThreadPoolManager m_threadPoolManager;

		public Manager() {
			Thread shutdownThread = new Thread() {
				@Override
				public void run() {
					shutdownAll();
				}
			};

			m_threadPoolManager = new ThreadPoolManager(this);
			shutdownThread.setDaemon(true);
			Runtime.getRuntime().addShutdownHook(shutdownThread);
		}

		public void addListener(ThreadListener listener) {
			m_listeners.add(listener);
		}

		public ThreadGroupManager getThreadGroupManager(String name) {
			ThreadGroupManager groupManager = m_groupManagers.get(name);

			if (groupManager == null) {
				synchronized (this) {
					groupManager = m_groupManagers.get(name);

					if (groupManager != null && !groupManager.isActive()) {
						m_groupManagers.remove(name);
						groupManager = null;
					}

					if (groupManager == null) {
						groupManager = new ThreadGroupManager(this, name);
						m_groupManagers.put(name, groupManager);

						onThreadGroupCreated(groupManager.getThreadGroup(), name);
					}
				}
			}

			return groupManager;
		}

		public ThreadPoolManager getThreadPoolManager() {
			return m_threadPoolManager;
		}

		public void onThreadGroupCreated(ThreadGroup group, String name) {
			for (ThreadListener listener : m_listeners) {
				try {
					listener.onThreadGroupCreated(group, name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void onThreadPoolCreated(ExecutorService service, String name) {
			for (ThreadListener listener : m_listeners) {
				try {
					listener.onThreadPoolCreated(service, name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void onThreadStarting(Thread thread, String name) {
			for (ThreadListener listener : m_listeners) {
				try {
					listener.onThreadStarting(thread, name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void onThreadStopped(Thread thread, String name) {
			for (ThreadListener listener : m_listeners) {
				try {
					listener.onThreadStopping(thread, name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void removeListener(ThreadListener listener) {
			m_listeners.remove(listener);
		}

		public void shutdownAll() {
			for (ThreadGroupManager manager : m_groupManagers.values()) {
				manager.shutdown();
			}

			m_threadPoolManager.shutdownAll();
		}

		@Override
		public void uncaughtException(Thread thread, Throwable e) {
			for (ThreadListener listener : m_listeners) {
				boolean handled = listener.onUncaughtException(thread, e);

				if (handled) {
					break;
				}
			}
		}
	}

	static class RunnableThread extends Thread {
		private static ThreadLocal<String> m_callerThreadLocal = new ThreadLocal<String>();

		private Runnable m_target;

		private String m_caller;

		public RunnableThread(ThreadGroup threadGroup, Runnable target, String name, UncaughtExceptionHandler handler) {
			super(threadGroup, target, name);

			m_target = target;
			m_caller = getCaller();

			setDaemon(true);
			setUncaughtExceptionHandler(handler);

			if (getPriority() != Thread.NORM_PRIORITY) {
				setPriority(Thread.NORM_PRIORITY);
			}
		}

		private String getCaller() {
			StackTraceElement[] elements = new Exception().getStackTrace();
			String prefix = Threads.class.getName() + "$";

			for (int i = 0; i < elements.length; i++) {
				String className = elements[i].getClassName();

				if (className.startsWith(prefix)) {
					continue;
				}

				int pos = className.lastIndexOf('$');

				if (pos < 0) {
					pos = className.lastIndexOf('.');
				}

				if (pos < 0) {
					return className;
				} else {
					return className.substring(pos + 1);
				}
			}

			return null;
		}

		public Runnable getTarget() {
			return m_target;
		}

		@Override
		public void run() {
			m_callerThreadLocal.set(m_caller);
			s_manager.onThreadStarting(this, getName());
			super.run();
			s_manager.onThreadStopped(this, getName());
			m_callerThreadLocal.remove();
		}

		public void shutdown() {
			if (m_target instanceof Task) {
				((Task) m_target).shutdown();
			} else {
				System.out.println(String.format("Thread(%s) is shutdown! ", getName()));
				interrupt();
			}
		}
	}

	public static class ThreadGroupManager {
		private DefaultThreadFactory m_factory;

		private ThreadGroup m_threadGroup;

		private boolean m_active;

		private boolean m_deamon;

		public ThreadGroupManager(UncaughtExceptionHandler handler, String name) {
			m_threadGroup = new ThreadGroup(name);
			m_factory = new DefaultThreadFactory(m_threadGroup);
			m_factory.setUncaughtExceptionHandler(handler);
			m_active = true;
			m_deamon = true;
		}

		public void awaitTermination(long time, TimeUnit unit) {
			long remaining = unit.toNanos(time);

			while (remaining > 0) {
				int len = m_threadGroup.activeCount();
				Thread[] activeThreads = new Thread[len];
				int num = m_threadGroup.enumerate(activeThreads);
				boolean anyAlive = false;

				for (int i = 0; i < num; i++) {
					Thread thread = activeThreads[i];

					if (thread.isAlive()) {
						anyAlive = true;
						break;
					}
				}

				if (anyAlive) {
					long slice = 1000 * 1000L;

					// wait for 1 ms
					LockSupport.parkNanos(slice);
					remaining -= slice;
				} else {
					break;
				}
			}
		}

		public ThreadGroup getThreadGroup() {
			return m_threadGroup;
		}

		public boolean isActive() {
			return m_active;
		}

		public ThreadGroupManager nonDaemon() {
			m_deamon = false;
			return this;
		}

		public void shutdown() {
			int len = m_threadGroup.activeCount();
			Thread[] activeThreads = new Thread[len];
			int num = m_threadGroup.enumerate(activeThreads);

			for (int i = 0; i < num; i++) {
				Thread thread = activeThreads[i];

				if (thread instanceof RunnableThread) {
					((RunnableThread) thread).shutdown();
				} else if (thread.isAlive()) {
					thread.interrupt();
				}
			}

			m_active = false;
		}

		public Thread start(Runnable runnable) {
			return start(runnable, m_deamon);
		}

		public Thread start(Runnable runnable, boolean deamon) {
			Thread thread = m_factory.newThread(runnable);

			System.out.println("cat client start thead " + thread.getName());

			thread.setDaemon(deamon);
			thread.start();
			return thread;
		}
	}

	public static class ThreadPoolManager {
		private UncaughtExceptionHandler m_handler;

		private Map<String, ExecutorService> m_services = new LinkedHashMap<String, ExecutorService>();

		public ThreadPoolManager(UncaughtExceptionHandler handler) {
			m_handler = handler;
		}

		public ExecutorService getCachedThreadPool(String name) {
			ExecutorService service = m_services.get(name);

			if (service != null && service.isShutdown()) {
				m_services.remove(name);
				service = null;
			}

			if (service == null) {
				synchronized (this) {
					service = m_services.get(name);

					if (service == null) {
						DefaultThreadFactory factory = newThreadFactory(name);
						service = Executors.newCachedThreadPool(factory);

						m_services.put(name, service);
						s_manager.onThreadPoolCreated(service, factory.getName());
					}
				}
			}

			return service;
		}

		public ExecutorService getFixedThreadPool(String name, int nThreads) {
			ExecutorService service = m_services.get(name);

			if (service != null && service.isShutdown()) {
				m_services.remove(name);
				service = null;
			}

			if (service == null) {
				synchronized (this) {
					service = m_services.get(name);

					if (service == null) {
						DefaultThreadFactory factory = newThreadFactory(name);
						service = Executors.newFixedThreadPool(nThreads, factory);

						m_services.put(name, service);
						s_manager.onThreadPoolCreated(service, factory.getName());
					}
				}
			}

			return service;
		}

		public ScheduledExecutorService getScheduledThreadPool(String name, int nThreads) {
			ExecutorService service = m_services.get(name);

			if (service != null && service.isShutdown()) {
				m_services.remove(name);
				service = null;
			}

			if (service == null) {
				synchronized (this) {
					service = m_services.get(name);

					if (service == null) {
						DefaultThreadFactory factory = newThreadFactory(name);
						service = Executors.newScheduledThreadPool(nThreads, factory);

						m_services.put(name, service);
						s_manager.onThreadPoolCreated(service, factory.getName());
					}
				}
			}

			return (ScheduledExecutorService) service;
		}

		DefaultThreadFactory newThreadFactory(String name) {
			DefaultThreadFactory factory = new DefaultThreadFactory(name);

			factory.setUncaughtExceptionHandler(m_handler);
			return factory;
		}

		public void shutdownAll() {
			for (ExecutorService service : m_services.values()) {
				service.shutdown();
			}

			boolean allTerminated = false;
			int count = 100;

			while (!allTerminated && count-- > 0) {
				try {
					for (ExecutorService service : m_services.values()) {
						if (!service.isTerminated()) {
							service.awaitTermination(10, TimeUnit.MILLISECONDS);
						}
					}

					allTerminated = true;
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		}
	}
}
