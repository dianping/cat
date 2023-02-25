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
package com.dianping.cat.message.codec;

import io.netty.buffer.ByteBuf;

import com.dianping.cat.message.spi.codec.BufferWriter;

public class HtmlEncodingBufferWriter implements BufferWriter {
	public static final String ID = "html";

	private static byte[] AMP = "&amp;".getBytes();

	private static byte[] LT = "&lt;".getBytes();

	private static byte[] GT = "&gt;".getBytes();

	private static byte[] BR = "<br>".getBytes();

	@Override
	public int writeTo(ByteBuf buffer, byte[] data) {
		int len = data.length;
		int count = len;
		int offset = 0;

		for (int i = 0; i < len; i++) {
			byte b = data[i];

			if (b == '&') {
				buffer.writeBytes(data, offset, i - offset);
				buffer.writeBytes(AMP);
				count += AMP.length - 1;
				offset = i + 1;
			} else if (b == '<') {
				buffer.writeBytes(data, offset, i - offset);
				buffer.writeBytes(LT);
				count += LT.length - 1;
				offset = i + 1;
			} else if (b == '>') {
				buffer.writeBytes(data, offset, i - offset);
				buffer.writeBytes(GT);
				count += GT.length - 1;
				offset = i + 1;
			} else if (b == '\n') {
				// we want '\n' be output again for better format
				buffer.writeBytes(data, offset, i - offset + 1);
				buffer.writeBytes(BR);
				count += BR.length;
				offset = i + 1;
			}
		}

		buffer.writeBytes(data, offset, len - offset);
		return count;
	}
}
