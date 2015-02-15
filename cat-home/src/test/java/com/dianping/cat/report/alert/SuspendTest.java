package com.dianping.cat.report.alert;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.sender.AlertEntity;
import com.dianping.cat.report.alert.sender.AlertManager;

public class SuspendTest extends ComponentTestCase {

	@Test
	public void test() {
		AlertManager manager = lookup(AlertManager.class);
		AlertEntity entity = new AlertEntity();
		entity.setDate(new Date()).setContent("test").setLevel("error");
		entity.setMetric("testMetric").setType(AlertType.Network.getName()).setGroup("testGroup");

		try {
			manager.addAlert(entity);
			TimeUnit.SECONDS.sleep(1);
		} catch (Exception ex) {

		}

		Assert.assertTrue(manager.isSuspend(entity.getKey(), 1));
		try {
			TimeUnit.SECONDS.sleep(65);
		} catch (InterruptedException e) {
		}

		Assert.assertFalse(manager.isSuspend(entity.getKey(), 1));
	}

}
