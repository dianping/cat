package com.dianping.cat;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Trace;

public class CatTest {

	@Test
	public void test() {
		Cat.newTransaction("logTransaction", "logTransaction");
		Cat.newEvent("logEvent", "logEvent");
		Cat.newTrace("logTrace", "logTrace");
		Cat.newHeartbeat("logHeartbeat", "logHeartbeat");
		Throwable cause = new Throwable();
		Cat.logError(cause);
		Cat.logError("message", cause);
		Cat.logTrace("logTrace", "<trace>");
		Cat.logTrace("logTrace", "<trace>", Trace.SUCCESS, "data");
		Cat.logMetric("logMetric", "test", "test");
		Cat.logMetricForCount("logMetricForCount");
		Cat.logMetricForCount("logMetricForCount", 4);
		Cat.logMetricForDuration("logMetricForDuration", 100);
		Cat.logMetricForSum("logMetricForSum", 100);
		Cat.logMetricForSum("logMetricForSum", 100, 100);
		Cat.logEvent("RemoteLink", "Call", Message.SUCCESS, "Cat-0a010680-384736-2061");
		Cat.logEvent("EventType", "EventName");
		Cat.logHeartbeat("logHeartbeat", "logHeartbeat", Message.SUCCESS, null);

		Assert.assertEquals(true, Cat.isInitialized());
	}
}
