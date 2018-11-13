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
package org.unidal.cat.message.storage;

import java.io.IOException;
import java.util.Map;

import io.netty.buffer.ByteBuf;

import com.dianping.cat.message.internal.MessageId;

public interface Bucket {
	public static final long SEGMENT_SIZE = 32 * 1024L;

	public static final int BYTE_PER_MESSAGE = 8;

	public static final int BYTE_PER_ENTRY = 8;

	public static final int MESSAGE_PER_SEGMENT = (int) (SEGMENT_SIZE / BYTE_PER_MESSAGE);

	public static final int ENTRY_PER_SEGMENT = (int) (SEGMENT_SIZE / BYTE_PER_ENTRY);

	public void close();

	public void flush();

	public ByteBuf get(MessageId id) throws IOException;

	public boolean initialize(String domain, String ip, int hour, boolean writeMode) throws IOException;

	public void puts(ByteBuf buf, Map<MessageId, Integer> mappings) throws IOException;
}
