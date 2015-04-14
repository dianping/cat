package com.dianping.cat.message;

import static com.dianping.cat.message.Message.SUCCESS;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.Cat;

@RunWith(JUnit4.class)
public class AppSimulator extends CatTestCase {
	@Test
	public void simulateHierarchyTransaction() throws Exception {
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("URL", "WebPage");
		String id1 = cat.createMessageId();
		String id2 = cat.createMessageId();

		try {
			// do your business here
			t.addData("k1", "v1");
			t.addData("k2", "v2");
			t.addData("k3", "v3");
			Thread.sleep(5);

			cat.logEvent("Type1", "Name1", SUCCESS, "data1");
			cat.logEvent("Type2", "Name2", SUCCESS, "data2");
			cat.logEvent("RemoteCall", "Service1", SUCCESS, id1);
			createChildThreadTransaction(id1, cat.createMessageId(), cat.createMessageId());
			cat.logEvent("Type3", "Name3", SUCCESS, "data3");
			cat.logEvent("RemoteCall", "Service1", SUCCESS, id2);
			createChildThreadTransaction(id2, cat.createMessageId(), cat.createMessageId(), cat.createMessageId());
			cat.logEvent("Type4", "Name4", SUCCESS, "data4");
			cat.logEvent("Type5", "Name5", SUCCESS, "data5");
			t.setStatus(SUCCESS);
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}

	protected void createChildThreadTransaction(final String id, final String... childIds) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				MessageProducer cat = Cat.getProducer();
				Transaction t = cat.newTransaction("Service", "service-" + (int) (Math.random() * 10));

				// override the message id
				Cat.getManager().getThreadLocalMessageTree().setMessageId(id);

				try {
					// do your business here
					t.addData("service data here");
					Thread.sleep(5);

					cat.logEvent("Type1", "Name1", SUCCESS, "data1");
					cat.logEvent("Type2", "Name2", SUCCESS, "data2");

					for (String childId : childIds) {
						cat.logEvent("RemoteCall", "Service1", SUCCESS, childId);
						createChildThreadTransaction(childId);
					}

					cat.logEvent("Type4", "Name4", SUCCESS, "data4");
					cat.logEvent("Type5", "Name5", SUCCESS, "data5");
					t.setStatus(SUCCESS);
				} catch (Exception e) {
					t.setStatus(e);
				} finally {
					t.complete();
				}
			}
		};

		thread.start();

		// wait for it to complete
		try {
			thread.join();
		} catch (InterruptedException e) {
			// ignore it
		}
	}
}
