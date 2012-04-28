package com.dianping.cat.message.spi.codec;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultHeartbeat;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.StringRope;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.lookup.annotation.Inject;

public class PlainTextMessageCodec implements MessageCodec, LogEnabled {
	private static final String ID = "PT1"; // plain text version 1

	private static final byte TAB = '\t'; // tab character

	private static final byte LF = '\n'; // line feed character

	@Inject
	private BufferWriter m_writer = new EscapingBufferWriter();

	private BufferHelper m_bufferHelper = new BufferHelper(m_writer);

	private DateHelper m_dateHelper = new DateHelper();

	private Logger m_logger;

	@Override
	public MessageTree decode(ChannelBuffer buf) {
		DefaultMessageTree tree = new DefaultMessageTree();

		decode(buf, tree);
		return tree;
	}

	@Override
	public void decode(ChannelBuffer buf, MessageTree tree) {
		buf.markReaderIndex();

		decodeHeader(buf, tree);

		if (buf.readableBytes() > 0) {
			decodeMessage(buf, tree);
		} else {
			m_logger.warn("Message received without root message!");
		}
	}

	protected void decodeHeader(ChannelBuffer buf, MessageTree tree) {
		BufferHelper helper = m_bufferHelper;
		String id = helper.read(buf, TAB);
		String domain = helper.read(buf, TAB);
		String hostName = helper.read(buf, TAB);
		String ipAddress = helper.read(buf, TAB);
		String threadGroupName = helper.read(buf, TAB);
		String threadId = helper.read(buf, TAB);
		String threadName = helper.read(buf, TAB);
		String messageId = helper.read(buf, TAB);
		String parentMessageId = helper.read(buf, TAB);
		String rootMessageId = helper.read(buf, TAB);
		String sessionToken = helper.read(buf, LF);

		if (ID.equals(id)) {
			tree.setDomain(domain);
			tree.setHostName(hostName);
			tree.setIpAddress(ipAddress);
			tree.setThreadGroupName(threadGroupName);
			tree.setThreadId(threadId);
			tree.setThreadName(threadName);
			tree.setMessageId(messageId);
			tree.setParentMessageId(parentMessageId);
			tree.setRootMessageId(rootMessageId);
			tree.setSessionToken(sessionToken);
		} else {
			throw new RuntimeException(String.format("Unrecognized id(%s) for plain text message codec!", id));
		}
	}

	protected Message decodeLine(ChannelBuffer buf, DefaultTransaction parent, Stack<DefaultTransaction> stack,
	      MessageTree tree) {
		BufferHelper helper = m_bufferHelper;
		byte identifier = buf.readByte();
		String timestamp = helper.read(buf, TAB);
		String type = helper.read(buf, TAB);
		String name = helper.read(buf, TAB);

		if (identifier == 'E') {
			DefaultEvent event = new DefaultEvent(type, name);
			String status = helper.read(buf, TAB);
			String data = helper.readRaw(buf, TAB);

			helper.read(buf, LF); // get rid of line feed
			event.setTimestamp(m_dateHelper.parse(timestamp));
			event.setStatus(status);
			event.addData(data);

			if (parent != null) {
				parent.addChild(event);
				return parent;
			} else {
				return event;
			}
		} else if (identifier == 'H') {
			DefaultHeartbeat heartbeat = new DefaultHeartbeat(type, name);
			String status = helper.read(buf, TAB);
			String data = helper.readRaw(buf, TAB);

			helper.read(buf, LF); // get rid of line feed
			heartbeat.setTimestamp(m_dateHelper.parse(timestamp));
			heartbeat.setStatus(status);
			heartbeat.addData(data);

			if (parent != null) {
				parent.addChild(heartbeat);
				return parent;
			} else {
				return heartbeat;
			}
		} else if (identifier == 't') {
			DefaultTransaction transaction = new DefaultTransaction(type, name, null);

			helper.read(buf, LF); // get rid of line feed
			transaction.setTimestamp(m_dateHelper.parse(timestamp));

			if (parent != null) {
				parent.addChild(transaction);
			}

			stack.push(parent);
			return transaction;
		} else if (identifier == 'A') {
			DefaultTransaction transaction = new DefaultTransaction(type, name, null);
			String status = helper.read(buf, TAB);
			String duration = helper.read(buf, TAB);
			String data = helper.readRaw(buf, TAB);

			helper.read(buf, LF); // get rid of line feed
			transaction.setTimestamp(m_dateHelper.parse(timestamp));
			transaction.setStatus(status);
			transaction.addData(data);

			long d = Long.parseLong(duration.substring(0, duration.length() - 2));
			transaction.setDurationInMicros(d);

			if (parent != null) {
				parent.addChild(transaction);
				return parent;
			} else {
				return transaction;
			}
		} else if (identifier == 'T') {
			String status = helper.read(buf, TAB);
			String duration = helper.read(buf, TAB);
			String data = helper.readRaw(buf, TAB);

			helper.read(buf, LF); // get rid of line feed
			parent.setStatus(status);
			parent.addData(data);

			long d = Long.parseLong(duration.substring(0, duration.length() - 2));
			parent.setDurationInMicros(d);

			return stack.pop();
		} else {
			buf.resetReaderIndex();
			m_logger.warn("Unknown identifier(" + identifier + ") of message: " + buf.toString(Charset.forName("utf-8")));

			// unknown message, ignore it
			return parent;
		}
	}

