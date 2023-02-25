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
package com.dianping.cat.consumer.dump;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;

public class MessageItem {
	private MessageTree m_tree;

	private MessageId m_messageId;

	public MessageItem(MessageTree tree, MessageId messageId) {
		m_tree = tree;
		m_messageId = messageId;
	}

	public MessageId getMessageId() {
		return m_messageId;
	}

	public void setMessageId(MessageId messageId) {
		m_messageId = messageId;
	}

	public MessageTree getTree() {
		return m_tree;
	}

	public void setTree(MessageTree tree) {
		m_tree = tree;
	}

}