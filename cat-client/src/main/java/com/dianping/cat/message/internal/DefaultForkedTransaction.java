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
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;

public class DefaultForkedTransaction extends DefaultTransaction implements ForkedTransaction {
	private String m_rootMessageId;

	private String m_parentMessageId;

	private String m_forkedMessageId;

	public DefaultForkedTransaction(String type, String name, MessageManager manager) {
		super(type, name, manager);

		setStandalone(false);

		MessageTree tree = manager.getThreadLocalMessageTree();

		if (tree != null) {
			m_rootMessageId = tree.getRootMessageId();
			m_parentMessageId = tree.getMessageId();

			// Detach parent transaction and this forked transaction, by calling linkAsRunAway(), at this earliest moment,
			// so that thread synchronization is not needed at all between them in the future.
			m_forkedMessageId = Cat.createMessageId();
		}
	}

	@Override
	public void fork() {
		MessageManager manager = getManager();

		manager.setup();
		manager.start(this, false);

		MessageTree tree = manager.getThreadLocalMessageTree();

		if (tree != null) {
			// Override tree.messageId to be forkedMessageId of current forked transaction, which is created in the parent
			// thread.
			tree.setMessageId(m_forkedMessageId);
			tree.setRootMessageId(m_rootMessageId == null ? m_parentMessageId : m_rootMessageId);
			tree.setParentMessageId(m_parentMessageId);
		}
	}

	@Override
	public String getForkedMessageId() {
		return m_forkedMessageId;
	}
}