	protected void decodeMessage(ChannelBuffer buf, MessageTree tree) {
		Stack<DefaultTransaction> stack = new Stack<DefaultTransaction>();
		Message parent = decodeLine(buf, null, stack, tree);

		tree.setMessage(parent);

		while (buf.readableBytes() > 0) {
			Message message = decodeLine(buf, (DefaultTransaction) parent, stack, tree);

			if (message instanceof DefaultTransaction) {
				parent = message;
			} else {
				break;
			}
		}
	}

	@Override
	public void encode(MessageTree tree, ChannelBuffer buf) {
		int count = 0;
		int index = buf.writerIndex();

		buf.writeInt(0); // place-holder
		count += encodeHeader(tree, buf);

		if (tree.getMessage() != null) {
			count += encodeMessage(tree.getMessage(), buf);
		}

		buf.setInt(index, count);
	}

	protected int encodeHeader(MessageTree tree, ChannelBuffer buf) {
		BufferHelper helper = m_bufferHelper;
		int count = 0;

		count += helper.write(buf, ID);
		count += helper.write(buf, TAB);
		count += helper.write(buf, tree.getDomain());
		count += helper.write(buf, TAB);
		count += helper.write(buf, tree.getHostName());
		count += helper.write(buf, TAB);
		count += helper.write(buf, tree.getIpAddress());
		count += helper.write(buf, TAB);
		count += helper.write(buf, tree.getThreadGroupName());
		count += helper.write(buf, TAB);
		count += helper.write(buf, tree.getThreadId());
		count += helper.write(buf, TAB);
		count += helper.write(buf, tree.getThreadName());
		count += helper.write(buf, TAB);
		count += helper.write(buf, tree.getMessageId());
		count += helper.write(buf, TAB);
		count += helper.write(buf, tree.getParentMessageId());
		count += helper.write(buf, TAB);
		count += helper.write(buf, tree.getRootMessageId());
		count += helper.write(buf, TAB);
		count += helper.write(buf, tree.getSessionToken());
		count += helper.write(buf, LF);

		return count;
	}

	protected int encodeLine(Message message, ChannelBuffer buf, char type, Policy policy) {
		BufferHelper helper = m_bufferHelper;
		int count = 0;

		count += helper.write(buf, (byte) type);

		if (type == 'T' && message instanceof Transaction) {
			long duration = ((Transaction) message).getDurationInMillis();

			count += helper.write(buf, m_dateHelper.format(message.getTimestamp() + duration));
		} else {
			count += helper.write(buf, m_dateHelper.format(message.getTimestamp()));
		}

		count += helper.write(buf, TAB);
		count += helper.write(buf, message.getType());
		count += helper.write(buf, TAB);
		count += helper.write(buf, message.getName());
		count += helper.write(buf, TAB);

		if (policy != Policy.WITHOUT_STATUS) {
			count += helper.write(buf, message.getStatus());
			count += helper.write(buf, TAB);

			Object data = message.getData();

			if (policy == Policy.WITH_DURATION && message instanceof Transaction) {
				long duration = ((Transaction) message).getDurationInMicros();

				count += helper.write(buf, String.valueOf(duration));
				count += helper.write(buf, "us");
				count += helper.write(buf, TAB);
			}

			if (data instanceof StringRope) {
				StringRope rope = (StringRope) data;

				count += rope.writeTo(buf, m_writer);
				count += helper.write(buf, TAB);
			} else {
				count += helper.writeRaw(buf, String.valueOf(data));
				count += helper.write(buf, TAB);
			}
		}

		count += helper.write(buf, LF);

		return count;
	}

