package com.dianping.cat.message.context;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.message.Transaction;

public class MessageContextTest {
	@Test
	public void testInstrument() {
		int index = 1;
		MessageContext ctx = new DefaultMessageContext();
		Transaction t = ctx.newTransaction("TransactionType", "TransactionName-" + (index++));

		try {
			Transaction t2 = ctx.newTransaction("TransactionType", "TransactionName-" + (index++));

			ctx.newEvent("EventType", "EventName-" + (index++)).success().complete();
			ctx.newEvent("EventType", "EventName-" + (index++)).success().complete();
			ctx.newEvent("EventType", "EventName-" + (index++)).success().complete();

			t2.success().complete();

			ctx.newEvent("EventType", "EventName-" + (index++)).success().complete();
			ctx.newEvent("EventType", "EventName-" + (index++)).success().complete();
			ctx.newEvent("EventType", "EventName-" + (index++)).success().complete();

			ctx.newHeartbeat("HeartbeatType", "HeartbeatName-" + (index++)).success().complete();
			ctx.newHeartbeat("HeartbeatType", "HeartbeatName-" + (index++)).success().complete();

			ctx.newTrace("OrderService", "placeOrder").success().complete();

			t.success();
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}

		System.out.println(ctx.getMessageTree());
	}

	@Test
	public void testAsyncContext() {
		MessageContext ctx = MessageContextHelper.getThreadLocal();

		Assert.assertNotNull(ctx);
		
		MessageContextHelper.extractFrom(null);
	}
}
