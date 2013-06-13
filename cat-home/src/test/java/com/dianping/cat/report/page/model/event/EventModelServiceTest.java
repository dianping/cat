package com.dianping.cat.report.page.model.event;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Cat;
import com.dianping.cat.report.model.ModelPeriod;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;

public class EventModelServiceTest extends ComponentTestCase {
	@Before
	public void before() {
		Cat.initialize(getContainer(), null);
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
		ModelResponse<?> response = local.invoke(new ModelRequest("Cat", ModelPeriod.CURRENT));

		Assert.assertEquals(true, response != null);
	}
}
