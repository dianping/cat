package com.dianping.cat.hadoop.sql;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.helper.Stringizers;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.spi.MessageStorage;
import com.dianping.cat.message.spi.MessageTree;

@RunWith(JUnit4.class)
public class SqlJobDataProduceTest extends ComponentTestCase {
	@After
	public void after() throws Exception {
		Cat.reset();
		Cat.destroy();
	}

	@Before
	public void before() throws Exception {
		Cat.initialize(getContainer(), new File(getClass().getResource("cat.xml").getPath()));
		Cat.setup(null);
	}

	@Test
	public void test() throws Exception {
		MessageStorage storage = lookup(MessageStorage.class);
		MessageProducer producer = lookup(MessageProducer.class);
		MockTransportManager transport = (MockTransportManager) lookup(TransportManager.class);

		long currentTimeMillis = System.currentTimeMillis();
		long currentHour = currentTimeMillis - currentTimeMillis % (60 * 60 * 1000);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 12000; j++) {
				Transaction t = producer.newTransaction("URL", "MyPage" + (int) (j / 500));

				try {
					// do your business here
					t.addData("k1", "v1");
					t.addData("k2", "v2");
					t.addData("k3", "v3");

					Thread.sleep(1);

					producer.logEvent("URL", "Payload", Message.SUCCESS, "host=my-host&ip=127.0.0.1&agent=...");
					producer.logEvent("URL", "Payload", Message.SUCCESS, "host=my-host&ip=127.0.0.1&agent=...");
					producer.logEvent("URL", "Payload", Message.SUCCESS, "host=my-host&ip=127.0.0.1&agent=...");
					producer.logEvent("URL", "Payload", Message.SUCCESS, "host=my-host&ip=127.0.0.1&agent=...");

					String sqlName = "Project.insert" + j / 500;
					String sqlParaMeter = "SQLParaMeter" + j / 500;
					String sqlStatement = "select * from	table where id=\"1\"\n	order by id	desc";
					Transaction sqlTran = producer.newTransaction("SQL", sqlName);

					producer
					      .logEvent("SQL.PARAM", sqlParaMeter, Transaction.SUCCESS, Stringizers.forJson().compact().from(sqlParaMeter));
					sqlTran.addData(sqlStatement);

					sqlTran.complete();

					DefaultTransaction sqlInternalTran = (DefaultTransaction) sqlTran;
					sqlInternalTran.setDurationInMillis((long) Math.pow(2, j % 12));
					if (j % 2 != 0) {
						sqlTran.setStatus(Message.SUCCESS);
					} else {
						sqlTran.setStatus("Error");
					}
					sqlInternalTran.setTimestamp(currentHour + (j % 60) * 1000 * 60);

					DefaultTransaction def = (DefaultTransaction) sqlTran;
					def.setDurationInMillis(j % 100 + 50);
					def.setTimestamp(currentHour + (j % 60) * 1000 * 60);
					t.setStatus(Message.SUCCESS);
				} catch (Exception e) {
					t.setStatus(e);
				} finally {
					t.complete();
				}

				MessageTree tree = transport.getQueue().poll();

				Assert.assertNotNull("No message found in the queue!", tree);

				tree.setDomain("domain" + i);
				storage.store(tree);
			}
		}
	}
}
