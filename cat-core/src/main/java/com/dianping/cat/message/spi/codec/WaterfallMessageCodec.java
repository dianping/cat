package com.dianping.cat.message.spi.codec;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jboss.netty.buffer.ChannelBuffer;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

/**
 * Local use only, do not use it over network since it only supports one-way encoding
 */
public class WaterfallMessageCodec implements MessageCodec, Initializable {
	public static final String ID = "waterfall";

	private static final String VERSION = "WF1"; // Waterfall version 1

	@Inject
	private BufferWriter m_writer;

	@Inject
	private boolean m_showNav = true;

	private BufferHelper m_bufferHelper;

	@Override
	public MessageTree decode(ChannelBuffer buf) {
		throw new UnsupportedOperationException("HtmlMessageCodec only supports one-way encoding!");
	}

	@Override
	public void decode(ChannelBuffer buf, MessageTree tree) {
		throw new UnsupportedOperationException("HtmlMessageCodec only supports one-way encoding!");
	}

	@Override
	public void encode(MessageTree tree, ChannelBuffer buf) {
		int count = 0;
		int index = buf.writerIndex();
		BufferHelper helper = m_bufferHelper;

		buf.writeInt(0); // place-holder

		count += helper.table1(buf);
		count += helper.crlf(buf);
		if (m_showNav) {
			count += encodeFooter(tree, buf);
		}
		count += encodeHeader(tree, buf);

		if (tree.getMessage() != null) {
			count += encodeMessage(tree, tree.getMessage(), buf, 0);
		}

		count += helper.table2(buf);
		buf.setInt(index, count);
	}

	protected int encodeFooter(MessageTree tree, ChannelBuffer buf) {
		BufferHelper helper = m_bufferHelper;
		int count = 0;
		String uri = "/cat/r/m/" + tree.getMessageId();

		count += helper.tr1(buf, "nav");
		count += helper.td1(buf, "colspan=\"4\" align=\"left\"");
		count += helper.nbsp(buf, 3);
		count += helper.write(buf, "<a href=\"");
		count += helper.write(buf, uri);
		count += helper.write(buf, "?tag1=t:");
		count += helper.write(buf, tree.getThreadId());
		count += helper.write(buf, "\">&lt;&lt;&lt; Thread &nbsp;&nbsp;</a>");
		count += helper.write(buf, "<a href=\"");
		count += helper.write(buf, uri);
		count += helper.write(buf, "?tag2=t:");
		count += helper.write(buf, tree.getThreadId());
		count += helper.write(buf, "\"> &nbsp;&nbsp;Thread &gt;&gt;&gt;</a>");
		count += helper.nbsp(buf, 3);
		count += helper.td2(buf);
		count += helper.tr2(buf);
		count += helper.crlf(buf);

		return count;
	}

	protected int encodeHeader(MessageTree tree, ChannelBuffer buf) {
		BufferHelper helper = m_bufferHelper;
		StringBuilder sb = new StringBuilder();

		sb.append("<tr class=\"header\"><td colspan=5>");
		sb.append(VERSION).append(" ").append(tree.getDomain()).append(" ");
		sb.append(tree.getHostName()).append(" ").append(tree.getIpAddress()).append(" ");
		sb.append(tree.getThreadGroupName()).append(" ").append(tree.getThreadId()).append(" ");
		sb.append(tree.getThreadName()).append(" ").append(tree.getMessageId()).append(" ");
		sb.append(tree.getParentMessageId()).append(" ").append(tree.getRootMessageId()).append(" ");
		sb.append(tree.getSessionToken()).append(" ");
		sb.append("</td></tr>");

		int count = helper.write(buf, sb.toString());
		return count;
	}

	protected int encodeLine(MessageTree tree, Message message, ChannelBuffer buf, int level) {
		return 0;
	}

	protected int encodeMessage(MessageTree tree, Message message, ChannelBuffer buf, int level) {
		if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;
			List<Message> children = transaction.getChildren();
			int count = 0;

			count += encodeLine(tree, transaction, buf, level);

			for (Message child : children) {
				count += encodeMessage(tree, child, buf, level + 1);
			}

			return count;
		} else {
			return 0;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_bufferHelper = new BufferHelper(m_writer);
	}

	public void setBufferWriter(BufferWriter writer) {
		m_writer = writer;
		m_bufferHelper = new BufferHelper(m_writer);
	}

	public void setShowNav(boolean showNav) {
		m_showNav = showNav;
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

		public int crlf(ChannelBuffer buf) {
			buf.writeBytes(CRLF);
			return CRLF.length;
		}

		public int nbsp(ChannelBuffer buf, int count) {
			for (int i = 0; i < count; i++) {
				buf.writeBytes(NBSP);
			}

			return count * NBSP.length;
		}

		public int table1(ChannelBuffer buf) {
			buf.writeBytes(TABLE1);
			return TABLE1.length;
		}

		public int table2(ChannelBuffer buf) {
			buf.writeBytes(TABLE2);
			return TABLE2.length;
		}

		public int td(ChannelBuffer buf, String str) {
			return td(buf, str, null);
		}

		public int td(ChannelBuffer buf, String str, String attributes) {
			if (str == null) {
				str = "null";
			}

			byte[] data = str.getBytes();
			int count = 0;

			if (attributes == null) {
				buf.writeBytes(TD1);
				count += TD1.length;
			} else {
				String tag = "<td " + attributes + ">";
				byte[] bytes = tag.getBytes();

				buf.writeBytes(bytes);
				count += bytes.length;
			}

			buf.writeBytes(data);
			count += data.length;

			buf.writeBytes(TD2);
			count += TD2.length;

			return count;
		}

		public int td1(ChannelBuffer buf) {
			buf.writeBytes(TD1);
			return TD1.length;
		}

		public int td1(ChannelBuffer buf, String attributes) {
			if (attributes == null) {
				buf.writeBytes(TD1);
				return TD1.length;
			} else {
				String tag = "<td " + attributes + ">";
				byte[] bytes = tag.getBytes();

				buf.writeBytes(bytes);
				return bytes.length;
			}
		}

		public int td2(ChannelBuffer buf) {
			buf.writeBytes(TD2);
			return TD2.length;
		}

		public int tr1(ChannelBuffer buf, String styleClass) {
			if (styleClass == null) {
				buf.writeBytes(TR1);
				return TR1.length;
			} else {
				String tag = "<tr class=\"" + styleClass + "\">";
				byte[] bytes = tag.getBytes();

				buf.writeBytes(bytes);
				return bytes.length;
			}
		}

		public int tr2(ChannelBuffer buf) {
			buf.writeBytes(TR2);
			return TR2.length;
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
}
