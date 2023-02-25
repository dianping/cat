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
package com.dianping.cat.message.codec;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import io.netty.buffer.ByteBuf;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.BufferWriter;

public class HtmlMessageCodec {

	public static final String ID = "html";

	private static final String VERSION = "HT2"; // HTML version 2 since Mar 20, 2013

	private BufferWriter m_writer = new HtmlEncodingBufferWriter();

	private String m_logViewPrefix = "/cat/r/m/";

	private BufferHelper m_bufferHelper = new BufferHelper(m_writer);

	private DateHelper m_dateHelper = new DateHelper();

	public void encode(MessageTree tree, ByteBuf buf) {
		int count = 0;
		int index = buf.writerIndex();
		BufferHelper helper = m_bufferHelper;

		buf.writeInt(0); // place-holder

		count += helper.table1(buf);
		count += helper.crlf(buf);
		count += encodeHeader(tree, buf);

		if (tree.getMessage() != null) {
			count += encodeMessage(tree, tree.getMessage(), buf, 0, new LineCounter());
		}

		count += helper.table2(buf);
		buf.setInt(index, count);
	}

	protected int encodeFooter(MessageTree tree, ByteBuf buf) {
		return 0;
	}

	protected int encodeHeader(MessageTree tree, ByteBuf buf) {
		BufferHelper helper = m_bufferHelper;
		StringBuilder sb = new StringBuilder(1024);
		String parentMessageId = tree.getParentMessageId();
		String rootMessageId = tree.getRootMessageId();
		String domain = tree.getDomain();

		sb.append("<tr class=\"header\"><td colspan=5>");
		if (rootMessageId != null && !rootMessageId.equalsIgnoreCase("null")) {
			sb.append(String.format("<a href='%s?domain=%s'>RootLogview</a>&nbsp;&nbsp;", rootMessageId, domain));

			if (!parentMessageId.equals(rootMessageId)) {
				sb.append(String.format("<a href='%s?domain=%s'>ParentLogview</a> ", parentMessageId, domain));
			}
		}
		sb.append(VERSION).append(" ").append(tree.getDomain()).append(" ");
		sb.append(tree.getHostName()).append(" ").append(tree.getIpAddress()).append(" ");
		sb.append(tree.getThreadGroupName()).append(" ").append(tree.getThreadId()).append(" ");
		sb.append(tree.getThreadName()).append(" ").append(tree.getMessageId()).append(" ");
		sb.append(parentMessageId).append(" ").append(rootMessageId).append(" ");
		sb.append(tree.getSessionToken()).append(" ");
		sb.append("</td></tr>");

		return helper.write(buf, sb.toString());
	}

	protected int encodeLine(MessageTree tree, Message message, ByteBuf buf, char type, Policy policy, int level,
							LineCounter counter) {
		BufferHelper helper = m_bufferHelper;
		int count = 0;

		if (counter != null) {
			counter.inc();

			count += helper.tr1(buf, counter.getCount() % 2 != 0 ? "odd" : "even");
		} else {
			count += helper.tr1(buf, null);
		}

		count += helper.td1(buf);

		count += helper.nbsp(buf, level * 2); // 2 spaces per level
		count += helper.write(buf, (byte) type);

		if (type == 'T' && message instanceof Transaction) {
			long duration = ((Transaction) message).getDurationInMillis();

			count += helper.write(buf, m_dateHelper.format(message.getTimestamp() + duration));
		} else {
			count += helper.write(buf, m_dateHelper.format(message.getTimestamp()));
		}

		count += helper.td2(buf);

		count += helper.td(buf, message.getType());
		count += helper.td(buf, message.getName());

		if (policy != Policy.WITHOUT_STATUS) {
			if (Message.SUCCESS.equals(message.getStatus())) {
				count += helper.td(buf, "&nbsp;"); // do not output "0"
			} else {
				count += helper.td(buf, message.getStatus(), "class=\"error\"");
			}

			Object data = message.getData();

			count += helper.td1(buf);

			if (policy == Policy.WITH_DURATION && message instanceof Transaction) {
				long durationInMicro = ((Transaction) message).getDurationInMicros();
				long durationInMillis = durationInMicro / 1000L;

				if (durationInMicro < 100L) {
					count += helper.write(buf, String.format("%.2f", durationInMicro / 1000.0));
				} else if (durationInMicro < 10000L) { // less than 10 ms
					count += helper.write(buf, String.format("%.2f", durationInMicro / 1000.0));
				} else { // no fraction
					count += helper.write(buf, Long.toString(durationInMillis));
				}

				count += helper.write(buf, "ms ");
			}

			count += helper.writeRaw(buf, String.valueOf(data));
			count += helper.td2(buf);
		} else {
			count += helper.td(buf, "");
			count += helper.td(buf, "");
		}

		count += helper.tr2(buf);
		count += helper.crlf(buf);

		return count;
	}

