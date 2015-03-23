package com.dianping.cat.service;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.service.IpService.IpInfo;

public class IpServiceTest extends ComponentTestCase {
	@Test
	public void getSystemTime() throws Exception {
		System.err.println(System.currentTimeMillis() - 60 * 1000 * 2);
	}

	@Test
	public void test() throws Exception {
		IpService service = (IpService) lookup(IpService.class);

		for (int i = 0; i < 10000; i++) {
			String ip = i % 255 + "." + i % 255 + "." + i % 255 + "." + i % 255;
			IpInfo info = service.findIpInfoByString(ip);

			if (info != null) {
				System.out.print(ip + " " + info.getChannel());
				System.out.print(" " + info.getCity());
				System.out.println(" " + info.getProvince());
			}
		}
	}

}
