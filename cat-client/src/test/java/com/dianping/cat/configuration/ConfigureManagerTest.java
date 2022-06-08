package com.dianping.cat.configuration;

import org.junit.Test;

import com.dianping.cat.ComponentTestCase;
import com.dianping.cat.configuration2.ConfigureManager;

public class ConfigureManagerTest extends ComponentTestCase {
	@Test
	public void test() {
		ConfigureManager manager = lookup(ConfigureManager.class);
		
		System.out.println(manager);
	}
}
