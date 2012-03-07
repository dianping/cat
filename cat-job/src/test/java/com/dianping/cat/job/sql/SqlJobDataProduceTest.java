package com.dianping.cat.job.sql;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.job.hdfs.CatTestCase;
import com.dianping.cat.job.hdfs.HdfsMessageStorage;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.io.InMemoryQueue;
import com.dianping.cat.message.spi.MessageStorage;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Stringizers;

@RunWith(JUnit4.class)
public class SqlJobDataProduceTest extends CatTestCase {
	@Test
	public void test() throws Exception {
		MessageStorage storage = lookup(MessageStorage.class, "hdfs");
		MessageProducer producer = lookup(MessageProducer.class);
		InMemoryQueue queue = lookup(InMemoryQueue.class);

		for (int i = 0; i < 10000; i++) {
			Transaction t = producer.newTransaction("URL", "MyPage" + (int) (i / 500));

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

				String sqlStatement = "SQLStatement" + i / 500;
				String sqlParaMeter = "SqlParaMeters";
				Transaction sqlTran = producer.newTransaction("SQL", sqlStatement);

				producer.logEvent("SQL.PARAM", sqlStatement, Transaction.SUCCESS,
				      Stringizers.forJson().compact().from(sqlParaMeter));
				sqlTran.addData(sqlStatement + "detail...");

				sqlTran.complete();

				if (i % 2 == 1) {
					sqlTran.setStatus(Message.SUCCESS);
				} else {
					sqlTran.setStatus("Error");
				}

				DefaultTransaction def = (DefaultTransaction) sqlTran;
				def.setDuration(i % 100);

				t.setStatus(Message.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
			} finally {
				t.complete();
			}

			MessageTree tree = queue.poll(0);

			
			//tree.setDomain("Domain" + i % 3);
			storage.store(tree);
		}

		((HdfsMessageStorage) storage).dispose();
	}
}
