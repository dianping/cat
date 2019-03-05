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
package com.dianping.cat.message.spi.internal;

import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultMessageStatistics implements MessageStatistics {
    private AtomicLong produced = new AtomicLong();
    private AtomicLong overflowed = new AtomicLong();
    private AtomicLong bytes = new AtomicLong();

    @Override
    public void onBytes(int bytes) {
        this.bytes.addAndGet(bytes);
        produced.incrementAndGet();
    }

    @Override
    public void onOverflowed(MessageTree tree) {
        overflowed.incrementAndGet();
    }

    @Override
    public Map<String, Long> getStatistics() {
        Map<String, Long> map = new HashMap<String, Long>();

        map.put("cat.status.message.produced", produced.get());
        produced = new AtomicLong();

        map.put("cat.status.message.overflowed", overflowed.get());
        overflowed = new AtomicLong();

        map.put("cat.status.message.bytes", bytes.get());
        bytes = new AtomicLong();

        return map;
    }
}
