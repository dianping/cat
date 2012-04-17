package com.dianping.cat.consumer.logview;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class LogviewUploaderTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		LogviewUploader uploader = lookup(LogviewUploader.class);
		BucketManager manager = lookup(BucketManager.class);
		long timestamp = 1334122638154L; // [04-11 13:37:18.154]
		String domain = "test";

		Bucket<MessageTree> bucket = manager.getLogviewBucket(timestamp, domain);

		for (int i = 0; i < 1000; i++) {
			DefaultMessageTree tree = newMessageTree(i, timestamp);

			bucket.storeById(tree.getMessageId(), tree);
		}

		bucket.flush();
		uploader.addBucket(timestamp, domain);
		
	}

	private DefaultMessageTree newMessageTree(int i, long timestamp) {
		DefaultMessageTree tree = new DefaultMessageTree();

		tree.setDomain("domain");
		tree.setHostName("hostName" + i);
		tree.setIpAddress("ipAddress" + i);
		tree.setMessageId(String.valueOf(i));
		tree.setParentMessageId("parentMessageId" + i);
		tree.setRootMessageId("rootMessageId" + i);
		tree.setSessionToken("sessionToken");
		tree.setThreadGroupName("threadGroupName");
		tree.setThreadId("threadId" + i);
		tree.setThreadName("threadName");

		tree.setMessage(newTransaction("type", "name" + i, timestamp, "0", 123456 + i, "data" + i));
		return tree;
	}

	private Transaction newTransaction(String type, String name, long timestamp, String status, int duration, String data) {
		DefaultTransaction transaction = new DefaultTransaction(type, name, null);

		transaction.setStatus(status);
		transaction.addData(data);
		transaction.complete();
		transaction.setTimestamp(timestamp);
		transaction.setDurationInMillis(duration);
		return transaction;
	}
}
