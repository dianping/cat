package com.dianping.cat.message.spi.internal;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.site.helper.Splitters;

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
		String path = "20120807/14/Cat-Cat-192.168.64.153";
		Object[] objects = format.parse(path);
		Date timestamp = (Date) objects[0];
		List<String> parts = Splitters.by('-').split((String) objects[1]);
		String domain = parts.get(1);
		String ip = parts.get(2);
		String id = domain + "-" + convertToHex(ip) + "-" + timestamp.getTime() / 3600000L + "-0";

		Assert.assertEquals("Cat-c0a84099-373422-0", id);
	}

	@Test
	public void testRemotePathBuilder() {
		DefaultMessagePathBuilder builder = new DefaultMessagePathBuilder();
		long hour = 60 * 60 * 1000;
		long current = System.currentTimeMillis();
		long currentHour = current - current % hour;

		Date date = new Date(currentHour);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd/HH/");
		String dateStr = sdf.format(date);
		String ip = "127.0.0.1";
		String path = builder.getMessageRemoteIdPath(ip, date);

		Assert.assertEquals(dateStr + "remoteid-" + ip, path);

		date = new Date(currentHour - hour);
		dateStr = sdf.format(date);
		path = builder.getMessageRemoteIdPath(ip, date);

		Assert.assertEquals(dateStr + "remoteid-" + ip, path);

		date = new Date(currentHour + 5 * hour);
		dateStr = sdf.format(date);
		path = builder.getMessageRemoteIdPath(ip, date);

		Assert.assertEquals(dateStr + "remoteid-" + ip, path);
	}
}
