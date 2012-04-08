package com.dianping.cat.report.page.model.event;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.Cat;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class EventModelServiceTest extends ComponentTestCase {
	@Before
	public void before() throws Exception {
		if (!Cat.isInitialized()) {
			Cat.initialize(null);
		}
	}

	@Test
	public void testLookup() throws Exception {
		ModelService<?> local = lookup(ModelService.class, "event-local");
		ModelService<?> composite = lookup(ModelService.class, "event");

		Assert.assertEquals(LocalEventService.class, local.getClass());
		Assert.assertEquals(CompositeEventService.class, composite.getClass());
	}

	@Test
	public void testLocal() throws Exception {
		LocalEventService local = (LocalEventService) lookup(ModelService.class, "event-local");
		ModelResponse<?> response = local.invoke(ModelRequest.from("Cat", "CURRENT"));

		Assert.assertEquals("null", String.valueOf(response.getModel()));
	}
}
