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
package com.dianping.cat.report.service;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultMessageProducer;

public abstract class ModelServiceWithCalSupport extends ContainerHolder {
	private Transaction m_current;

	protected void logError(Throwable cause) {
		StringWriter writer = new StringWriter(2048);

		cause.printStackTrace(new PrintWriter(writer));

		if (cause instanceof Error) {
			logEvent("Error", cause.getClass().getName(), "ERROR", writer.toString());
		} else if (cause instanceof RuntimeException) {
			logEvent("RuntimeException", cause.getClass().getName(), "ERROR", writer.toString());
		} else {
			logEvent("Exception", cause.getClass().getName(), "ERROR", writer.toString());
		}
	}

	protected void logEvent(String type, String name, String status, String nameValuePairs) {
		DefaultEvent event = new DefaultEvent(type, name);

		m_current.addChild(event);

		if (nameValuePairs != null && nameValuePairs.length() > 0) {
			event.addData(nameValuePairs);
		}
		event.setStatus(status);
		event.complete();
	}

	protected Transaction newTransaction(String type, String name) {
		DefaultMessageProducer cat = (DefaultMessageProducer) Cat.getProducer();

		return cat.newTransaction(m_current, type, name);
	}

	protected void setParentTransaction(Transaction current) {
		m_current = current;
	}
}
