package com.dianping.cat.job.joblet;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.job.spi.joblet.JobletRunner;
import org.unidal.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class HelpJobletTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		JobletRunner runner = lookup(JobletRunner.class);
		int exitCode = runner.run("help");

		Assert.assertEquals(-1, exitCode);
	}
}
