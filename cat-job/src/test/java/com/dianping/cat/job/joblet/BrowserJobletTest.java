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
public class BrowserJobletTest extends ComponentTestCase {
	private Message buildMockMessage(String userAgent) {
		DefaultTransaction t = new DefaultTransaction("URL", "test", null);
		DefaultEvent e = new DefaultEvent("URL", "ClientInfo");

		e.addData("RemoteIp=...&Agent=" + userAgent);
		e.setStatus(Message.SUCCESS);
		e.complete();
		t.addChild(e);
		t.setStatus(Message.SUCCESS);
		t.complete();

		return t;
	}

	private void checkMapper(String userAgent, final String expectedOutputKey) throws Exception {
		BrowserJoblet joblet = (BrowserJoblet) lookup(Joblet.class, "browser");
		MessageTree tree = new DefaultMessageTree();
		MessageTreeWritable treeWritable = new MessageTreeWritable(tree);
		final SoftReference<Boolean> flag = new SoftReference<Boolean>(true);

		tree.setMessage(buildMockMessage(userAgent));

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
		int exitCode = runner.run("browser", "target/12");

		Assert.assertEquals(0, exitCode);
	}

	@Test
	public void testMapper() throws Exception {
		checkMapper("MApi 1.0 (dpscope 4.8.1 appstore; iPhone 5.1)", "MApi 1.0|4.8.1|iPhone|5.1|appstore|dpscope|");
		checkMapper("MApi 1.0 (dpscopehd 1.5.1 appstore; iPad 5.1)", "MApi 1.0|1.5.1|iPad|5.1|appstore|dpscopehd|");
		checkMapper("MApi 1.0 (dpscope 4.9 appstore; iPhone 4.2.1)", "MApi 1.0|4.9|iPhone|4.2.1|appstore|dpscope|");
		checkMapper("MApi 1.0 (dpscope 4.6.1 appstore; iPhone 4.2.1)", "MApi 1.0|4.6.1|iPhone|4.2.1|appstore|dpscope|");
		checkMapper("MApi 1.0 (dpscope 4.8.1 ppzhushou; iPhone 5.0.1)", "MApi 1.0|4.8.1|iPhone|5.0.1|ppzhushou|dpscope|");
		checkMapper("MApi 1.0 (dpscope 4.8.1 appstore; iPhone 5.0.1)", "MApi 1.0|4.8.1|iPhone|5.0.1|appstore|dpscope|");
		checkMapper("MApi 1.0 (dpscope 4.7 appstore; iPhone 5.0.1)", "MApi 1.0|4.7|iPhone|5.0.1|appstore|dpscope|");
		checkMapper("MApi 1.0 (com.dianping.v1 4.7.1 dianping Lenovo_A60; Android 2.3.3)",
		      "MApi 1.0|4.7.1|Android|2.3.3|dianping|com.dianping.v1|Lenovo_A60");
		checkMapper("MApi 1.0 (com.dianping.v1 4.9.1.193 null SCH-I519; Android 2.3.6)",
		      "MApi 1.0|4.9.1.193|Android|2.3.6|null|com.dianping.v1|SCH-I519");
		checkMapper("MApi 1.0 (com.dianping.v1 4.7.1 91zs500 HTC_Desire_S; Android 2.3.5)",
		      "MApi 1.0|4.7.1|Android|2.3.5|91zs500|com.dianping.v1|HTC_Desire_S");
	}
}