	public int encodeMessage(Message message, ChannelBuffer buf) {
		if (message instanceof Event) {
			return encodeLine(message, buf, 'E', Policy.DEFAULT);
		} else if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;
			List<Message> children = transaction.getChildren();

			if (children.isEmpty()) {
				return encodeLine(transaction, buf, 'A', Policy.WITH_DURATION);
			} else {
				int count = 0;
				int len = children.size();

				count += encodeLine(transaction, buf, 't', Policy.WITHOUT_STATUS);

				for (int i = 0; i < len; i++) {
					Message child = children.get(i);

					count += encodeMessage(child, buf);
				}

				count += encodeLine(transaction, buf, 'T', Policy.WITH_DURATION);

				return count;
			}
		} else if (message instanceof Heartbeat) {
			return encodeLine(message, buf, 'H', Policy.DEFAULT);
		} else {
			throw new RuntimeException(String.format("Unsupported message type: %s.", message.getClass()));
		}
	}

	public void setBufferWriter(BufferWriter writer) {
		m_writer = writer;
		m_bufferHelper = new BufferHelper(m_writer);
	}

	protected static class BufferHelper {
		private BufferWriter m_writer;

		public BufferHelper(BufferWriter writer) {
			m_writer = writer;
		}

		public String read(ChannelBuffer buf, byte separator) {
			int count = buf.bytesBefore(separator);

			if (count < 0) {
				return null;
			} else {
				byte[] data = new byte[count];

				buf.readBytes(data);
				buf.readByte(); // get rid of separator
				return new String(data);
			}
		}

		public String readRaw(ChannelBuffer buf, byte separator) {
			int count = buf.bytesBefore(separator);

			if (count < 0) {
				return null;
			} else {
				byte[] data = new byte[count];
				String str;

				buf.readBytes(data);
				buf.readByte(); // get rid of separator

				int length = data.length;

				for (int i = 0; i < length; i++) {
					if (data[i] == '\\') {
						if (i + 1 < length) {
							byte b = data[i + 1];

							if (b == 't') {
								data[i] = '\t';
							} else if (b == 'r') {
								data[i] = '\r';
							} else if (b == 'n') {
								data[i] = '\n';
							} else {
								data[i] = b;
							}

							System.arraycopy(data, i + 2, data, i + 1, length - i - 2);
							length--;
						}
					}
				}

				try {
					str = new String(data, 0, length, "utf-8");
				} catch (UnsupportedEncodingException e) {
					str = new String(data, 0, length);
				}

				return str;
			}
		}

		public int write(ChannelBuffer buf, byte b) {
			buf.writeByte(b);
			return 1;
		}

		public int write(ChannelBuffer buf, String str) {
			if (str == null) {
				str = "null";
			}

			byte[] data = str.getBytes();

			buf.writeBytes(data);
			return data.length;
		}

		public int writeRaw(ChannelBuffer buf, String str) {
			if (str == null) {
				str = "null";
			}

			byte[] data;

			try {
				data = str.getBytes("utf-8");
			} catch (UnsupportedEncodingException e) {
				data = str.getBytes();
			}

			return m_writer.writeTo(buf, data);
		}
	}

	/**
	 * Thread safe date helper class. DateFormat is NOT thread safe.
	 */
	protected static class DateHelper {
		private BlockingQueue<SimpleDateFormat> m_queue = new ArrayBlockingQueue<SimpleDateFormat>(20);

		public String format(long timestamp) {
			SimpleDateFormat format = m_queue.poll();

			if (format == null) {
				format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			}

			try {
				return format.format(new Date(timestamp));
			} finally {
				if (m_queue.remainingCapacity() > 0) {
					m_queue.offer(format);
				}
			}
		}

		public long parse(String str) {
			SimpleDateFormat format = m_queue.poll();

			if (format == null) {
				format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			}

			try {
				return format.parse(str).getTime();
			} catch (ParseException e) {
				return -1;
			} finally {
				if (m_queue.remainingCapacity() > 0) {
					m_queue.offer(format);
				}
			}
		}
	}

	protected static enum Policy {
		DEFAULT,

		WITHOUT_STATUS,

		WITH_DURATION;

		public static Policy getByMessageIdentifier(byte identifier) {
			switch (identifier) {
			case 't':
				return WITHOUT_STATUS;
			case 'T':
			case 'A':
				return WITH_DURATION;
			case 'E':
			case 'H':
				return DEFAULT;
			default:
				return DEFAULT;
			}
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
