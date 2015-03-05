package com.dianping.cat.consumer.performance;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.MockMessageBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class ProblemPerformanceTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		ProblemAnalyzer analyzer = (ProblemAnalyzer) lookup(MessageAnalyzer.class, ProblemAnalyzer.ID);
		MessageTree tree = buildMessage();

		long current = System.currentTimeMillis();

		long size = 10000000l;
		for (int i = 0; i < size; i++) {
			analyzer.process(tree);
		}
		System.out.println("Cost " + (System.currentTimeMillis() - current) / 1000);
		System.out.println(analyzer.getReport("cat"));
		// cost 64
	}

	@Test
	public void test2() throws Exception {
		ProblemAnalyzer analyzer = (ProblemAnalyzer) lookup(MessageAnalyzer.class, ProblemAnalyzer.ID);
		MessageTree tree = buildMessage();

		long current = System.currentTimeMillis();

		long size = 10000000l;
		for (int i = 0; i < size; i++) {
			Event event = new DefaultEvent("Exception", "name" + i % 100);
			tree.setMessage(event);
			analyzer.process(tree);

			Event event2 = new DefaultEvent("Error", "name" + i % 100);
			tree.setMessage(event2);
			analyzer.process(tree);

			Event event3 = new DefaultEvent("Call", "name" + i % 100);
			tree.setMessage(event3);
			analyzer.process(tree);
		}
		System.out.println(analyzer.getReport("cat"));
		System.out.println("Cost " + (System.currentTimeMillis() - current) / 1000);
		// cost 64
	}

	public MessageTree buildMessage() {
		Message message = new MockMessageBuilder() {
			@Override
			public MessageHolder define() {
				TransactionHolder t = t("WEB CLUSTER", "GET", 112819)
				      .at(1348374838231L)
				      .after(1300)
				      .child(t("QUICKIE SERVICE", "gimme_stuff", 1571))
				      .after(100)
				      .child(e("SERVICE", "event1"))
				      .child(e("Excetion", "NullPointException1"))
				      .after(100)
				      .child(h("SERVICE", "heartbeat1"))
				      .after(100)
				      .child(
				            t("WEB SERVER", "GET", 109358)
				                  .after(1000)
				                  .child(t("SOME SERVICE", "get", 4345) //
				                        .after(4000).child(t("MEMCACHED", "Get", 279)))
				                  .mark()
				                  .after(200)
				                  .child(t("MEMCACHED", "Inc", 319))
				                  .reset()
				                  .after(500)
				                  .child(
				                        t("BIG ASS SERVICE", "getThemDatar", 97155).after(1000).mark()
				                              .child(t("SERVICE", "getStuff", 3760)).reset()
				                              .child(t("DATAR", "findThings", 94537)).after(200)
				                              .child(t("THINGIE", "getMoar", 1435)) //
				                  )
				                  .after(100)
				                  .mark()
				                  .child(
				                        t("OTHER DATA SERVICE", "get", 4394).after(1000).mark()
				                              .child(t("MEMCACHED", "Get", 378)).reset().child(t("MEMCACHED", "Get", 3496)) //
				                  )
				                  .reset()
				                  .child(
				                        t("FINAL DATA SERVICE", "get", 4394).after(1000).mark()
				                              .child(t("MEMCACHED", "Get", 386)).reset().child(t("MEMCACHED", "Get", 322))
				                              .reset().child(t("MEMCACHED", "Get", 322))
				                              .child(e("Excetion", "NullPointException"))) //
				      );

				return t;
			}
		}.build();

		MessageTree tree = new DefaultMessageTree();
		tree.setDomain("cat");
		tree.setHostName("test");
		tree.setIpAddress("test");
		tree.setThreadGroupName("test");
		tree.setThreadId("test");
		tree.setThreadName("test");
		tree.setMessage(message);
		return tree;
	}

}
