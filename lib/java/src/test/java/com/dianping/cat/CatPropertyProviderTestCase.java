package com.dianping.cat;

import org.junit.Assert;
import org.junit.Test;

public class CatPropertyProviderTestCase {

	@Test
	public void test() {
		
		CatPropertyProvider inst = CatPropertyProvider.INST;
		Assert.assertNotNull(inst);
		
		String catHost = inst.getProperty("CAT_HOST", "A");
		Assert.assertEquals("A", catHost);
	}
}
