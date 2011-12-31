package com.dianping.cat.message.spi.codec;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.StringRope;

public class CopyOfPlainTextMessageCodec implements MessageCodec {
	private static final String ID = "PT1"; // plain text version 1

	private static final String TAB = "\t";

	private static final String LF = "\n";

	private SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.sss");

	@Override
	public void decode(ChannelBuffer buf, MessageTree tree) {
		// TODO Auto-generated method stub

	}

	public StringRope encode(MessageTree tree) {
		StringRope rope = new StringRope(1024);

		encodeHeader(rope, tree);
		encodeMessage(rope, tree.getMessage());
		return rope;
	}

	protected void encodeHeader(StringRope rope, MessageTree tree) {
		rope.add(ID);
		rope.add(tree.getDomain()).add(TAB);
		rope.add(tree.getHostName()).add(TAB);
		rope.add(String.valueOf(tree.getPort())).add(TAB);
		rope.add(tree.getIpAddress()).add(TAB);
		rope.add(tree.getThreadId()).add(TAB);
		rope.add(tree.getMessageId()).add(TAB);
		rope.add(tree.getRequestToken()).add(TAB);
		rope.add(tree.getSessionToken()).add(TAB);
		rope.add(LF);
	}

	protected void encodeLine(StringRope rope, Message message, String type, Policy policy) {
		rope.add(type);
		rope.add(m_dateFormat.format(new Date(message.getTimestamp()))).add(TAB);
		rope.add(message.getType()).add(TAB);
		rope.add(message.getName()).add(TAB);

		if (policy != Policy.WITHOUT_STATUS) {
			rope.add(message.getStatus()).add(TAB);

			Object data = message.getData();

			if (policy == Policy.WITH_DURATION && message instanceof Transaction) {
				long duration = ((Transaction) message).getDuration();

				rope.add(String.valueOf(duration)).add("ms").add(TAB);
			}

			if (data instanceof StringRope) {
				rope.add((StringRope) message.getData()).add(TAB);
			} else {
				rope.add(String.valueOf(data), true).add(TAB);
			}
		}

		rope.add(LF);
	}

	protected void encodeMessage(StringRope rope, Message message) {
		if (message instanceof Event) {
			encodeLine(rope, message, "E", Policy.DEFAULT);
		} else if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;
			List<Message> children = transaction.getChildren();

			if (children.isEmpty()) {
				encodeLine(rope, message, "A", Policy.WITH_DURATION);
			} else {
				encodeLine(rope, message, "t", Policy.WITHOUT_STATUS);

				for (Message child : children) {
					encodeMessage(rope, child);
				}

				encodeLine(rope, message, "T", Policy.WITH_DURATION);
			}
		} else if (message instanceof Heartbeat) {
			encodeLine(rope, message, "H", Policy.DEFAULT);
		} else {
			throw new RuntimeException(String.format("Unsupported message type: %s.", message.getClass()));
		}
	}

	protected static enum Policy {
		DEFAULT,

		WITHOUT_STATUS,

		WITH_DURATION;
	}

	@Override
	public void encode(MessageTree tree, ChannelBuffer buf) {

	}
}
