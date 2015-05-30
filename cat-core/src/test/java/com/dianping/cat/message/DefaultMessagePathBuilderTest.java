package com.dianping.cat.message;

import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Splitters;

import com.dianping.cat.message.DefaultPathBuilder;

public class DefaultMessagePathBuilderTest {
	private String convertToHex(String ip) {
		List<String> items = Splitters.by(".").noEmptyItem().split(ip);
		byte[] bytes = new byte[4];

		for (int i = 0; i < 4; i++) {
			bytes[i] = (byte) Integer.parseInt(items.get(i));
		}

		StringBuilder sb = new StringBuilder(bytes.length / 2);

		for (byte b : bytes) {
			sb.append(Integer.toHexString((b >> 4) & 0x0F));
			sb.append(Integer.toHexString(b & 0x0F));
		}

		return sb.toString();
	}

	@Test
	public void testParseMessageIdFromPath() throws Exception {
		MessageFormat format = new MessageFormat("{0,date,yyyyMMdd'/'HH}/{1}");

		for (Format child : format.getFormats()) {
			if (child instanceof SimpleDateFormat) {
				((SimpleDateFormat) child).setTimeZone(TimeZone.getTimeZone("GMT+8"));
			}
		}

		String path = "20120807/14/Cat-Cat-192.168.64.153";
		Object[] objects = format.parse(path);
		Date timestamp = (Date) objects[0];
		List<String> parts = Splitters.by('-').split((String) objects[1]);
		String domain = parts.get(1);
		String ip = parts.get(2);
		String id = domain + "-" + convertToHex(ip) + "-" + timestamp.getTime() / 3600000L + "-0";

		Assert.assertEquals("Cat-c0a84099-373422-0", id);

		DefaultPathBuilder builder = new DefaultPathBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
		String str = builder.getLogviewPath(sdf.parse("2013010101"), "transaction");

		Assert.assertEquals("20130101/01/transaction", str);
	}

}
