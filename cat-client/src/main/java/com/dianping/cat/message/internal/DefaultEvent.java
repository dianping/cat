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

import java.io.PrintWriter;
import java.io.StringWriter;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.context.TraceContext;

public class DefaultEvent extends AbstractMessage implements Event {
	public DefaultEvent(TraceContext ctx, String type, String name) {
		super(type, name);

		ctx.add(this);
	}
	
	public DefaultEvent(TraceContext ctx, String message, Throwable e) {
		super("Error", e.getClass().getName());

		if (!ctx.hasException(e)) {
			StringWriter writer = new StringWriter(2048);

			if (message != null && message.length() > 0) {
				writer.append(message).append(' ');
			}

			setStatus("ERROR");
			e.printStackTrace(new PrintWriter(writer));
			addData(writer.toString());
			ctx.add(this);
		}
	}

	public DefaultEvent(String type, String name) {
		super(type, name);
	}

	@Override
	public void complete() {
		super.setCompleted();
	}

	@Override
	public void complete(long startInMillis) {
		setTimestamp(startInMillis);
		super.setCompleted();
	}
}
