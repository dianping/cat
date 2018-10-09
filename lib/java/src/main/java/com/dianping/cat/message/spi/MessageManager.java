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
package com.dianping.cat.message.spi;

import com.dianping.cat.configuration.ClientConfigService;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageManager;

/**
 * Message manager to help build CAT message.
 * <p>
 * <p>
 * Notes: This method is reserved for internal usage only. Application developer should never call this method directly.
 */
public interface MessageManager {
    void add(Message message);

    void end(Transaction transaction);

    ClientConfigService getConfigService();

    String getDomain();

    Transaction getPeekTransaction();

    MessageTree getThreadLocalMessageTree();

    boolean hasContext();

    boolean isCatEnabled();

    boolean isMessageEnabled();

    boolean isTraceMode();

    void reset();

    void setTraceMode(boolean traceMode);

    void setup();

    void start(Transaction transaction, boolean forked);

    DefaultMessageManager.Context getContext();

}