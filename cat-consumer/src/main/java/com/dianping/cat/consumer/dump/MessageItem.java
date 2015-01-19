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

	public MessageTree getTree() {
		return m_tree;
	}

	public void setMessageId(MessageId messageId) {
		m_messageId = messageId;
	}

	public void setTree(MessageTree tree) {
		m_tree = tree;
	}

}