package com.dianping.cat;

import org.junit.Test;

public class HashTest {

	@Test
	public void test() {
		print("mobile-log-web");
		print("shoppic-service");
		print("pay-promo-city-detail-service");
		print("Search-realtime-shopwishservice");
		print("credit-collectdata-server");
		print("alpaca-admin");
		print("mapcenter-service");
		print("RT-Traffic-Nginx");
		print("mobile-config-web");
	}

	private void print(String domain) {
		System.out.println(domain + " " + Math.abs((""+domain).hashCode()) % 3);
	}
}
