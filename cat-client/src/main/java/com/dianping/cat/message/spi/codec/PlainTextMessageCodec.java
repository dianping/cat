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
package com.dianping.cat.message.spi.codec;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import com.dianping.cat.Cat;
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
import com.dianping.cat.message.io.BufReleaseHelper;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class PlainTextMessageCodec implements MessageCodec {
	public static final String ID = "plain-text";

	private static final String VERSION = "PT1"; // plain text version 1

	private static final byte TAB = '\t'; // tab character

	private static final byte LF = '\n'; // line feed character

	private BufferWriter m_writer = new EscapingBufferWriter();

	private BufferHelper m_bufferHelper = new BufferHelper(m_writer);

	private DateHelper m_dateHelper = new DateHelper();

	private ThreadLocal<Context> m_ctx;

	public static String encodeTree(MessageTree tree) {
		String result = "";
		ByteBuf buf = null;

		try {
			PlainTextMessageCodec codec = new PlainTextMessageCodec();

			buf = codec.encode(tree);
			buf.readInt(); // get rid of length
			result = buf.toString(Charset.forName("utf-8"));
		} catch (Exception ex) {
			Cat.logError(ex);
		} finally {
			BufReleaseHelper.release(buf);
		}

		return result;
	}

	@Override
	public MessageTree decode(ByteBuf buf) {
		buf.readInt(); // read the length of the message tree
		MessageTree tree = new DefaultMessageTree();

		decode(buf, tree);
		return tree;
	}

	private void decode(ByteBuf buf, MessageTree tree) {
		if (m_ctx == null) {
			m_ctx = new ThreadLocal<Context>() {
				@Override
				protected Context initialValue() {
					return new Context();
				}
			};
		}

		Context ctx = m_ctx.get().setBuffer(buf);

		try {
			decodeHeader(ctx, tree);

			if (buf.readableBytes() > 0) {
				decodeMessage(ctx, tree);
			}
		} finally {
			ctx.removeBuf();
		}
	}

	protected void decodeHeader(Context ctx, MessageTree tree) {
		BufferHelper helper = m_bufferHelper;
		String id = helper.read(ctx, TAB);
		String domain = helper.read(ctx, TAB);
		String hostName = helper.read(ctx, TAB);
		String ipAddress = helper.read(ctx, TAB);
		String threadGroupName = helper.read(ctx, TAB);
		String threadId = helper.read(ctx, TAB);
		String threadName = helper.read(ctx, TAB);
		String messageId = helper.read(ctx, TAB);
		String parentMessageId = helper.read(ctx, TAB);
		String rootMessageId = helper.read(ctx, TAB);
		String sessionToken = helper.read(ctx, LF);

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

	protected Message decodeLine(Context ctx, DefaultTransaction parent, Stack<DefaultTransaction> stack,
							MessageTree tree) {
		BufferHelper helper = m_bufferHelper;
		byte identifier = ctx.getBuffer().readByte();
		String timestamp = helper.read(ctx, TAB);
		String type = helper.read(ctx, TAB);
		String name = helper.read(ctx, TAB);

		switch (identifier) {
		case 't':
			DefaultTransaction transaction = new DefaultTransaction(type, name);

			tree.findOrCreateTransactions().add(transaction);

			helper.read(ctx, LF); // get rid of line feed
			transaction.setTimestamp(m_dateHelper.parse(timestamp));

			if (parent != null) {
				parent.addChild(transaction);
			}

			stack.push(parent);
			return transaction;
		case 'A':
			DefaultTransaction tran = new DefaultTransaction(type, name);

			tree.findOrCreateTransactions().add(tran);

			String status = helper.read(ctx, TAB);
			String duration = helper.read(ctx, TAB);
			String data = helper.read(ctx, TAB);

			helper.read(ctx, LF); // get rid of line feed
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
			String transactionStatus = helper.read(ctx, TAB);
			String transactionDuration = helper.read(ctx, TAB);
			String transactionData = helper.read(ctx, TAB);

			helper.read(ctx, LF); // get rid of line feed
			parent.setStatus(transactionStatus);
			parent.addData(transactionData);

			long transactionD = Long.parseLong(transactionDuration.substring(0, transactionDuration.length() - 2));

			parent.setDurationInMicros(transactionD);

			return stack.pop();
		case 'E':
			DefaultEvent event = new DefaultEvent(type, name);
			String eventStatus = helper.read(ctx, TAB);
			String eventData = helper.read(ctx, TAB);

			helper.read(ctx, LF); // get rid of line feed
			event.setTimestamp(m_dateHelper.parse(timestamp));
			event.setStatus(eventStatus);
			event.addData(eventData);

			tree.findOrCreateEvents().add(event);

			if (parent != null) {
				parent.addChild(event);
				return parent;
			} else {
				return event;
			}
		case 'M':
			DefaultMetric metric = new DefaultMetric(type, name);

			tree.findOrCreateMetrics().add(metric);

			String metricStatus = helper.read(ctx, TAB);
			String metricData = helper.read(ctx, TAB);

			helper.read(ctx, LF); // get rid of line feed
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
			String traceStatus = helper.read(ctx, TAB);
			String traceData = helper.read(ctx, TAB);

			helper.read(ctx, LF); // get rid of line feed
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

			tree.findOrCreateHeartbeats().add(heartbeat);

			String heartbeatStatus = helper.read(ctx, TAB);
			String heartbeatData = helper.read(ctx, TAB);

			helper.read(ctx, LF); // get rid of line feed
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
			throw new RuntimeException("Unknown identifier int name");
		}
	}

	protected void decodeMessage(Context ctx, MessageTree tree) {
		Stack<DefaultTransaction> stack = new Stack<DefaultTransaction>();
		Message parent = decodeLine(ctx, null, stack, tree);

		tree.setMessage(parent);

		while (ctx.getBuffer().readableBytes() > 0) {
			Message message = decodeLine(ctx, (DefaultTransaction) parent, stack, tree);

			if (message instanceof DefaultTransaction) {
				parent = message;
			} else {
				break;
			}
		}
	}

	@Override
	public ByteBuf encode(MessageTree tree) {
		ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.buffer(4 * 1024);
		int count = 0;
		int index = buf.writerIndex();

		buf.writeInt(0); // place-holder
		count += encodeHeader(tree, buf);

		if (tree.getMessage() != null) {
			count += encodeMessage(tree.getMessage(), buf);
		}

		buf.setInt(index, count);
		return buf;
	}

	protected int encodeHeader(MessageTree tree, ByteBuf buf) {
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

	protected int encodeLine(Message message, ByteBuf buf, char type, Policy policy) {
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

	public int encodeMessage(Message message, ByteBuf buf) {
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

					if (child != null) {
						count += encodeMessage(child, buf);
					}
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

	public void reset() {
		if (m_ctx != null) {
			m_ctx.remove();
		}
	}

	protected void setBufferWriter(BufferWriter writer) {
		m_writer = writer;
		m_bufferHelper = new BufferHelper(m_writer);
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

	protected static class BufferHelper {
		private BufferWriter m_writer;

		public BufferHelper(BufferWriter writer) {
			m_writer = writer;
		}

		public String read(Context ctx, byte separator) {
			ByteBuf buf = ctx.getBuffer();
			char[] data = ctx.getData();
			int from = buf.readerIndex();
			int to = buf.writerIndex();
			int index = 0;
			boolean flag = false;

			for (int i = from; i < to; i++) {
				byte b = buf.readByte();

				if (b == separator) {
					break;
				}

				if (index >= data.length) {
					char[] data2 = new char[to - from];

					System.arraycopy(data, 0, data2, 0, index);
					data = data2;
				}

				char c = (char) (b & 0xFF);

				if (c > 127) {
					flag = true;
				}

				if (c == '\\' && i + 1 < to) {
					byte b2 = buf.readByte();

					if (b2 == 't') {
						c = '\t';
						i++;
					} else if (b2 == 'r') {
						c = '\r';
						i++;
					} else if (b2 == 'n') {
						c = '\n';
						i++;
					} else if (b2 == '\\') {
						c = '\\';
						i++;
					} else {
						// move back
						buf.readerIndex(i + 1);
					}
				}

				data[index] = c;
				index++;
			}

			if (!flag) {
				return new String(data, 0, index);
			} else {
				byte[] ba = new byte[index];

				for (int i = 0; i < index; i++) {
					ba[i] = (byte) (data[i] & 0xFF);
				}

				try {
					return new String(ba, 0, index, "utf-8");
				} catch (UnsupportedEncodingException e) {
					return new String(ba, 0, index);
				}
			}
		}

		public int write(ByteBuf buf, byte b) {
			buf.writeByte(b);
			return 1;
		}

		public int write(ByteBuf buf, String str) {
			if (str == null) {
				str = "null";
			}

			byte[] data = str.getBytes();

			buf.writeBytes(data);
			return data.length;
		}

		public int writeRaw(ByteBuf buf, String str) {
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

	public static class Context {
		private ByteBuf m_buffer;

		private char[] m_data;

		public Context() {
			m_data = new char[1024 * 1024];
		}

		public ByteBuf getBuffer() {
			return m_buffer;
		}

		public Context setBuffer(ByteBuf buffer) {
			m_buffer = buffer;
			return this;
		}

		public char[] getData() {
			return m_data;
		}

		public void removeBuf() {
			m_buffer = null;
		}
	}

	/**
		* Thread safe date helper class. DateFormat is NOT thread safe.
		*/
	protected static class DateHelper {
		private BlockingQueue<SimpleDateFormat> m_formats = new ArrayBlockingQueue<SimpleDateFormat>(20);

		private Map<String, Long> m_map = new ConcurrentHashMap<String, Long>();

		public String format(long timestamp) {
			SimpleDateFormat format = m_formats.poll();

			if (format == null) {
				format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			}

			try {
				return format.format(new Date(timestamp));
			} finally {
				if (m_formats.remainingCapacity() > 0) {
					m_formats.offer(format);
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

}