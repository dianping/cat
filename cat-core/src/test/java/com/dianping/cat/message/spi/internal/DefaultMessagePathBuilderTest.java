package com.dianping.cat.message.spi.internal;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

public class DefaultMessagePathBuilderTest {

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
