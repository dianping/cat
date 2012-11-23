package com.dianping.cat.job.joblet;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.job.spi.joblet.JobletRunner;
import org.unidal.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class SqlJobletTest extends ComponentTestCase {
	@Test
	public void testJoblet() throws Exception {
		JobletRunner runner = lookup(JobletRunner.class);
		int exitCode = runner.run("sql", "target/12", "-Dreducers=1");

		Assert.assertEquals(0, exitCode);
	}

	@Test
	public void testMapper() throws Exception {
	}
}
