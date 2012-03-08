package com.dianping.cat.report.page.model.transaction;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class TransactionModelServiceTest extends ComponentTestCase {
	@Test
	public void testLookup() throws Exception {
		ModelService<?> local = lookup(ModelService.class, "transaction-local");
		ModelService<?> localhost = lookup(ModelService.class, "transaction-localhost");
		ModelService<?> composite = lookup(ModelService.class, "transaction");

		Assert.assertEquals(LocalTransactionService.class, local.getClass());
		Assert.assertEquals(RemoteTransactionModelService.class, localhost.getClass());
		Assert.assertEquals(CompositeTransactionService.class, composite.getClass());
	}

	@Test
	public void testLocal() throws Exception {
		LocalTransactionService local = (LocalTransactionService) lookup(ModelService.class,
		      "transaction-local");
		ModelResponse<?> response = local.invoke(ModelRequest.from("Cat", "CURRENT"));

		Assert.assertEquals("null", String.valueOf(response.getModel())); // TODO try to mock up a real consumer for test
	}

	@Test
	public void testRemote() throws Exception {
		RemoteTransactionModelService remote = (RemoteTransactionModelService) lookup(ModelService.class,
		      "transaction-localhost");
		ModelRequest request = ModelRequest.from("Cat", "CURRENT");

		Assert.assertEquals("http://localhost:2281/cat/r/t/service?domain=Cat&period=CURRENT", remote.buildUrl(request).toString());

		ModelResponse<?> response = remote.invoke(request);

		Assert.assertEquals("null", String.valueOf(response.getModel())); // TODO start a test server, and do real stuff
	}
}
