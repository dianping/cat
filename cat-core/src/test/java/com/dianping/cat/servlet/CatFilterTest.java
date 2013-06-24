package com.dianping.cat.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Urls;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.site.helper.Files;

public class CatFilterTest {
	@Before
	public void before() {
		Cat.initialize(new File("/data/appdatas/cat/client.xml"));
	}

	@Test
	public void testPassedCatId() throws IOException, InterruptedException {
		String url = "http://localhost:2281/cat/r/t";
		Transaction t = Cat.newTransaction("Test", getClass().getSimpleName());

		try {
			String childId = Cat.createMessageId();
			String id = Cat.getManager().getThreadLocalMessageTree().getMessageId();

			Cat.logEvent("RemoteCall", url, Message.SUCCESS, childId);

			InputStream in = Urls.forIO().connectTimeout(100) // .readTimeout(1000) //
			      .header("X-Cat-Id", childId) //
			      .header("X-Cat-Parent-Id", id) //
			      .header("X-Cat-Root-Id", id) //
			      .openStream(url);

			Files.forIO().readFrom(in, "utf-8");
			System.out.println(childId + ":" + id);
			t.setStatus("ForTest");
		} finally {
			t.complete();
		}

		TimeUnit.MILLISECONDS.sleep(100);
	}
}
