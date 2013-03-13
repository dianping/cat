package com.dianping.cat.report.page.model.transaction;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;

@RunWith(JUnit4.class)
public class TransactionModelServiceTest extends ComponentTestCase {

	public void testLookup() throws Exception {
		ModelService<?> local = lookup(ModelService.class, "transaction-local");
		ModelService<?> composite = lookup(ModelService.class, "transaction");

		Assert.assertEquals(LocalTransactionService.class, local.getClass());
		Assert.assertEquals(CompositeTransactionService.class, composite.getClass());
	}

	@Test
	public void testLocal() throws Exception {
		LocalTransactionService local = (LocalTransactionService) lookup(ModelService.class, "transaction-local");
		ModelResponse<?> response = local.invoke(ModelRequest.from("Cat", "CURRENT"));

		Assert.assertEquals(true, response != null);
	}
}
