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
package com.dianping.cat.message.spi.codec;

import io.netty.buffer.ByteBuf;

public class EscapingBufferWriter implements BufferWriter {
	public static final String ID = "escape";

	@Override
	public int writeTo(ByteBuf buf, byte[] data) {
		int len = data.length;
		int count = len;
		int offset = 0;

		for (int i = 0; i < len; i++) {
			byte b = data[i];

			if (b == '\t' || b == '\r' || b == '\n' || b == '\\') {
				buf.writeBytes(data, offset, i - offset);
				buf.writeByte('\\');

				if (b == '\t') {
					buf.writeByte('t');
				} else if (b == '\r') {
					buf.writeByte('r');
				} else if (b == '\n') {
					buf.writeByte('n');
				} else {
					buf.writeByte(b);
				}

				count++;
				offset = i + 1;
			}
		}

		if (len > offset) {
			buf.writeBytes(data, offset, len - offset);
		}

		return count;
	}
}
