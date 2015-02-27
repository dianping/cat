package com.dianping.cat.consumer.performance;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.MockMessageBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class DependencyPerformanceTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		DependencyAnalyzer analyzer = (DependencyAnalyzer)lookup(MessageAnalyzer.class,DependencyAnalyzer.ID);
		MessageTree tree = buildMessage();

		long current = System.currentTimeMillis();

		long size = 10000000l;
		for (int i = 0; i < size; i++) {
			analyzer.process(tree);
		}
		System.out.println("Cost " + (System.currentTimeMillis() - current) / 1000);
		System.out.println(analyzer.getReport("cat"));
		// cost 26
	}

	public MessageTree buildMessage() {
		Message message = new MockMessageBuilder() {
			@Override
			public MessageHolder define() {
				TransactionHolder t = t("URL", "GET", 112819)
				      .child(
				            t("PigeonCall",
				                  "groupService:groupNoteService_1.0.0:updateNoteDraft(Integer,Integer,String,String)", "",
				                  100).child(e("PigeonCall.server", "10.1.2.99:2011", "Execute[34796272]")).child(e("PigeonCall.app", "app1", "Execute[34796272]")))
				      .child(
				            t("PigeonCall",
				                  "groupService:groupNoteService_1.0.1:updateNoteDraft1(Integer,Integer,String,String)",
				                  "", 100).child(e("PigeonCall.server", "10.1.2.199:2011", "Execute[34796272]")).child(e("PigeonCall.app", "app2", "Execute[34796272]")))
				      .child(
				            t("PigeonCall",
				                  "groupService:groupNoteService_1.0.1:updateNoteDraft2(Integer,Integer,String,String)",
				                  "", 100).child(e("PigeonCall.server", "10.1.2.199:2011", "Execute[34796272]")).child(e("PigeonCall.app", "app3", "Execute[34796272]")))
				      .child(
				            t("PigeonCall",
				                  "groupService:groupNoteService_1.0.1:updateNoteDraft3(Integer,Integer,String,String)",
				                  "", 100).child(e("PigeonCall.server", "10.1.2.199:2011", "Execute[34796272]")).child(e("PigeonCall.app", "app4", "Execute[34796272]")))
				      .child(
				            t("PigeonCall",
				                  "groupService:groupNoteService_1.0.1:updateNoteDraft4(Integer,Integer,String,String)",
				                  "", 100).child(e("PigeonCall.server", "10.1.2.199:2011", "Execute[34796272]")).child(e("PigeonCall.app", "app5", "Execute[34796272]")))
				      .child(
				            t("PigeonService",
				                  "groupService:groupNoteService_1.0.1:updateNoteDraft5(Integer,Integer,String,String)",
				                  "", 100).child(e("PigeonService.client", "10.1.7.127:37897", "Execute[34796272]")).child(e("PigeonService.app", "app6", "Execute[34796272]")))
				      .child(
				            t("PigeonService",
				                  "groupService:groupNoteService_1.0.1:updateNoteDraft7(Integer,Integer,String,String)",
				                  "", 100).child(e("PigeonService.client", "10.1.7.127:37897", "Execute[34796272]")).child(e("PigeonService.app", "app7", "Execute[34796272]")))
				      .child(
				            t("PigeonService",
				                  "groupService:groupNoteService_1.0.1:updateNoteD1aft6(Integer,Integer,String,String)",
				                  "", 100).child(e("PigeonService1.client", "10.1.7.128:37897", "Execute[34796272]")).child(e("PigeonService.app", "app8", "Execute[34796272]")));

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
		tree.setMessageId("MobileApi-0a01077f-379304-1362256");
		return tree;
	}

}
