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
package com.dianping.cat.alarm.spi.receiver;

import java.util.ArrayList;
import java.util.List;

import org.unidal.helper.Splitters;

import com.dianping.cat.alarm.receiver.entity.Receiver;

public abstract class DefaultContactor {

	protected List<String> buildDefaultMailReceivers(Receiver receiver) {
		List<String> mailReceivers = new ArrayList<String>();

		if (receiver != null) {
			mailReceivers.addAll(receiver.getEmails());
		}
		return mailReceivers;
	}

	protected List<String> buildDefaultSMSReceivers(Receiver receiver) {
		List<String> smsReceivers = new ArrayList<String>();

		if (receiver != null) {
			smsReceivers.addAll(receiver.getPhones());
		}
		return smsReceivers;
	}

	protected List<String> buildDefaultDXReceivers(Receiver receiver) {
		List<String> receivers = new ArrayList<String>();

		if (receiver != null) {
			receivers.addAll(receiver.getDxs());
		}
		return receivers;
	}

	protected List<String> buildDefaultWeixinReceivers(Receiver receiver) {
		List<String> weixinReceivers = new ArrayList<String>();

		if (receiver != null) {
			weixinReceivers.addAll(receiver.getWeixins());
		}
		return weixinReceivers;
	}

	protected List<String> split(String str) {
		List<String> result = new ArrayList<String>();

		if (str != null) {
			result.addAll(Splitters.by(",").noEmptyItem().trim().split(str));
		}

		return result;
	}
}