	protected int encodeLogViewLink(MessageTree tree, Message message, ByteBuf buf, int level, LineCounter counter) {
		BufferHelper helper = m_bufferHelper;
		Map<String, String> links = parseLinks(message.getData().toString());
		int count = 0;

		for (Map.Entry<String, String> e : links.entrySet()) {
			String link = e.getKey();
			String title = e.getValue();

			if (title.length() == 0) {
				title = "show";
			}
			if (counter != null) {
				counter.inc();

				count += helper.tr1(buf, "link");
			} else {
				count += helper.tr1(buf, null);
			}

			count += helper.td1(buf);
			count += helper.nbsp(buf, level * 2); // 2 spaces per level
			count += helper.write(buf, String.format("<a href=\"%s%s\" onclick=\"return show(this,'%s');\">[:: %s ::]</a>", //
									m_logViewPrefix, link, link, title));
			count += helper.td2(buf);
			count += helper.td(buf, "<div id=\"" + link + "\"></div>", "colspan=\"4\"");

			count += helper.tr2(buf);
			count += helper.crlf(buf);
		}

		return count;
	}

	protected int encodeMessage(MessageTree tree, Message message, ByteBuf buf, int level, LineCounter counter) {
		if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;
			List<Message> children = transaction.getChildren();

			if (children.isEmpty()) {
				if (transaction.getDurationInMillis() < 0) {
					return encodeLine(tree, transaction, buf, 't', Policy.WITHOUT_STATUS, level, counter);
				} else {
					return encodeLine(tree, transaction, buf, 'A', Policy.WITH_DURATION, level, counter);
				}
			} else {
				int count = 0;

				count += encodeLine(tree, transaction, buf, 't', Policy.WITHOUT_STATUS, level, counter);

				for (Message child : children) {
					count += encodeMessage(tree, child, buf, level + 1, counter);
				}

				count += encodeLine(tree, transaction, buf, 'T', Policy.WITH_DURATION, level, counter);

				return count;
			}
		} else if (message instanceof Event) {
			String type = message.getType();

			if ("RemoteCall".equals(type)) {
				return encodeLogViewLink(tree, message, buf, level, counter);
			} else if ("RemoteLink".equals(type)) {
				return encodeRemoteLink(tree, message, buf, level, counter);
			} else {
				return encodeLine(tree, message, buf, 'E', Policy.DEFAULT, level, counter);
			}
		} else if (message instanceof Trace) {
			return encodeLine(tree, message, buf, 'L', Policy.DEFAULT, level, counter);
		} else if (message instanceof Metric) {
			return encodeLine(tree, message, buf, 'M', Policy.DEFAULT, level, counter);
		} else if (message instanceof Heartbeat) {
			return encodeLine(tree, message, buf, 'H', Policy.DEFAULT, level, counter);
		} else {
			throw new RuntimeException(String.format("Unsupported message type: %s.", message.getClass()));
		}
	}

	protected int encodeRemoteLink(MessageTree tree, Message message, ByteBuf buf, int level, LineCounter counter) {
		BufferHelper helper = m_bufferHelper;
		int count = 0;

		if (counter != null) {
			counter.inc();

			count += helper.tr1(buf, "link");
		} else {
			count += helper.tr1(buf, null);
		}

		String link = message.getData().toString();
		String name = message.getName();

		count += helper.td1(buf);
		count += helper.nbsp(buf, level * 2); // 2 spaces per level

		count += helper.write(buf, String.format("<a href=\"%s%s\" onclick=\"return show(this,'%s');\">[:: %s ::]</a>", //
								m_logViewPrefix, link, link, name));
		count += helper.td2(buf);
		count += helper.td(buf, "<div id=\"" + link + "\"></div>", "colspan=\"4\"");
		count += helper.tr2(buf);
		count += helper.crlf(buf);

		return count;
	}

	protected Map<String, String> parseLinks(String str) {
		Map<String, String> links = new LinkedHashMap<String, String>();
		int len = str.length();
		StringBuilder name = new StringBuilder();
		StringBuilder value = new StringBuilder();
		boolean inName = true;

		for (int i = 0; i < len; i++) {
			char ch = str.charAt(i);

			switch (ch) {
			case '&':
				links.put(name.toString(), value.toString());
				name.setLength(0);
				value.setLength(0);
				inName = true;

				break;
			case '=':
				if (inName) {
					inName = false;
					break;
				}

				// fall through
			default:
				if (inName) {
					name.append(ch);
				} else {
					value.append(ch);
				}

				break;
			}
		}

		if (name.length() > 0) {
			links.put(name.toString(), value.toString());
		}

		return links;
	}

	public void setBufferWriter(BufferWriter writer) {
		m_writer = writer;
		m_bufferHelper = new BufferHelper(m_writer);
	}

	public void setLogViewPrefix(String logViewPrefix) {
		m_logViewPrefix = logViewPrefix;
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
		private static byte[] TABLE1 = "<table class=\"logview\">".getBytes();

		private static byte[] TABLE2 = "</table>".getBytes();

		private static byte[] TR1 = "<tr>".getBytes();

		private static byte[] TR2 = "</tr>".getBytes();

		private static byte[] TD1 = "<td>".getBytes();

		private static byte[] TD2 = "</td>".getBytes();

		private static byte[] NBSP = "&nbsp;".getBytes();

		private static byte[] CRLF = "\r\n".getBytes();

		private BufferWriter m_writer;

		public BufferHelper(BufferWriter writer) {
			m_writer = writer;
		}

		public int crlf(ByteBuf buf) {
			buf.writeBytes(CRLF);
			return CRLF.length;
		}

		public int nbsp(ByteBuf buf, int count) {
			for (int i = 0; i < count; i++) {
				buf.writeBytes(NBSP);
			}

			return count * NBSP.length;
		}

		public int table1(ByteBuf buf) {
			buf.writeBytes(TABLE1);
			return TABLE1.length;
		}

		public int table2(ByteBuf buf) {
			buf.writeBytes(TABLE2);
			return TABLE2.length;
		}

		public int td(ByteBuf buf, String str) {
			return td(buf, str, null);
		}

		public int td(ByteBuf buf, String str, String attributes) {
			if (str == null) {
				str = "null";
			}

			byte[] data = str.getBytes(StandardCharsets.UTF_8);
			int count = 0;

			if (attributes == null) {
				buf.writeBytes(TD1);
				count += TD1.length;
			} else {
				String tag = "<td " + attributes + ">";
				byte[] bytes = tag.getBytes(StandardCharsets.UTF_8);

				buf.writeBytes(bytes);
				count += bytes.length;
			}

			buf.writeBytes(data);
			count += data.length;

			buf.writeBytes(TD2);
			count += TD2.length;

			return count;
		}

		public int td1(ByteBuf buf) {
			buf.writeBytes(TD1);
			return TD1.length;
		}

		public int td1(ByteBuf buf, String attributes) {
			if (attributes == null) {
				buf.writeBytes(TD1);
				return TD1.length;
			} else {
				String tag = "<td " + attributes + ">";
				byte[] bytes = tag.getBytes(StandardCharsets.UTF_8);

				buf.writeBytes(bytes);
				return bytes.length;
			}
		}

		public int td2(ByteBuf buf) {
			buf.writeBytes(TD2);
			return TD2.length;
		}

		public int tr1(ByteBuf buf, String styleClass) {
			if (styleClass == null) {
				buf.writeBytes(TR1);
				return TR1.length;
			} else {
				String tag = "<tr class=\"" + styleClass + "\">";
				byte[] bytes = tag.getBytes(StandardCharsets.UTF_8);

				buf.writeBytes(bytes);
				return bytes.length;
			}
		}

		public int tr2(ByteBuf buf) {
			buf.writeBytes(TR2);
			return TR2.length;
		}

		public int write(ByteBuf buf, byte b) {
			buf.writeByte(b);
			return 1;
		}

		public int write(ByteBuf buf, String str) {
			if (str == null) {
				str = "null";
			}

			byte[] data = str.getBytes(StandardCharsets.UTF_8);

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

	/**
		* Thread safe date helper class. DateFormat is NOT thread safe.
		*/
	protected static class DateHelper {
		private static final String DATE_PATTERN = "HH:mm:ss.SSS";

		private BlockingQueue<SimpleDateFormat> m_queue = new ArrayBlockingQueue<SimpleDateFormat>(20);

		public String format(long timestamp) {
			SimpleDateFormat format = m_queue.poll();

			if (format == null) {
				format = new SimpleDateFormat(DATE_PATTERN);
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
			SimpleDateFormat format = m_queue.poll();

			if (format == null) {
				format = new SimpleDateFormat(DATE_PATTERN);
				format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
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

	protected static class LineCounter {
		private int m_count;

		public int getCount() {
			return m_count;
		}

		public void inc() {
			m_count++;
		}
	}
}
