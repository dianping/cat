package com.dianping.cat.consumer.performance;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.MockMessageBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class MetricPerformanceTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		MetricAnalyzer analyzer = (MetricAnalyzer)lookup(MessageAnalyzer.class,MetricAnalyzer.ID);
		MessageTree tree = buildMessage();

		System.out.println(tree);
		long current = System.currentTimeMillis();

		long size = 10000000l;
		for (int i = 0; i < size; i++) {
			analyzer.process(tree);
		}
		System.out.println(analyzer.getReport("TuanGou"));
		System.out.println("Cost " + (System.currentTimeMillis() - current) / 1000);
		// 21
	}

	public MessageTree buildMessage() {
		Message message = new MockMessageBuilder() {

			@Override
			public MessageHolder define() {
				TransactionHolder t = t("URL", "/index", 112819)
				      .child(m("order", "order", "quantity=1"))
				      .child(m("payment.pending", "payment.pending", "amount=1"))
				      .child(m("payment.success", "payment.success", "amount=1"))
				      .child(
				            t("URL", "/detail", 112819).child(m("order", "order", "quantity=1"))
				                  .child(m("payment.pending", "payment.pending", "amount=1"))
				                  .child(m("payment.success", "payment.success", "amount=1")))
				      .child(
				            t("URL", "/order/submitOrder", 112819).child(m("order", "order", "quantity=1"))
				                  .child(m("payment.pending", "payment.pending", "amount=1"))
				                  .child(m("payment.success", "payment.success", "amount=1")));

				return t;
			}
		}.build();

		MessageTree tree = new DefaultMessageTree();
		tree.setDomain("TuanGou");
		tree.setHostName("test");
		tree.setIpAddress("test");
		tree.setThreadGroupName("test");
		tree.setThreadId("test");
		tree.setThreadName("test");
		tree.setMessage(message);
		return tree;
	}

}
