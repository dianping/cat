package com.dianping.cat.message.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.MockMessageTreeBuilder;
import com.dianping.cat.message.codec.WaterfallMessageCodec;
import com.dianping.cat.message.codec.WaterfallMessageCodec.Ruler;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;

public class WaterfallMessageCodecTest extends ComponentTestCase {
	private WaterfallMessageCodec m_codec;

	private ByteBuf m_buf;

	private MessageTree m_tree;

	private MockMessageTreeBuilder m_messageTreeBuilder;

	@Before
	public void setupTest() throws Exception {
		setupMockWaterfallMessageCodec();

		m_buf = ByteBufAllocator.DEFAULT.buffer();
		m_messageTreeBuilder = new MockMessageTreeBuilder();
		m_tree = m_messageTreeBuilder.build();
	}

	@Test
	public void testRuler() {
		checkRuler(0, 0, 1);
		checkRuler(3, 3, 1);
		checkRuler(6, 6, 1);
		checkRuler(10, 10, 1);
		checkRuler(11, 6, 2);
		checkRuler(20, 10, 2);
		checkRuler(21, 7, 3);
		checkRuler(34, 7, 5);
		checkRuler(51, 6, 10);
		checkRuler(100, 10, 10);
		checkRuler(1001, 6, 200);
		checkRuler(3476, 7, 500);
		checkRuler(112819, 6, 20000);
	}

	private void checkRuler(int maxValue, int expectedUnitNum, int expectedUnitStep) {
		Ruler ruler = new Ruler(maxValue);

		Assert.assertEquals(String.format("[%s, %s, %s]", maxValue, expectedUnitNum, expectedUnitStep), ruler.toString());
	}

	@Test
	public void testEncode() throws Exception {
		m_codec.encode(m_tree, m_buf);
		m_buf.readInt(); // ignore int

		String expectedHtml = loadExpectedHtmlFromFile();
		String actual = extractActualFromBuffer(m_buf);

		Assert.assertEquals(expectedHtml, actual);
	}

	@Test
	public void testEncodeLength() throws Exception {
		m_codec.encode(m_tree, m_buf);
		int size = m_buf.readInt(); 

		Assert.assertEquals(size, getLengthFromBuffer(m_buf));
	}

	private String extractActualFromBuffer(ByteBuf buf) {
		return removeExcapeCharacters(buf.toString(Charset.forName("utf-8")));
	}

	private int getLengthFromBuffer(ByteBuf buf) {
		return buf.toString(Charset.forName("utf-8")).getBytes().length;
	}

	private void setupMockWaterfallMessageCodec() throws Exception {
		m_codec = (WaterfallMessageCodec) lookup(MessageCodec.class, WaterfallMessageCodec.ID);
	}

	private String loadExpectedHtmlFromFile() throws IOException {
		InputStream in = WaterfallMessageCodecTest.class.getResourceAsStream("WaterfallMessageCodec.html");
		String expectedHtml = Files.forIO().readFrom(in, "utf-8");

		return removeExcapeCharacters(expectedHtml);
	}

	private String removeExcapeCharacters(String html) {
		return html.replaceAll("\r", "");
	}
}
