package com.dianping.cat.job.joblet;

import java.io.IOException;
import java.lang.ref.SoftReference;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.job.spi.joblet.AbstractJobletContext;
import com.dianping.cat.job.spi.joblet.Joblet;
import com.dianping.cat.job.spi.joblet.JobletRunner;
import com.dianping.cat.job.spi.mapreduce.MessageTreeWritable;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class LocationJobletTest extends ComponentTestCase {
	private Message buildMockMessage(double lat, double lng) {
		DefaultTransaction t = new DefaultTransaction("URL", "/location.bin", null);
		DefaultEvent e = new DefaultEvent("URL", "Payload");

		e.addData("HTTP/GET /location.bin?lat=" + lat + "&lng=" + lng + "&accuracy=1385");
		e.setStatus(Message.SUCCESS);
		e.complete();
		t.addChild(e);
		t.setStatus(Message.SUCCESS);
		t.complete();

		t.setTimestamp(1337336291944L);
		return t;
	}

	private void checkMapper(double lat, double lng, final String expectedOutputKey) throws Exception {
		LocationJoblet joblet = (LocationJoblet) lookup(Joblet.class, "location");
		MessageTree tree = new DefaultMessageTree();
		MessageTreeWritable treeWritable = new MessageTreeWritable(tree);
		final SoftReference<Boolean> flag = new SoftReference<Boolean>(true);

		tree.setMessage(buildMockMessage(lat, lng));

		joblet.map(new AbstractJobletContext() {
			@Override
			public void write(Object key, Object value) throws IOException, InterruptedException {
				flag.clear();
				Assert.assertEquals(expectedOutputKey, key.toString());
			}
		}, treeWritable);

		Assert.assertNull("Mapper has not been triggered.", flag.get());

		release(joblet);
	}

	@Test
	public void testJoblet() throws Exception {
		JobletRunner runner = lookup(JobletRunner.class);
		int exitCode = runner.run("location", "target/12", "-Doutputter=report");

		Assert.assertEquals(0, exitCode);

		// for (int i = 0; i < 8; i++) {
		// int exitCode = runner.run("location", "/Users/qmwu/data/20120517/" + (i
		// < 10 ? "0" + i : "" + i));
		//
		// Assert.assertEquals(0, exitCode);
		// }
	}

	@Test
	public void testMapper() throws Exception {
		checkMapper(1, 2, "1.0|2.0|1337336291944");
		checkMapper(31.20334, 121.58017, "31.20334|121.58017|1337336291944");
	}
}
