package com.dianping.cat.message.spi.codec;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jboss.netty.buffer.ChannelBuffer;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MockMessageBuilder;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import org.unidal.lookup.annotation.Inject;

/**
 * Local use only, do not use it over network since it only supports one-way encoding
 */
public class WaterfallMessageCodec implements MessageCodec, Initializable {
	public static final String ID = "waterfall";

	private static final String VERSION = "WF1"; // Waterfall version 1

	@Inject
	private BufferWriter m_writer;

	private BufferHelper m_bufferHelper;

	private boolean m_mockMode = false;

	protected int countTransactions(Transaction t) {
		int count = 1;

		for (Message child : t.getChildren()) {
			if (child instanceof Transaction) {
				count += countTransactions((Transaction) child);
			}
		}

		return count;
	}

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
		Message message = tree.getMessage();

		if (m_mockMode) {
			message = mockTransaction();
			tree.setMessage(message);
		}

		if (message instanceof Transaction) {
			int count = 0;
			int index = buf.writerIndex();
			BufferHelper helper = m_bufferHelper;
			Transaction t = (Transaction) message;
			Locator locator = new Locator();
			Ruler ruler = new Ruler((int) t.getDurationInMicros());

			ruler.setWidth(1400);
			ruler.setHeight(18 * countTransactions(t) + 10);
			ruler.setOffsetX(200);
			ruler.setOffsetY(10);

			buf.writeInt(0); // place-holder

			count += helper.table1(buf);
			count += helper.crlf(buf);
			count += encodeHeader(tree, buf, ruler);

			count += encodeRuler(buf, locator, ruler);
			count += encodeTransaction(tree, t, buf, locator, ruler);

			count += encodeFooter(tree, buf);
			count += helper.table2(buf);
			buf.setInt(index, count);
		}
	}

	protected int encodeFooter(MessageTree tree, ChannelBuffer buf) {
		BufferHelper helper = m_bufferHelper;
		XmlBuilder b = new XmlBuilder();
		StringBuilder sb = b.getResult();

		b.tag2("g");
		b.tag2("svg");
		sb.append("</td></tr>");

		int count = helper.write(buf, sb.toString());
		return count;
	}

	protected int encodeHeader(MessageTree tree, ChannelBuffer buf, Ruler ruler) {
		BufferHelper helper = m_bufferHelper;
		XmlBuilder b = new XmlBuilder();
		StringBuilder sb = b.getResult();

		sb.append("<tr class=\"header\"><td>");
		sb.append(VERSION).append(" ").append(tree.getDomain()).append(" ");
		sb.append(tree.getHostName()).append(" ").append(tree.getIpAddress()).append(" ");
		sb.append(tree.getThreadGroupName()).append(" ").append(tree.getThreadId()).append(" ");
		sb.append(tree.getThreadName()).append(" ").append(tree.getMessageId()).append(" ");
		sb.append(tree.getParentMessageId()).append(" ").append(tree.getRootMessageId()).append(" ");
		sb.append(tree.getSessionToken()).append(" ");
		sb.append("</td></tr>");
		sb.append("<tr><td>");

		int height = ruler.getHeight();
		int width = ruler.getWidth();

		b.add("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\r\n");
		b.tag1("svg", "x", 0, "y", 0, "width", width, "height", height, "viewBox", "0,0," + width + "," + height, "xmlns",
		      "http://www.w3.org/2000/svg");
		b.tag1("g", "font-size", "12", "stroke", "gray");

		int count = helper.write(buf, sb.toString());
		return count;
	}

	protected int encodeLine(MessageTree tree, Transaction t, ChannelBuffer buf, Locator locator, Ruler ruler) {
		BufferHelper helper = m_bufferHelper;
		XmlBuilder b = new XmlBuilder();
		StringBuilder sb = b.getResult();
		int width = 6;
		int height = 18;
		int x = 0;
		int y = locator.getLine() * height + ruler.getOffsetY();

		b.branch(locator, x, y, width, height);

		x += locator.getLevel() * width;

		if (t.getStatus().equals("0")) {
			b.tagWithText("text", t.getType(), "x", x, "y", y - 5, "font-weight", "bold", "stroke-width", "0");
		} else {
			b.tagWithText("text", t.getType(), "x", x, "y", y - 5, "font-weight", "bold", "stroke-width", "0", "fill", "red");
		}

		long t0 = tree.getMessage().getTimestamp();
		long t1 = t.getTimestamp();
		long d = t.getDurationInMicros();

		int rx = ruler.calcX((t1 - t0) * 1000);
		int rw = ruler.calcX(d) - ruler.getOffsetX();

		b.tag("rect", "x", rx + 1, "y", y - 15, "width", rw, "height", height - 2, "fill", "#0066ff", "opacity", "0.5");
		b.tagWithText("text", String.format("%.2f %s", t.getDurationInMicros() / 1000.0, t.getName()), "x", rx + 5, "y", y - 3,
		      "font-size", "11", "stroke-width", "0");

		int count = helper.write(buf, sb.toString());
		return count;
	}

	protected int encodeRuler(ChannelBuffer buf, Locator locator, Ruler ruler) {
		BufferHelper helper = m_bufferHelper;
		XmlBuilder b = new XmlBuilder();
		StringBuilder sb = b.getResult();
		PathBuilder p = new PathBuilder();
		int height = ruler.getHeight();

		b.tag1("g", "id", "ruler", "font-size", "12", "text-anchor", "middle", "stroke", "black", "stroke-width", "1");

		int unitNum = ruler.getUnitNum();
		int unitStep = ruler.getUnitStep();
		int unit = (int) ruler.getUnit();
		int x = ruler.getOffsetX();
		int y = 10;

		for (int i = 0; i <= unitNum; i++) {
			String text;

			if (unitStep >= 1000) {
				text = (i * unitStep / 1000) + "ms";
			} else {
				text = (i * unitStep) + "us";
			}

			b.tagWithText("text", text, "x", x + i * unit, "y", y, "stroke-width", "0");
		}

		for (int i = 0; i <= unitNum; i++) {
			b.tag("path", "d", p.moveTo(x + i * unit, y + 6).v(height).build(), "stroke-dasharray", "3,4");
		}

		b.tag2("g");

		int count = helper.write(buf, sb.toString());
		return count;
	}

	protected int encodeTransaction(MessageTree tree, Transaction transaction, ChannelBuffer buf, Locator locator, Ruler ruler) {
		List<Transaction> children = getChildTransactions(transaction);
		int count = 0;

		locator.downLevel(children.isEmpty());
		locator.nextLine();
		count += encodeLine(tree, transaction, buf, locator, ruler);

		int len = children.size();

		for (int i = 0; i < len; i++) {
			Transaction child = children.get(i);

			locator.setLast(i == len - 1);
			count += encodeTransaction(tree, child, buf, locator, ruler);
		}

		locator.upLevel();
		return count;
	}

	protected List<Transaction> getChildTransactions(Transaction transaction) {
		List<Transaction> children = new ArrayList<Transaction>();

		for (Message child : transaction.getChildren()) {
			if (child instanceof Transaction) {
				children.add((Transaction) child);
			}
		}

		return children;
	}

	@Override
	public void initialize() throws InitializationException {
		m_bufferHelper = new BufferHelper(m_writer);
	}

	protected Transaction mockTransaction() { // for test/debug purpose
		return (Transaction) new MockMessageBuilder() {
			@Override
			public MessageHolder define() {
				TransactionHolder t = t("WEB CLUSTER", "GET", 112819) //
				      .at(1348374838231L) //
				      .after(1300).child(t("QUICKIE SERVICE", "gimme_stuff", 1571)) //
				      .after(100).child(e("SERVICE", "event1")) //
				      .after(100).child(h("SERVICE", "heartbeat1")) //
				      .after(100).child(t("WEB SERVER", "GET", 109358).status("1") //
				            .after(1000).child(t("SOME SERVICE", "get", 4345) //
				                  .after(4000).child(t("MEMCACHED", "Get", 279))) //
				            .mark().after(200).child(t("MEMCACHED", "Inc", 319)) //
				            .reset().after(500).child(t("BIG ASS SERVICE", "getThemDatar", 97155) //
				                  .after(1000).mark().child(t("SERVICE", "getStuff", 3760)) //
				                  .reset().child(t("DATAR", "findThings", 94537)) //
				                  .after(200).child(t("THINGIE", "getMoar", 1435)) //
				            ) //
				            .after(100).mark().child(t("OTHER DATA SERVICE", "get", 4394) //
				                  .after(800).mark().child(t("MEMCACHED", "Get", 378)) //
				                  .reset().child(t("MEMCACHED", "Get", 3496)) //
				            ) //
				            .reset().child(t("FINAL DATA SERVICE", "get", 1902) //
				                  .after(1000).mark().child(t("MEMCACHED", "Get", 386)) //
				                  .reset().child(t("MEMCACHED", "Get", 322)) //
				                  .reset().child(t("MEMCACHED", "Get", 322)) //
				            ) //
				      ) //
				;

				return t;
			}
		}.build();
	}

	public void setBufferWriter(BufferWriter writer) {
		m_writer = writer;
		m_bufferHelper = new BufferHelper(m_writer);
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

	protected static class Locator {
		private int m_level;

		private int m_line;

		private Stack<Boolean> m_last = new Stack<Boolean>();

		private Stack<Integer> m_flags = new Stack<Integer>();

		public void downLevel(boolean atomic) {
			if (m_level > 0) {
				boolean last = m_last.peek();

				m_flags.pop();

				if (last) {
					m_flags.push(6); // 00110
				} else {
					m_flags.push(22); // 10110
				}

				for (int i = 0; i < m_level - 1; i++) {
					Integer flag = m_flags.get(i);
					int f = flag;

					if (flag == 6) { // 00110
						f = 0; // 00000
					} else if (flag == 22) { // 10110
						f = 20; // 10100
					}

					m_flags.set(i, f);
				}
			}

			boolean root = m_level == 0;

			if (atomic) {
				if (root) {
					m_flags.push(1); // 00001
				} else {
					m_flags.push(9); // 01001
				}
			} else {
				if (root) {
					m_flags.push(17); // 10001
				} else {
					m_flags.push(25); // 11001
				}
			}

			m_last.push(root ? true : false);
			m_level++;
		}

		public Stack<Integer> getFlags() {
			return m_flags;
		}

		public boolean getLast(int level) {
			return m_last.get(level);
		}

		public int getLevel() {
			return m_level;
		}

		public int getLine() {
			return m_line;
		}

		public boolean isFirst() {
			return m_level == 1;
		}

		public boolean isLast() {
			return m_last.peek();
		}

		public void nextLine() {
			m_line++;
		}

		public void setLast(boolean last) {
			m_last.pop();
			m_last.push(last);
		}

		@Override
		public String toString() {
			return String.format("Locator[level=%s, line=%s, first=%s, last=%s]", m_level, m_line, isFirst(), isLast());
		}

		public void upLevel() {
			m_level--;
			m_last.pop();
			m_flags.pop();
		}
	}

	protected static class PathBuilder {
		private int m_marker;

		private StringBuilder m_sb = new StringBuilder(64);

		public String build() {
			String result = m_sb.toString();

			m_sb.setLength(0);
			return result;
		}

		public PathBuilder h(int deltaX) {
			m_sb.append(" h").append(deltaX);
			return this;
		}

		public PathBuilder m(int deltaX, int deltaY) {
			m_sb.append(" m").append(deltaX).append(',').append(deltaY);
			return this;
		}

		public PathBuilder mark() {
			m_marker = m_sb.length();
			return this;
		}

		public PathBuilder moveTo(int x, int y) {
			m_sb.append('M').append(x).append(',').append(y);
			return this;
		}

		public PathBuilder repeat(int count) {
			int pos = m_sb.length();

			for (int i = 0; i < count; i++) {
				m_sb.append(m_sb.subSequence(m_marker, pos));
			}

			return this;
		}

		public PathBuilder v(int deltaY) {
			m_sb.append(" v").append(deltaY);
			return this;
		}
	}

	protected static class Ruler {
		private static final int[] UNITS = { 1, 2, 3, 5 };

		private int m_maxValue;

		private int m_unitNum;

		private int m_unitStep;

		public int m_width;

		private int m_height;

		private int m_offsetX;

		private int m_offsetY;

		public Ruler(int maxValue) {
			m_maxValue = maxValue;

			int e = 1;
			int value = maxValue;

			while (true) {
				if (value > 50) {
					value = (value + 9) / 10;
					e *= 10;
				} else {
					if (value < 6) {
						m_unitNum = value;
						m_unitStep = e;
					} else {
						for (int unit : UNITS) {
							int num = (value + unit - 1) / unit;

							if (num >= 6 && num <= 10) {
								m_unitNum = num;
								m_unitStep = unit * e;
								break;
							}
						}
					}

					break;
				}
			}
		}

		public int calcX(long value) {
			int w = (int) (value * getUnit() / m_unitStep);

			if (w == 0 && value > 0) {
				w = 1;
			}

			return w + m_offsetX;
		}

		public int getHeight() {
			return m_height;
		}

		public int getMaxValue() {
			return m_maxValue;
		}

		public int getOffsetX() {
			return m_offsetX;
		}

		public int getOffsetY() {
			return m_offsetY;
		}

		public double getUnit() {
			return (m_width - m_offsetX - 20) * 1.0 / m_unitNum;
		}

		public int getUnitNum() {
			return m_unitNum;
		}

		public int getUnitStep() {
			return m_unitStep;
		}

		public int getWidth() {
			return m_width;
		}

		public void setHeight(int height) {
			m_height = height;
		}

		public void setOffsetX(int offsetX) {
			m_offsetX = offsetX;
		}

		public void setOffsetY(int offsetY) {
			m_offsetY = offsetY;
		}

		public void setWidth(int width) {
			m_width = width;
		}

		@Override
		public String toString() {
			return String.format("[%s, %s, %s]", m_maxValue, m_unitNum, m_unitStep);
		}
	}

	protected static class XmlBuilder {
		private boolean m_compact;

		private int m_level;

		private StringBuilder m_sb = new StringBuilder(8192);

		public XmlBuilder add(String text) {
			m_sb.append(text);
			return this;
		}

		public void branch(Locator locator, int x, int y, int width, int height) {
			PathBuilder p = new PathBuilder();
			int w = width / 2;
			int h = height / 2;
			int r = 2;

			for (Integer flag : locator.getFlags()) {
				int cx = x + w;
				int cy = y - h;

				if ((flag & 2) != 0) { // 00010
					tag("path", "d", p.moveTo(cx, cy).h(w).build());
				}

				if ((flag & 4) != 0) { // 00100
					tag("path", "d", p.moveTo(cx, cy).v(-h).build());
				}

				if ((flag & 8) != 0) { // 01000
					tag("path", "d", p.moveTo(cx, cy).h(-w).build());
				}

				if ((flag & 16) != 0) { // 10000
					tag("path", "d", p.moveTo(cx, cy).v(h).build());
				}

				if ((flag & 1) != 0) { // 00001
					m_sb.append("<circle cx=\"").append(cx).append("\" cy=\"").append(cy).append("\" r=\"").append(r)
					      .append("\" stroke=\"red\" fill=\"white\"/>");
				}

				x += width;
			}
		}

		public XmlBuilder element(String name, String value) {
			indent();
			m_sb.append('<').append(name).append('>');
			m_sb.append(value);
			m_sb.append("</").append(name).append(">");
			newLine();
			return this;
		}

		public StringBuilder getResult() {
			return m_sb;
		}

		public XmlBuilder indent() {
			if (!m_compact) {
				for (int i = m_level - 1; i >= 0; i--) {
					m_sb.append("  ");
				}
			}

			return this;
		}

		public XmlBuilder newLine() {
			m_sb.append("\r\n");
			return this;
		}

		public XmlBuilder tag(String name, Object... attributes) {
			return tagWithText(name, null, attributes);
		}

		public XmlBuilder tag1(String name, Object... attributes) {
			indent();

			m_sb.append('<').append(name);

			int len = attributes.length;
			for (int i = 0; i < len; i += 2) {
				Object key = attributes[i];
				Object val = attributes[i + 1];

				if (val != null) {
					m_sb.append(' ').append(key).append("=\"").append(val).append('"');
				}
			}

			m_sb.append(">");
			newLine();
			m_level++;
			return this;
		}

		public XmlBuilder tag2(String name) {
			m_level--;
			indent();
			m_sb.append("</").append(name).append(">");
			newLine();
			return this;
		}

		public XmlBuilder tagWithText(String name, Object text, Object... attributes) {
			indent();

			m_sb.append('<').append(name);

			int len = attributes.length;
			for (int i = 0; i < len; i += 2) {
				Object key = attributes[i];
				Object val = attributes[i + 1];

				if (val != null) {
					m_sb.append(' ').append(key).append("=\"").append(val).append('"');
				}
			}

			if (text == null) {
				m_sb.append("/>");
			} else {
				m_sb.append('>').append(text).append("</").append(name).append('>');
			}

			newLine();
			return this;
		}
	}
}
