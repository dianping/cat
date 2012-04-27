package com.dianping.cat.report.page.ip.location;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class IPSeekerTest {
	@Test
	public void testGetIPLocationPref() throws IOException {
		IPSeekerManager.initailize(new File("target/ip"));

		String location = IPSeekerManager.getLocation("113.116.205.222");

		System.out.println(location);

		long start = System.currentTimeMillis();

		for (int i = 0; i < 100; i++) {
			IPSeekerManager.getLocation("112.64.189." + i);
		}

		System.out.println(String.format("Done in %s ms for 100 IP lookup.", System.currentTimeMillis() - start));
	}

	@Test
	public void testGetIP() throws IOException {
		File file = new File(IPSeekerManager.class.getResource("qqwry.dat").getFile());
		IPSeeker seeker = new IPSeeker(file.getAbsolutePath(), null);
		IPLocation location = seeker.getIPLocation("113.116.205.222");

		System.out.println(location.getArea() + " " + location.getCountry());

	}

}
