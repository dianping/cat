package com.dianping.cat.message.spi.codec;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultHeartbeat;
import com.dianping.cat.message.internal.DefaultMetric;
import com.dianping.cat.message.internal.DefaultTrace;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class PlainTextMessageCodec implements MessageCodec, LogEnabled, Initializable {
	public static final String ID = "plain-text";

	private static final String VERSION = "PT1"; // plain text version 1

	private static final byte TAB = '\t'; // tab character

	private static final byte LF = '\n'; // line feed character

	private Map<String, Pair<Long, ChannelBuffer>> m_bufs = new ConcurrentHashMap<String, Pair<Long, ChannelBuffer>>();

	@Inject
	private BufferWriter m_writer = new EscapingBufferWriter();

	private BufferHelper m_bufferHelper = new BufferHelper(m_writer);

	private DateHelper m_dateHelper = new DateHelper();

	private Logger m_logger;

	private int m_maxDecodeNumber = 5000;

	@Override
	public MessageTree decode(ChannelBuffer buf) {
		MessageTree tree = new DefaultMessageTree();

		decode(buf, tree);
		return tree;
	}

	@Override
	public void decode(ChannelBuffer buf, MessageTree tree) {
		buf.markReaderIndex();

		String key = Thread.currentThread().getName();
		Pair<Long, ChannelBuffer> pair = m_bufs.get(key);

		if (pair == null) {
			pair = new Pair<Long, ChannelBuffer>(System.currentTimeMillis(), buf);

			m_bufs.put(key, pair);
		} else {
			pair.setKey(System.currentTimeMillis());
			pair.setValue(buf);
		}
		decodeHeader(buf, tree);
		if (buf.readableBytes() > 0) {
			decodeMessage(buf, tree);
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

		if (VERSION.equals(id)) {
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
		String type = helper.readRaw(buf, TAB);
		String name = helper.readRaw(buf, TAB);

		switch (identifier) {
		case 't':
			DefaultTransaction transaction = new DefaultTransaction(type, name, null);

			helper.read(buf, LF); // get rid of line feed
			transaction.setTimestamp(m_dateHelper.parse(timestamp));

			if (parent != null) {
				parent.addChild(transaction);
			}

			stack.push(parent);
			return transaction;
		case 'A':
			DefaultTransaction tran = new DefaultTransaction(type, name, null);
			String status = helper.readRaw(buf, TAB);
			String duration = helper.read(buf, TAB);
			String data = helper.readRaw(buf, TAB);

			helper.read(buf, LF); // get rid of line feed
			tran.setTimestamp(m_dateHelper.parse(timestamp));
			tran.setStatus(status);
			tran.addData(data);

			long d = Long.parseLong(duration.substring(0, duration.length() - 2));
			tran.setDurationInMicros(d);

			if (parent != null) {
				parent.addChild(tran);
				return parent;
			} else {
				return tran;
			}
		case 'T':
			String transactionStatus = helper.readRaw(buf, TAB);
			String transactionDuration = helper.read(buf, TAB);
			String transactionData = helper.readRaw(buf, TAB);

			helper.read(buf, LF); // get rid of line feed
			parent.setStatus(transactionStatus);
			parent.addData(transactionData);

			long transactionD = Long.parseLong(transactionDuration.substring(0, transactionDuration.length() - 2));

			parent.setDurationInMicros(transactionD);

			return stack.pop();
		case 'E':
			DefaultEvent event = new DefaultEvent(type, name);
			String eventStatus = helper.readRaw(buf, TAB);
			String eventData = helper.readRaw(buf, TAB);

			helper.read(buf, LF); // get rid of line feed
			event.setTimestamp(m_dateHelper.parse(timestamp));
			event.setStatus(eventStatus);
			event.addData(eventData);

			if (parent != null) {
				parent.addChild(event);
				return parent;
			} else {
				return event;
			}
		case 'M':
			DefaultMetric metric = new DefaultMetric(type, name);
			String metricStatus = helper.readRaw(buf, TAB);
			String metricData = helper.readRaw(buf, TAB);

			helper.read(buf, LF); // get rid of line feed
			metric.setTimestamp(m_dateHelper.parse(timestamp));
			metric.setStatus(metricStatus);
			metric.addData(metricData);

			if (parent != null) {
				parent.addChild(metric);
				return parent;
			} else {
				return metric;
			}
		case 'L':
			DefaultTrace trace = new DefaultTrace(type, name);
			String traceStatus = helper.readRaw(buf, TAB);
			String traceData = helper.readRaw(buf, TAB);

			helper.read(buf, LF); // get rid of line feed
			trace.setTimestamp(m_dateHelper.parse(timestamp));
			trace.setStatus(traceStatus);
			trace.addData(traceData);

			if (parent != null) {
				parent.addChild(trace);
				return parent;
			} else {
				return trace;
			}
		case 'H':
			DefaultHeartbeat heartbeat = new DefaultHeartbeat(type, name);
			String heartbeatStatus = helper.readRaw(buf, TAB);
			String heartbeatData = helper.readRaw(buf, TAB);

			helper.read(buf, LF); // get rid of line feed
			heartbeat.setTimestamp(m_dateHelper.parse(timestamp));
			heartbeat.setStatus(heartbeatStatus);
			heartbeat.addData(heartbeatData);

			if (parent != null) {
				parent.addChild(heartbeat);
				return parent;
			} else {
				return heartbeat;
			}
		default:
			m_logger.warn("Unknown identifier(" + (char) identifier + ") of message: "
			      + buf.toString(Charset.forName("utf-8")));
			throw new RuntimeException("Unknown identifier int name");
		}
	}

	protected void decodeMessage(ChannelBuffer buf, MessageTree tree) {
		Stack<DefaultTransaction> stack = new Stack<DefaultTransaction>();
		Message parent = decodeLine(buf, null, stack, tree);

		tree.setMessage(parent);

		int total = m_maxDecodeNumber;

		while (buf.readableBytes() > 0) {
			Message message = decodeLine(buf, (DefaultTransaction) parent, stack, tree);

			if (message instanceof DefaultTransaction) {
				parent = message;
			} else {
				break;
			}

			total--;
			if (total <= 0) {
				buf.resetReaderIndex();
				String messageTree = buf.toString(Charset.forName("utf-8"));
				m_logger.warn("Decode message in a dead loop" + messageTree);

				throw new RuntimeException("Error when decoding cat message! message tree:" + messageTree);
			}
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
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

		count += helper.write(buf, VERSION);
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
		count += helper.writeRaw(buf, message.getType());
		count += helper.write(buf, TAB);
		count += helper.writeRaw(buf, message.getName());
		count += helper.write(buf, TAB);

		if (policy != Policy.WITHOUT_STATUS) {
			count += helper.writeRaw(buf, message.getStatus());
			count += helper.write(buf, TAB);

			Object data = message.getData();

			if (policy == Policy.WITH_DURATION && message instanceof Transaction) {
				long duration = ((Transaction) message).getDurationInMicros();

				count += helper.write(buf, String.valueOf(duration));
				count += helper.write(buf, "us");
				count += helper.write(buf, TAB);
			}

			count += helper.writeRaw(buf, String.valueOf(data));
			count += helper.write(buf, TAB);
		}

		count += helper.write(buf, LF);

		return count;
	}

	public int encodeMessage(Message message, ChannelBuffer buf) {
		if (message instanceof Transaction) {
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
		} else if (message instanceof Event) {
			return encodeLine(message, buf, 'E', Policy.DEFAULT);
		} else if (message instanceof Trace) {
			return encodeLine(message, buf, 'L', Policy.DEFAULT);
		} else if (message instanceof Metric) {
			return encodeLine(message, buf, 'M', Policy.DEFAULT);
		} else if (message instanceof Heartbeat) {
			return encodeLine(message, buf, 'H', Policy.DEFAULT);
		} else {
			throw new RuntimeException(String.format("Unsupported message type: %s.", message));
		}
	}

	public void setBufferWriter(BufferWriter writer) {
		m_writer = writer;
		m_bufferHelper = new BufferHelper(m_writer);
	}

	protected class BufferHelper {

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
			try {
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
			} finally {
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

		private Map<String, Long> m_map = new ConcurrentHashMap<String, Long>();

		public String format(long timestamp) {
			SimpleDateFormat format = m_queue.poll();

			if (format == null) {
				format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
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
			int len = str.length();
			String date = str.substring(0, 10);
			Long baseline = m_map.get(date);

			if (baseline == null) {
				try {
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

					format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
					baseline = format.parse(date).getTime();
					m_map.put(date, baseline);
				} catch (ParseException e) {
					return -1;
				}
			}

			long time = baseline.longValue();
			long metric = 1;
			boolean millisecond = true;

			for (int i = len - 1; i > 10; i--) {
				char ch = str.charAt(i);

				if (ch >= '0' && ch <= '9') {
					time += (ch - '0') * metric;
					metric *= 10;
				} else if (millisecond) {
					millisecond = false;
				} else {
					metric = metric / 100 * 60;
				}
			}
			return time;
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
	public void initialize() throws InitializationException {
		Threads.forGroup("Cat").start(new PrintThread());
	}

	public class PrintThread implements Task {

		@Override
		public void run() {
			while (true) {
				for (Entry<String, Pair<Long, ChannelBuffer>> entry : m_bufs.entrySet()) {
					Pair<Long, ChannelBuffer> pair = entry.getValue();

					if (System.currentTimeMillis() - pair.getKey() > 1000) {
						ChannelBuffer channelBuffer = pair.getValue();

						channelBuffer.markReaderIndex();
						m_logger.info("====" + channelBuffer.toString(Charset.forName("utf-8")) + "====");
					}
				}

				try {
					Thread.sleep(1000 * 5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public String getName() {
			return "print-thread";
		}

		@Override
		public void shutdown() {

		}

	}

}
