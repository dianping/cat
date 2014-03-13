package com.dianping.cat.storage.dump;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@RunWith(JUnit4.class)
public class LocalMessageBucketTest extends ComponentTestCase {

	private final String m_baseDir = "target/bucket/hdfs/dump/";

	public void setup() {
		String[] files = { "dump", "dump-0", "dump-1", "dump-2", "outbox/dump" };

		for (String file : files) {
			new File(m_baseDir + file).delete();
			new File(m_baseDir + file + ".idx").delete();
		}

		String tmpDir = System.getProperty("java.io.tmpdir");
		new File(tmpDir, "cat-Test.mark").delete();
	}

	@Test
	public void testReadWrite() throws Exception {
		setup();
		MessageIdFactory factory = new MockMessageIdFactory();
		LocalMessageBucket bucket = createBucket(factory, "");
		MessageCodec codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);

		int count = 2000;
		int i = 0;
		MessageBlock block = null;
		MessageTree tree = new DefaultMessageTree();

		for (i = 0; i < count; i++) {
			ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
			MessageId id = buildChannelBuffer(factory, codec, tree, buf);

			block = bucket.storeMessage(buf, id);

			if (block != null) {
				bucket.getWriter().writeBlock(block);
				break;
			}
		}

		for (int j = 0; j < i; j++) {
			MessageTree t = bucket.findByIndex(j);
			int index = MessageId.parse(t.getMessageId()).getIndex();

			Assert.assertEquals(j, index);
		}

		bucket.close();

		testArchive(bucket);
	}

	private void testArchive(LocalMessageBucket bucket) throws IOException {
		File from = new File(m_baseDir, "dump");
		File fromIdx = new File(m_baseDir, "dump.idx");

		Assert.assertEquals(true, from.exists());
		Assert.assertEquals(true, fromIdx.exists());

		bucket.archive();

		Assert.assertEquals(false, from.exists());
		Assert.assertEquals(false, fromIdx.exists());

		File outbox = new File(m_baseDir, "outbox" + File.separator + "dump");
		File oubboxIdx = new File(m_baseDir, "outbox" + File.separator + "dump.idx");

		Assert.assertEquals(true, outbox.exists());
		Assert.assertEquals(true, oubboxIdx.exists());

	}

	private MessageId buildChannelBuffer(MessageIdFactory factory, MessageCodec codec, MessageTree tree,
	      ChannelBuffer buf) {
		String messageId = factory.getNextId();

		tree.setMessageId(messageId);
		MessageId id = MessageId.parse(messageId);

		codec.encode(tree, buf);

		return id;
	}

	@Test
	public void testManyReadWrite() throws Exception {
		setup();

		MessageIdFactory factory = new MockMessageIdFactory();
		LocalMessageBucket[] buckets = new LocalMessageBucket[3];
		MessageCodec codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);

		for (int i = 0; i < buckets.length; i++) {
			LocalMessageBucket bucket = createBucket(factory, "-" + i);

			buckets[i] = bucket;
		}

		MessageTree tree = new DefaultMessageTree();
		int count = 3000;
		MessageBlock block = null;
		Set<Integer> fullBucket = new HashSet<Integer>();
		Map<Integer, Integer> maxIdForBucket = new HashMap<Integer, Integer>();

		for (int i = 0; i < count; i++) {
			ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
			MessageId id = buildChannelBuffer(factory, codec, tree, buf);

			int pos = i % buckets.length;

			if (!fullBucket.contains(pos)) {
				block = buckets[pos].storeMessage(buf, id);
			}

			if (block != null && !fullBucket.contains(pos)) {
				buckets[pos].getWriter().writeBlock(block);
				fullBucket.add(pos);
				maxIdForBucket.put(pos, i);
			}
		}

		for (int i = 0; i < count; i++) {
			int pos = i % buckets.length;
			if (i <= maxIdForBucket.get(pos)) {
				MessageTree t = buckets[pos].findByIndex(i);

				int index = MessageId.parse(t.getMessageId()).getIndex();

				Assert.assertEquals(i, index);
			}
		}

		for (int i = 0; i < buckets.length; i++) {
			buckets[i].close();
		}
	}

	private LocalMessageBucket createBucket(MessageIdFactory factory, String id) throws Exception, IOException {
		LocalMessageBucket bucket = (LocalMessageBucket) lookup(MessageBucket.class, LocalMessageBucket.ID);

		bucket.setBaseDir(new File(m_baseDir));
		bucket.initialize("dump" + id);
		factory.setIpAddress("7f000001");
		factory.initialize("Test");
		return bucket;
	}

	static class MockMessageIdFactory extends MessageIdFactory {
		@Override
		protected long getTimestamp() {
			return 1343532130488L;
		}
	}
}
