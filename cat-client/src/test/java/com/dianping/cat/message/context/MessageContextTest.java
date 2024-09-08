package com.dianping.cat.message.context;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.dianping.cat.ComponentTestCase;
import com.dianping.cat.message.Transaction;

public class MessageContextTest extends ComponentTestCase {
	@Test
	public void testInstrument() {
		int index = 1;
		TraceContext ctx = TraceContextHelper.threadLocal();
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
	}

	@Test
	@Ignore
	public void testAsyncContext() {
		TraceContext ctx = TraceContextHelper.threadLocal();

		Assert.assertNotNull(ctx);

		HttpServletRequest req = null;

		TraceContextHelper.extractFrom(req);
	}
}
