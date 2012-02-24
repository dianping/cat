package com.dianping.cat.message;

import org.junit.After;
import org.junit.Before;

import com.dianping.cat.Cat;
import com.site.lookup.ComponentTestCase;

public abstract class CatTestCase extends ComponentTestCase {
	@Before
	public void before() throws Exception {
		Cat.initialize(getContainer(), null);
		Cat.setup(null);
	}

	@After
	public void after() throws Exception {
		Cat.reset();
		Cat.destroy();
	}
}