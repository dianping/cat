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
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.util.Splitters;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageIdFactory {
    private volatile long timestamp = getTimestamp();
    private volatile AtomicInteger index = new AtomicInteger(0);
    private String domain = "UNKNOWN";
    private String ipAddress;
    private MappedByteBuffer byteBuffer;
    private RandomAccessFile markFile;
    private int retry;
    private String idPrefix;
    private String idPrefixOfMultiMode;
    private static final long HOUR = 3600 * 1000L;
    private static MessageIdFactory INSTANCE = new MessageIdFactory();
    private Map<String, AtomicInteger> map = new ConcurrentHashMap<String, AtomicInteger>(100);

    public static MessageIdFactory getInstance() {
        return INSTANCE;
    }

    private MessageIdFactory() {
    }

    public void close() {
        try {
            saveMark();
            markFile.close();
        } catch (Exception e) {
            // ignore it
        }
    }

    private File createMarkFile(String domain) {
        File mark = new File(Cat.getCatHome(), "cat-" + domain + ".mark");

        if (!mark.exists()) {
            boolean success;
            try {
                success = mark.createNewFile();
            } catch (Exception e) {
                success = false;
            }
            if (!success) {
                mark = createTempFile(domain);
            }
        } else if (!mark.canWrite()) {
            mark = createTempFile(domain);
        }
        return mark;
    }

    private File createTempFile(String domain) {
        String tmpDir = System.getProperty("java.io.tmpdir");

        return new File(tmpDir, "cat-" + domain + ".mark");
    }

    public String getNextId() {
        long timestamp = getTimestamp();

        if (timestamp != this.timestamp) {
            synchronized (this) {
                if (timestamp != this.timestamp) {
                    resetCounter(timestamp);
                }
            }
        }

        int index = this.index.getAndIncrement();
        StringBuilder sb = new StringBuilder(64);

        if (Cat.isMultiInstanceEnable()) {
            sb.append(idPrefixOfMultiMode).append(index);
        } else {
            sb.append(idPrefix).append(index);
        }

        return sb.toString();
    }

    public String getNextId(String domain) {
        if (domain.equals(this.domain)) {
            return getNextId();
        } else {
            long timestamp = getTimestamp();

            if (timestamp != this.timestamp) {
                synchronized (this) {
                    if (timestamp != this.timestamp) {
                        resetCounter(timestamp);
                    }
                }
            }

            AtomicInteger value = map.get(domain);

            if (value == null) {
                synchronized (map) {
                    value = map.get(domain);

                    if (value == null) {
                        value = new AtomicInteger(0);
                        map.put(domain, value);
                    }
                }
            }
            int index = value.getAndIncrement();
            StringBuilder sb = new StringBuilder(this.domain.length() + 32);

            sb.append(domain).append('-').append(ipAddress).append('-').append(timestamp).append('-').append(index);

            return sb.toString();
        }
    }

    private int getProcessID() {
        try {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            return Integer.valueOf(runtimeMXBean.getName().split("@")[0]);
        } catch (Exception e) {
            Cat.logError(e);
        }
        return -1;
    }

    protected long getTimestamp() {
        return System.currentTimeMillis() / HOUR;
    }

    public void initialize(String domain) throws IOException {
        String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
        List<String> items = Splitters.by(".").noEmptyItem().split(ip);
        byte[] bytes = new byte[4];

        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) Integer.parseInt(items.get(i));
        }

        StringBuilder sb = new StringBuilder(bytes.length / 2);

        for (byte b : bytes) {
            sb.append(Integer.toHexString((b >> 4) & 0x0F));
            sb.append(Integer.toHexString(b & 0x0F));
        }

        this.domain = domain;
        ipAddress = sb.toString();
        File mark = createMarkFile(domain);

        markFile = new RandomAccessFile(mark, "rw");
        byteBuffer = markFile.getChannel().map(MapMode.READ_WRITE, 0, 1024 * 1024L);
        idPrefix = initIdPrefix(getTimestamp(), false);
        idPrefixOfMultiMode = initIdPrefix(getTimestamp(), true);

        if (byteBuffer.limit() > 0) {
            try {
                long lastTimestamp = byteBuffer.getLong();
                int index = byteBuffer.getInt();

                if (lastTimestamp == timestamp) {
                    this.index = new AtomicInteger(index + 1000);

                    int mapLength = byteBuffer.getInt();

                    for (int i = 0; i < mapLength; i++) {
                        int domainLength = byteBuffer.getInt();
                        byte[] domainArray = new byte[domainLength];

                        byteBuffer.get(domainArray);
                        int value = byteBuffer.getInt();

                        map.put(new String(domainArray), new AtomicInteger(value + 1000));
                    }
                } else {
                    this.index = new AtomicInteger(0);
                }
            } catch (Exception e) {
                retry++;

                if (retry == 1) {
                    mark.delete();
                    initialize(domain);
                }
            }
        }

        saveMark();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                close();
            }
        });
    }

    private String initIdPrefix(long timestamp, boolean multiMode) {
        StringBuilder sb = new StringBuilder(domain.length() + 32);
        int processID = getProcessID();

        if (multiMode && processID > 0) {
            sb.append(domain).append('-').append(ipAddress).append(".").append(processID).append('-').append(timestamp).append('-');
        } else {
            sb.append(domain).append('-').append(ipAddress).append('-').append(timestamp).append('-');
        }

        return sb.toString();
    }

    private void resetCounter(long timestamp) {
        index.set(0);

        for (Entry<String, AtomicInteger> entry : map.entrySet()) {
            entry.getValue().set(0);
        }

        idPrefix = initIdPrefix(timestamp, false);
        idPrefixOfMultiMode = initIdPrefix(timestamp, true);

        this.timestamp = timestamp;
    }

    public synchronized void saveMark() {
        try {
            byteBuffer.rewind();
            byteBuffer.putLong(timestamp);
            byteBuffer.putInt(index.get());
            byteBuffer.putInt(map.size());

            for (Entry<String, AtomicInteger> entry : map.entrySet()) {
                byte[] bytes = entry.getKey().toString().getBytes();

                byteBuffer.putInt(bytes.length);
                byteBuffer.put(bytes);
                byteBuffer.putInt(entry.getValue().get());
            }

            byteBuffer.force();
        } catch (Throwable e) {
            // ignore it
        }
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

}
