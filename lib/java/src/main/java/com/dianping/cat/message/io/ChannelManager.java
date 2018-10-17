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

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ClientConfigService;
import com.dianping.cat.configuration.DefaultClientConfigService;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.log.CatLogger;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.util.Pair;
import com.dianping.cat.util.Splitters;
import com.dianping.cat.util.StringUtils;
import com.dianping.cat.util.Threads;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ChannelManager implements Threads.Task {
    private ClientConfigService configService = DefaultClientConfigService.getInstance();
    private Bootstrap bootstrap;
    private boolean active = true;
    private int channelStalledTimes = 0;
    private ChannelHolder activeChannelHolder;
    private MessageIdFactory idFactory = MessageIdFactory.getInstance();
    private AtomicInteger attempts = new AtomicInteger();
    private int reconnectCount;
    private static CatLogger LOGGER = CatLogger.getInstance();
    private static ChannelManager instance = new ChannelManager();

    public static ChannelManager getInstance() {
        return instance;
    }

    private ChannelManager() {
        List<Server> servers = configService.getServers();
        List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();

        for (Server server : servers) {
            if (server.isEnabled()) {
                addresses.add(new InetSocketAddress(server.getIp(), server.getPort()));
            }
        }

        EventLoopGroup group = new NioEventLoopGroup(1, new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);

                t.setDaemon(true);
                return t;
            }
        });

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
            }
        });
        this.bootstrap = bootstrap;

        String routerConfig = configService.getRouters();

        if (StringUtils.isNotEmpty(routerConfig)) {
            List<InetSocketAddress> configuredAddresses = parseSocketAddress(routerConfig);
            ChannelHolder holder = initChannel(configuredAddresses, routerConfig);

            if (holder != null) {
                activeChannelHolder = holder;
            } else {
                activeChannelHolder = new ChannelHolder();
                activeChannelHolder.setServerAddresses(configuredAddresses);
            }
        } else {
            ChannelHolder holder = initChannel(addresses, null);

            if (holder != null) {
                activeChannelHolder = holder;
            } else {
                activeChannelHolder = new ChannelHolder();
                activeChannelHolder.setServerAddresses(addresses);
            }
        }
    }

    public ChannelFuture channel() {
        if (activeChannelHolder != null) {
            ChannelFuture future = activeChannelHolder.getActiveFuture();

            if (checkWritable(future)) {
                return future;
            }
        }
        return null;
    }

    private boolean checkActive(ChannelFuture future) {
        boolean isActive = false;

        if (future != null) {
            Channel channel = future.channel();

            if (channel.isActive() && channel.isOpen()) {
                isActive = true;
            } else {
                LOGGER.error("channel buf is not active ,current channel " + future.channel().remoteAddress());
            }
        }

        return isActive;
    }

    private void checkServerChanged() {
        Pair<Boolean, String> pair = routerConfigChanged();

        if (pair.getKey()) {
            LOGGER.info("router config changed :" + pair.getValue());
            String servers = pair.getValue();
            List<InetSocketAddress> serverAddresses = parseSocketAddress(servers);
            ChannelHolder newHolder = initChannel(serverAddresses, servers);

            if (newHolder != null) {
                if (newHolder.isConnectChanged()) {
                    ChannelHolder last = activeChannelHolder;

                    activeChannelHolder = newHolder;
                    closeChannelHolder(last);
                    LOGGER.info("switch active channel to " + activeChannelHolder);
                } else {
                    activeChannelHolder = newHolder;
                }
            }
        }
    }

    private boolean checkWritable(ChannelFuture future) {
        boolean isWritable = false;

        if (future != null) {
            Channel channel = future.channel();

            if (channel.isActive() && channel.isOpen()) {
                if (channel.isWritable()) {
                    isWritable = true;
                } else {
                    channel.flush();
                }
            } else {
                int count = attempts.incrementAndGet();

                if (count % 1000 == 0 || count == 1) {
                    LOGGER.error("channel buf is is close when send msg! Attempts: " + count);
                }
            }
        }

        return isWritable;
    }

    private void closeChannel(ChannelFuture channel) {
        try {
            if (channel != null) {
                SocketAddress address = channel.channel().remoteAddress();

                if (address != null) {
                    LOGGER.info("close channel " + address);
                }
                channel.channel().close();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void closeChannelHolder(ChannelHolder channelHolder) {
        try {
            ChannelFuture channel = channelHolder.getActiveFuture();

            closeChannel(channel);
        } catch (Exception e) {
            // ignore
        }
    }

    private ChannelFuture createChannel(InetSocketAddress address) {
        LOGGER.info("start connect server" + address.toString());
        ChannelFuture future = null;

        try {
            future = bootstrap.connect(address);
            future.awaitUninterruptibly(configService.getClientConnectTimeout(), TimeUnit.MILLISECONDS); // 100 ms

            if (!future.isSuccess()) {
                LOGGER.error("Error when try connecting to " + address);
                closeChannel(future);
            } else {
                LOGGER.info("Connected to CAT server at " + address);
                return future;
            }
        } catch (Throwable e) {
            LOGGER.error("Error when connect server " + address.getAddress(), e);

            if (future != null) {
                closeChannel(future);
            }
        }
        return null;
    }

    private void doubleCheckActiveServer(ChannelHolder channelHolder) {
        try {
            if (isChannelStalled(channelHolder)) {
                closeChannelHolder(activeChannelHolder);
                channelHolder.setActiveIndex(-1);
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return "netty-channel-health-check";
    }

    private ChannelHolder initChannel(List<InetSocketAddress> addresses, String serverConfig) {
        try {
            int len = addresses.size();

            for (int i = 0; i < len; i++) {
                InetSocketAddress address = addresses.get(i);
                String hostAddress = address.getAddress().getHostAddress();
                ChannelHolder holder = null;

                if (activeChannelHolder != null && hostAddress.equals(activeChannelHolder.getIp())) {
                    holder = new ChannelHolder();
                    holder.setActiveFuture(activeChannelHolder.getActiveFuture()).setConnectChanged(false);
                } else {
                    ChannelFuture future = createChannel(address);

                    if (future != null) {
                        holder = new ChannelHolder();
                        holder.setActiveFuture(future).setConnectChanged(true);
                    }
                }
                if (holder != null) {
                    holder.setActiveIndex(i).setIp(hostAddress);
                    holder.setActiveServerConfig(serverConfig).setServerAddresses(addresses);

                    LOGGER.info("success when init CAT server, new active holder" + holder.toString());
                    return holder;
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        try {
            StringBuilder sb = new StringBuilder();

            for (InetSocketAddress address : addresses) {
                sb.append(address.toString()).append(";");
            }
            LOGGER.info("Error when init CAT server " + sb.toString());
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private boolean isChannelStalled(ChannelHolder holder) {
        ChannelFuture future = holder.getActiveFuture();
        boolean active = checkActive(future);

        if (!active) {
            return (++channelStalledTimes) % 3 == 0;
        } else {
            if (channelStalledTimes > 0) {
                channelStalledTimes--;
            }
            return false;
        }
    }

    private List<InetSocketAddress> parseSocketAddress(String content) {
        try {
            List<String> strs = Splitters.by(";").noEmptyItem().split(content);
            List<InetSocketAddress> address = new ArrayList<InetSocketAddress>();

            for (String str : strs) {
                List<String> items = Splitters.by(":").noEmptyItem().split(str);

                address.add(new InetSocketAddress(items.get(0), Integer.parseInt(items.get(1))));
            }
            return address;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<InetSocketAddress>();
    }

    private void reconnectDefaultServer(ChannelFuture activeFuture, List<InetSocketAddress> serverAddresses) {
        try {
            int reconnectServers = activeChannelHolder.getActiveIndex();

            if (reconnectServers == -1) {
                reconnectServers = serverAddresses.size();
            }
            for (int i = 0; i < reconnectServers; i++) {
                ChannelFuture future = createChannel(serverAddresses.get(i));

                if (future != null) {
                    activeChannelHolder.setActiveFuture(future);
                    activeChannelHolder.setActiveIndex(i);
                    closeChannel(activeFuture);
                    break;
                }
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private Pair<Boolean, String> routerConfigChanged() {
        String routerConfig = configService.getRouters();

        if (!StringUtils.isEmpty(routerConfig) && !routerConfig.equals(activeChannelHolder.getActiveServerConfig())) {
            return new Pair<Boolean, String>(true, routerConfig);
        } else {
            return new Pair<Boolean, String>(false, routerConfig);
        }
    }

    @Override
    public void run() {
        while (active) {
            if (Cat.isEnabled()) {
                reconnectCount++;

                if (reconnectCount % 10 == 0) {
                    // make save message id index async very 10 seconds
                    idFactory.saveMark();
                    checkServerChanged();
                }

                ChannelFuture activeFuture = activeChannelHolder.getActiveFuture();
                List<InetSocketAddress> serverAddresses = activeChannelHolder.getServerAddresses();

                doubleCheckActiveServer(activeChannelHolder);
                reconnectDefaultServer(activeFuture, serverAddresses);
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    @Override
    public void shutdown() {
        active = false;
    }

    public class ChannelHolder {
        private ChannelFuture activeFuture;
        private int activeIndex = -1;
        private String activeServerConfig;
        private List<InetSocketAddress> serverAddresses;
        private String ip;
        private boolean connectChanged;

        public ChannelFuture getActiveFuture() {
            return activeFuture;
        }

        public int getActiveIndex() {
            return activeIndex;
        }

        public String getActiveServerConfig() {
            return activeServerConfig;
        }

        public String getIp() {
            return ip;
        }

        public List<InetSocketAddress> getServerAddresses() {
            return serverAddresses;
        }

        public boolean isConnectChanged() {
            return connectChanged;
        }

        public ChannelHolder setActiveFuture(ChannelFuture activeFuture) {
            this.activeFuture = activeFuture;
            return this;
        }

        public ChannelHolder setActiveIndex(int activeIndex) {
            this.activeIndex = activeIndex;
            return this;
        }

        public ChannelHolder setActiveServerConfig(String activeServerConfig) {
            this.activeServerConfig = activeServerConfig;
            return this;
        }

        public ChannelHolder setConnectChanged(boolean connectChanged) {
            this.connectChanged = connectChanged;
            return this;
        }

        public ChannelHolder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public ChannelHolder setServerAddresses(List<InetSocketAddress> serverAddresses) {
            this.serverAddresses = serverAddresses;
            return this;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("active future :").append(activeFuture.channel().remoteAddress());
            sb.append(" index:").append(activeIndex);
            sb.append(" ip:").append(ip);
            sb.append(" server config:").append(activeServerConfig);
            return sb.toString();
        }
    }

}