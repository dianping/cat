package com.dianping.cat.report.page.model.transaction;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.model.ModelPeriod;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;

public class TransactionModelServiceTest extends ComponentTestCase {
	@Test
	public void testLookup() throws Exception {
		ModelService<?> local = lookup(ModelService.class, "transaction-local");
		ModelService<?> composite = lookup(ModelService.class, "transaction");

		Assert.assertEquals(LocalTransactionService.class, local.getClass());
		Assert.assertEquals(CompositeTransactionService.class, composite.getClass());
	}

	@Test
	public void testLocal() throws Exception {
		LocalTransactionService local = (LocalTransactionService) lookup(ModelService.class, "transaction-local");
		ModelResponse<?> response = local.invoke(new ModelRequest("Cat", ModelPeriod.CURRENT));

		Assert.assertEquals(true, response != null);
	}
}
