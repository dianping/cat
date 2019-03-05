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
package com.dianping.cat.status;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ApplicationEnvironment;
import com.dianping.cat.configuration.ClientConfigService;
import com.dianping.cat.configuration.DefaultClientConfigService;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.log.CatLogger;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageProducer;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.io.ChannelManager;
import com.dianping.cat.status.http.HttpStatsCollector;
import com.dianping.cat.status.jvm.ClassLoadingInfoCollector;
import com.dianping.cat.status.jvm.JvmInfoCollector;
import com.dianping.cat.status.jvm.ThreadInfoCollector;
import com.dianping.cat.status.jvm.ThreadInfoWriter;
import com.dianping.cat.status.model.entity.CustomInfo;
import com.dianping.cat.status.model.entity.Extension;
import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.util.Threads;
import io.netty.channel.ChannelFuture;
import com.dianping.cat.status.datasource.c3p0.C3P0InfoCollector;
import com.dianping.cat.status.datasource.druid.DruidInfoCollector;
import com.dianping.cat.status.system.ProcessorInfoCollector;
import com.dianping.cat.status.system.StaticInfoCollector;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class StatusUpdateTask implements Threads.Task {
    private ClientConfigService configService = DefaultClientConfigService.getInstance();
    private ChannelManager channelManager = ChannelManager.getInstance();
    private boolean active = true;
    private static CatLogger LOGGER = CatLogger.getInstance();

    public StatusUpdateTask() {
        initialize();
    }

    private void await() {
        // try to wait cat client init success
        try {
            Thread.sleep(10 * 1000L);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private void buildExtenstion(StatusInfo status) {
        StatusExtensionRegister res = StatusExtensionRegister.getInstance();
        List<StatusExtension> extensions = res.getStatusExtension();

        for (StatusExtension extension : extensions) {
            Transaction t = Cat.newTransaction("System", "StatusExtension-" + extension.getId());

            try {
                Map<String, String> properties = extension.getProperties();

                if (properties.size() > 0) {
                    String id = extension.getId();
                    String des = extension.getDescription();
                    Extension item = status.findOrCreateExtension(id).setDescription(des);

                    for (Entry<String, String> entry : properties.entrySet()) {
                        final String key = entry.getKey();
                        final String value = entry.getValue();

                        try {
                            double doubleValue = Double.parseDouble(value);

                            if (value.equalsIgnoreCase("NaN")) {
                                doubleValue = 0;
                            }

                            item.findOrCreateExtensionDetail(key).setValue(doubleValue);
                        } catch (Exception e) {
                            status.getCustomInfos().put(key, new CustomInfo().setKey(key).setValue(value));
                        }
                    }
                }
                t.setSuccessStatus();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                t.setStatus(e);
            } finally {
                t.complete();
            }
        }

        ChannelFuture future;
        if (null != (future = channelManager.channel())) {
            String localAddress = future.channel().localAddress().toString();

            status.getCustomInfos().put("localAddress", new CustomInfo().setKey("localAddress").setValue(localAddress));
            status.getCustomInfos().put("env", new CustomInfo().setKey("env").setValue(ApplicationEnvironment.ENVIRONMENT));
        }
    }

    private void buildHeartbeat(final String localHostAddress) {
        Transaction t = Cat.newTransaction("System", "Status");
        Heartbeat h = Cat.getProducer().newHeartbeat("Heartbeat", localHostAddress);
        StatusInfo status = new StatusInfo();
        Cat.getManager().getThreadLocalMessageTree().setDiscardPrivate(false);

        try {
            buildExtenstion(status);
            h.addData(status.toString());
            h.setStatus(Message.SUCCESS);
        } catch (Throwable e) {
            h.setStatus(e);
            Cat.logError(e);
        } finally {
            h.complete();
        }
        String eventName = calMinuteString();

        if (Cat.isJstackEnabled()) {
            Cat.logEvent("Heartbeat", "jstack." + eventName, Event.SUCCESS, buildJstack());
        }

        t.setStatus(Message.SUCCESS);
        t.complete();
    }

    private String buildJstack() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();

        bean.setThreadContentionMonitoringEnabled(true);

        ThreadInfo[] threads = bean.dumpAllThreads(false, false);
        return new ThreadInfoWriter().buildThreadsInfo(threads);
    }

    private String calMinuteString() {
        Calendar cal = Calendar.getInstance();
        int minute = cal.get(Calendar.MINUTE);
        String eventName;

        if (minute < 10) {
            eventName = "jstack-0" + minute;
        } else {
            eventName = "jstack-" + minute;
        }
        return eventName;
    }

    private void clearCache() {
        DefaultTransaction.clearCache();
        DefaultMessageProducer.clearCache();
    }

    @Override
    public String getName() {
        return "heartbeat-task";
    }

    private void initialize() {
        try {
            JvmInfoCollector.getInstance().registerJVMCollector();
            StatusExtensionRegister instance = StatusExtensionRegister.getInstance();

            instance.register(new StaticInfoCollector());
            instance.register(new ClassLoadingInfoCollector());
            instance.register(new ThreadInfoCollector());

            if (!isDocker()) {
                instance.register(new ProcessorInfoCollector());
            }

            if (Cat.isDataSourceMonitorEnabled()) {
                instance.register(new C3P0InfoCollector());
                instance.register(new DruidInfoCollector());
            }

            instance.register(new HttpStatsCollector());
        } catch (Exception e) {
            // ignore
        }

        logMemoryBean();
    }

    private boolean isDocker() {
        File file = new File("/data/webapps/hulk");
        String hostname = NetworkInterfaceManager.INSTANCE.getLocalHostName();

        return hostname.startsWith("set-") || file.exists();
    }

    private void logMemoryBean() {
        try {
            for (final MemoryPoolMXBean memoryPool : ManagementFactory.getMemoryPoolMXBeans()) {
                LOGGER.info("memory pool:" + memoryPool.getName());
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void refreshClientConfig() {
        try {
            long current = System.currentTimeMillis() / 1000 / 60;
            int min = (int) (current % (60));

            if (min % 3 == 0) {
                configService.refreshConfig();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    public void run() {
        await();

        String localHostAddress = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
        Cat.logEvent("Reboot", localHostAddress, Message.SUCCESS, null);

        while (active) {
            // build heartbeat stack
            buildHeartbeat(localHostAddress);

            // refresh cat client config
            refreshClientConfig();

            // clear some cache in client memory
            clearCache();

            try {
                Calendar cal = Calendar.getInstance();

                cal.set(Calendar.SECOND, 20);
                cal.add(Calendar.MINUTE, 1);

                long elapsed = cal.getTimeInMillis() - System.currentTimeMillis();

                if (elapsed > 0) {
                    Thread.sleep(elapsed);
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Override
    public void shutdown() {
        active = false;
    }
}
