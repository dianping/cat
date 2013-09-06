package com.dianping.cat.report.page.cross;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.DomainManager;
import com.dianping.cat.DomainManager.ReloadDomainTask;

public class CMDBTest {

	@Test
	public void test() throws Exception {
		String content = Files.forIO().readFrom(getClass().getResourceAsStream("cmdb.json"), "utf-8");

		DomainManager manager = new DomainManager();
		ReloadDomainTask task = manager.new ReloadDomainTask();

		Assert.assertEquals("promo-server", task.parseIp(content));
	}

}
