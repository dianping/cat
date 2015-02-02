package com.dianping.cat.statistic;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.statistic.ServerStatistic.Statistic;

public class ServerStatisticManagerTest {

	@Test
	public void test() {
		ServerStatisticManager manager = new ServerStatisticManager();
		String domain = "cat";
		long time = System.currentTimeMillis();

		time = time - time % (60 * 1000);

		manager.addBlockLoss(1);
		manager.addBlockTime(2);
		manager.addBlockTotal(3);
		manager.addMessageDump(4);
		manager.addMessageDumpLoss(5);
		manager.addMessageSize(domain,6);
		manager.addMessageTotal(7);
		manager.addMessageTotalLoss(8);
		manager.addPigeonTimeError(9);
		manager.addNetworkTimeError(10);
		manager.addProcessDelay(11);
		manager.addMessageSize(domain, 1);
		manager.addMessageTotal(domain, 2);
		manager.addMessageTotalLoss(domain, 3);

		Assert.assertEquals(1, findState(manager, time).getBlockLoss());
		Assert.assertEquals(2, findState(manager, time).getBlockTime());
		Assert.assertEquals(3, findState(manager, time).getBlockTotal());
		Assert.assertEquals(4, findState(manager, time).getMessageDump());
		Assert.assertEquals(5, findState(manager, time).getMessageDumpLoss());
		Assert.assertEquals(7, findState(manager, time).getMessageSize());
		Assert.assertEquals(7, findState(manager, time).getMessageTotal());
		Assert.assertEquals(11, findState(manager, time).getMessageTotalLoss());
		Assert.assertEquals(9, findState(manager, time).getPigeonTimeError());
		Assert.assertEquals(10, findState(manager, time).getNetworkTimeError());
		Assert.assertEquals(11.0, findState(manager, time).getProcessDelaySum());
		Assert.assertEquals(11.0, findState(manager, time).getAvgProcessDelay());
		Assert.assertEquals(1, findState(manager, time).getProcessDelayCount());
		Assert.assertEquals(7, findState(manager, time).getMessageSizes().get(domain).get());
		Assert.assertEquals(2, findState(manager, time).getMessageTotals().get(domain).get());
		Assert.assertEquals(3, findState(manager, time).getMessageTotalLosses().get(domain).get());


		manager.addMessageTotal(7);
		manager.addMessageTotalLoss(8);
		manager.addPigeonTimeError(9);
		Assert.assertEquals(14, findState(manager, time).getMessageTotal());
		Assert.assertEquals(19, findState(manager, time).getMessageTotalLoss());
		Assert.assertEquals(18, findState(manager, time).getPigeonTimeError());
		
		manager.removeState(time);

		Assert.assertEquals(true, null != manager.findOrCreateState(time));
	}

	private Statistic findState(ServerStatisticManager manager, long time) {
		Statistic state = manager.findOrCreateState(time);

		if (state == null) {
			state = manager.findOrCreateState(time + 60 * 1000);
		}
		return state;
	}
}
