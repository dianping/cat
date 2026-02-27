package com.dianping.cat.status;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.status.model.StatusInfoHelper;
import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.support.Files;

public class StatusModelTest {
	@Test
	public void test() throws Exception {
		InputStream in = getClass().getResourceAsStream("status.xml");
		String expected = Files.forIO().readUtf8String(in);
		StatusInfo m1 = StatusInfoHelper.fromXml(expected);
		StatusInfo m2 = StatusInfoHelper.fromXml(m1.toString());
		String actual = StatusInfoHelper.asXml(m2);

		Assert.assertEquals(m1, m2);
		Assert.assertEquals(expected.replace("\r\n", "\n"), actual.replace("\r\n", "\n"));
	}
}
