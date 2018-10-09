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

import com.dianping.cat.log.CatLogger;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class Threads {
    private static volatile Manager manager = new Manager();
    private static CatLogger logger = CatLogger.getInstance();

    public static void addListener(ThreadListener listener) {
        manager.addListener(listener);
    }

    public static ThreadGroupManager forGroup() {
        return manager.getThreadGroupManager("Background");
    }

    public static ThreadGroupManager forGroup(String name) {
        return manager.getThreadGroupManager(name);
    }

    public static ThreadPoolManager forPool() {
        return manager.getThreadPoolManager();
    }

    public static String getCallerClass() {
        return RunnableThread.callerThreadLocal.get();
    }

    public static void removeListener(ThreadListener listener) {
        manager.removeListener(listener);
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
        private ThreadGroup threadGroup;

        private String name;

        private AtomicInteger index = new AtomicInteger();

        private UncaughtExceptionHandler handler;

        public DefaultThreadFactory(String name) {
            threadGroup = new ThreadGroup(name);
            this.name = name;
        }

        public DefaultThreadFactory(ThreadGroup threadGroup) {
            this.threadGroup = threadGroup;
            name = threadGroup.getName();
        }

        public String getName() {
            return name;
        }

        @Override
        public Thread newThread(Runnable r) {
            int nextIndex = index.getAndIncrement(); // always increase by one
            String threadName;

            if (r instanceof Task) {
                threadName = name + "-" + ((Task) r).getName();
            } else {
                threadName = name + "-" + nextIndex;
            }

            return new RunnableThread(threadGroup, r, threadName, handler);
        }

        public void setUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
            this.handler = handler;
        }
    }

    static class Manager implements UncaughtExceptionHandler {
        private Map<String, ThreadGroupManager> groupManagers = new LinkedHashMap<String, ThreadGroupManager>();

        private List<ThreadListener> listeners = new ArrayList<ThreadListener>();

        private ThreadPoolManager threadPoolManager;

        public Manager() {
            Thread shutdownThread = new Thread() {
                @Override
                public void run() {
                    shutdownAll();
                }
            };

            threadPoolManager = new ThreadPoolManager(this);
            shutdownThread.setDaemon(true);
            Runtime.getRuntime().addShutdownHook(shutdownThread);
        }

        public void addListener(ThreadListener listener) {
            listeners.add(listener);
        }

        public ThreadGroupManager getThreadGroupManager(String name) {
            ThreadGroupManager groupManager = groupManagers.get(name);

            if (groupManager == null) {
                synchronized (this) {
                    groupManager = groupManagers.get(name);

                    if (groupManager != null && !groupManager.isActive()) {
                        groupManagers.remove(name);
                        groupManager = null;
                    }

                    if (groupManager == null) {
                        groupManager = new ThreadGroupManager(this, name);
                        groupManagers.put(name, groupManager);

                        onThreadGroupCreated(groupManager.getThreadGroup(), name);
                    }
                }
            }

            return groupManager;
        }

        public ThreadPoolManager getThreadPoolManager() {
            return threadPoolManager;
        }

        public void onThreadGroupCreated(ThreadGroup group, String name) {
            for (ThreadListener listener : listeners) {
                try {
                    listener.onThreadGroupCreated(group, name);
                } catch (Exception e) {
                    CatLogger.getInstance().error(e.getMessage(), e);
                }
            }
        }

        public void onThreadPoolCreated(ExecutorService service, String name) {
            for (ThreadListener listener : listeners) {
                try {
                    listener.onThreadPoolCreated(service, name);
                } catch (Exception e) {
                    CatLogger.getInstance().error(e.getMessage(), e);
                }
            }
        }

        public void onThreadStarting(Thread thread, String name) {
            for (ThreadListener listener : listeners) {
                try {
                    listener.onThreadStarting(thread, name);
                } catch (Exception e) {
                    CatLogger.getInstance().error(e.getMessage(), e);
                }
            }
        }

        public void onThreadStopped(Thread thread, String name) {
            for (ThreadListener listener : listeners) {
                try {
                    listener.onThreadStopping(thread, name);
                } catch (Exception e) {
                    CatLogger.getInstance().error(e.getMessage(), e);
                }
            }
        }

        public void removeListener(ThreadListener listener) {
            listeners.remove(listener);
        }

        public void shutdownAll() {
            for (ThreadGroupManager manager : groupManagers.values()) {
                manager.shutdown();
            }

            threadPoolManager.shutdownAll();
        }

        @Override
        public void uncaughtException(Thread thread, Throwable e) {
            for (ThreadListener listener : listeners) {
                boolean handled = listener.onUncaughtException(thread, e);

                if (handled) {
                    break;
                }
            }
        }
    }

    static class RunnableThread extends Thread {
        private Runnable target;
        private String caller;
        private static ThreadLocal<String> callerThreadLocal = new ThreadLocal<String>();

        public RunnableThread(ThreadGroup threadGroup, Runnable target, String name, UncaughtExceptionHandler handler) {
            super(threadGroup, target, name);

            this.target = target;
            caller = getCaller();

            setDaemon(true);
            setUncaughtExceptionHandler(handler);

            if (getPriority() != Thread.NORM_PRIORITY) {
                setPriority(Thread.NORM_PRIORITY);
            }
        }

        private String getCaller() {
            StackTraceElement[] elements = new Exception().getStackTrace();
            String prefix = Threads.class.getName() + "$";

            for (StackTraceElement element : elements) {
                String className = element.getClassName();

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
            return target;
        }

        @Override
        public void run() {
            callerThreadLocal.set(caller);
            manager.onThreadStarting(this, getName());
            super.run();
            manager.onThreadStopped(this, getName());
            callerThreadLocal.remove();
        }

        public void shutdown() {
            if (target instanceof Task) {
                ((Task) target).shutdown();
            } else {
                CatLogger.getInstance().info(String.format("Thread(%s) is shutdown! ", getName()));
                interrupt();
            }
        }
    }

    public interface Task extends Runnable {
        String getName();

        void shutdown();
    }

    public static class ThreadGroupManager {
        private DefaultThreadFactory factory;
        private ThreadGroup threadGroup;
        private boolean active;
        private boolean deamon;

        public ThreadGroupManager(UncaughtExceptionHandler handler, String name) {
            threadGroup = new ThreadGroup(name);
            factory = new DefaultThreadFactory(threadGroup);
            factory.setUncaughtExceptionHandler(handler);
            active = true;
            deamon = true;
        }

        public void awaitTermination(long time, TimeUnit unit) {
            long remaining = unit.toNanos(time);

            while (remaining > 0) {
                int len = threadGroup.activeCount();
                Thread[] activeThreads = new Thread[len];
                int num = threadGroup.enumerate(activeThreads);
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
            return threadGroup;
        }

        public boolean isActive() {
            return active;
        }

        public ThreadGroupManager nonDaemon() {
            deamon = false;
            return this;
        }

        public void shutdown() {
            int len = threadGroup.activeCount();
            Thread[] activeThreads = new Thread[len];
            int num = threadGroup.enumerate(activeThreads);

            for (int i = 0; i < num; i++) {
                Thread thread = activeThreads[i];

                if (thread instanceof RunnableThread) {
                    ((RunnableThread) thread).shutdown();
                } else if (thread.isAlive()) {
                    thread.interrupt();
                }
            }

            active = false;
        }

        public Thread start(Runnable runnable) {
            return start(runnable, deamon);
        }

        public Thread start(Runnable runnable, boolean deamon) {
            Thread thread = factory.newThread(runnable);

            logger.info("cat client start thread " + thread.getName());

            thread.setDaemon(deamon);
            thread.start();
            return thread;
        }
    }

    public interface ThreadListener {
        void onThreadGroupCreated(ThreadGroup group, String name);

        /**
         * Triggered when a thread pool (ExecutorService) has been created.
         *
         * @param pool    thread pool
         * @param pattern thread pool name pattern
         */
        void onThreadPoolCreated(ExecutorService pool, String pattern);

        /**
         * Triggered when a thread is starting.
         *
         * @param thread thread which is starting
         * @param name   thread name
         */
        void onThreadStarting(Thread thread, String name);

        void onThreadStopping(Thread thread, String name);

        /**
         * Triggered when an uncaught exception thrown from within a thread.
         *
         * @param thread thread which has an uncaught exception thrown
         * @param e      the exception uncaught
         * @return true means the exception is handled, it will be not handled again other listeners, false otherwise.
         */
        boolean onUncaughtException(Thread thread, Throwable e);
    }

    public static class ThreadPoolManager {
        private UncaughtExceptionHandler handler;
        private Map<String, ExecutorService> services = new LinkedHashMap<String, ExecutorService>();

        public ThreadPoolManager(UncaughtExceptionHandler handler) {
            this.handler = handler;
        }

        public ExecutorService getCachedThreadPool(String name) {
            ExecutorService service = services.get(name);

            if (service != null && service.isShutdown()) {
                services.remove(name);
                service = null;
            }

            if (service == null) {
                synchronized (this) {
                    service = services.get(name);

                    if (service == null) {
                        DefaultThreadFactory factory = newThreadFactory(name);
                        service = Executors.newCachedThreadPool(factory);

                        services.put(name, service);
                        manager.onThreadPoolCreated(service, factory.getName());
                    }
                }
            }

            return service;
        }

        public ExecutorService getFixedThreadPool(String name, int nThreads) {
            ExecutorService service = services.get(name);

            if (service != null && service.isShutdown()) {
                services.remove(name);
                service = null;
            }

            if (service == null) {
                synchronized (this) {
                    service = services.get(name);

                    if (service == null) {
                        DefaultThreadFactory factory = newThreadFactory(name);
                        service = Executors.newFixedThreadPool(nThreads, factory);

                        services.put(name, service);
                        manager.onThreadPoolCreated(service, factory.getName());
                    }
                }
            }

            return service;
        }

        public ScheduledExecutorService getScheduledThreadPool(String name, int nThreads) {
            ExecutorService service = services.get(name);

            if (service != null && service.isShutdown()) {
                services.remove(name);
                service = null;
            }

            if (service == null) {
                synchronized (this) {
                    service = services.get(name);

                    if (service == null) {
                        DefaultThreadFactory factory = newThreadFactory(name);
                        service = Executors.newScheduledThreadPool(nThreads, factory);

                        services.put(name, service);
                        manager.onThreadPoolCreated(service, factory.getName());
                    }
                }
            }

            return (ScheduledExecutorService) service;
        }

        DefaultThreadFactory newThreadFactory(String name) {
            DefaultThreadFactory factory = new DefaultThreadFactory(name);

            factory.setUncaughtExceptionHandler(handler);
            return factory;
        }

        public void shutdownAll() {
            for (ExecutorService service : services.values()) {
                service.shutdown();
            }

            boolean allTerminated = false;
            int count = 100;

            while (!allTerminated && count-- > 0) {
                try {
                    for (ExecutorService service : services.values()) {
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
