package com.dianping.cat.consumer.ip.location;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IPSeekerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetIPLocation() throws IOException {
		IPSeeker seeker = IPSeekerFactory.getInstance();
		IPLocation loc = seeker.getIPLocation("112.64.189.69");
		
		System.out.println(loc.getCountry() + " " + loc.getArea());
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < 100; i++) {
			seeker.getIPLocation("112.64.189.69");
		}
		System.out.println(System.currentTimeMillis() - start);
		
		IPSeekerFactory.destroy();
	}
	

}
