package com.dianping.cat.message.spi.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import junit.framework.Assert;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.core.WaterfallMessageCodec.Ruler;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class WaterfallMessageCodecTest extends ComponentTestCase {
	private WaterfallMessageCodec m_codec;
	private ChannelBuffer m_buf;
	private MessageTree m_tree;

	@Before
	public
	void setupTest() throws Exception {
		setupMockWaterfallMessageCodec();
		
		m_buf = ChannelBuffers.dynamicBuffer();
		m_tree = new DefaultMessageTree();
   }

	@Test
	public void testMockMode() throws Exception {
		Assert.assertEquals("WaterfallMessageCodec is in mock mode.", true, m_codec.isMockMode());
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
	public void testEncodeLength() throws Exception{

		m_codec.encode(m_tree, m_buf);
		int size = m_buf.readInt(); // ignore int

		Assert.assertEquals(size, getLengthFromBuffer(m_buf));
	}
	

	private String extractActualFromBuffer(ChannelBuffer buf) {
		return removeExcapeCharacters(buf.toString(Charset.forName("utf-8")));
	}

	private int getLengthFromBuffer(ChannelBuffer buf) {
		return buf.toString(Charset.forName("utf-8")).getBytes().length;
	}

	private void setupMockWaterfallMessageCodec() throws Exception {
		m_codec = (WaterfallMessageCodec) lookup(MessageCodec.class, WaterfallMessageCodec.ID);
		m_codec.setMockMode(true);
	}

	private String loadExpectedHtmlFromFile() throws IOException {
		InputStream in = WaterfallMessageCodecTest.class.getResourceAsStream("WaterfallMessageCodec.html");
		String expectedHtml = Files.forIO().readFrom(in, "utf-8");

		return removeExcapeCharacters(expectedHtml);
	}

	private String removeExcapeCharacters(String html) {
		return html.replaceAll("\\s*", "");
	}
}
