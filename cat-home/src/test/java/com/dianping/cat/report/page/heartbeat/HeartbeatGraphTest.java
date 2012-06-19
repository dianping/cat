package com.dianping.cat.report.page.heartbeat;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.hadoop.dal.Graph;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class HeartbeatGraphTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		Handler handler = new Handler();

		handler.getHeartBeatData(creatModel(), creatPayload());
	}

	private Model creatModel() {
		return null;
	}

	private Payload creatPayload() {
		return null;
	}

	private Graph creatGraph(Date start) {
		return null;
	}
}
