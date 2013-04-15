package com.dianping.cat.storage.dump;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.EscapingBufferWriter;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@RunWith(JUnit4.class)
@Ignore
public class LocalMessageBucketTest extends ComponentTestCase {
	@BeforeClass
	public static void beforeClass() {
		String[] files = { "dump", "dump-0", "dump-1", "dump-2" };

		for (String file : files) {
			new File("target/bucket/hdfs/dump/" + file).delete();
			new File("target/bucket/hdfs/dump/" + file + ".idx").delete();
		}
	}

	@Test
	public void testReadWrite() throws Exception {
		MessageIdFactory factory = new MockMessageIdFactory();
		LocalMessageBucket bucket = createBucket(factory, "");

		DefaultMessageTree tree = new DefaultMessageTree();

		PlainTextMessageCodec codec = new PlainTextMessageCodec();
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer();

		codec.setBufferWriter(new EscapingBufferWriter());
		int count = 2000;

		for (int i = 0; i < count; i++) {
			MessageId id = MessageId.parse(tree.getMessageId());
	
			tree.setMessageId(factory.getNextId());

			codec.encode(tree, buf);
			bucket.storeMessage(buf,id);
		}

		for (int i = 0; i < count; i++) {
			MessageTree t = bucket.findByIndex(i);
			int index = MessageId.parse(t.getMessageId()).getIndex();

			Assert.assertEquals(i, index);
		}

		bucket.close();
		bucket.archive();
	}

	@Test
	public void testManyReadWrite() throws Exception {
		MessageIdFactory factory = new MockMessageIdFactory();
		LocalMessageBucket[] buckets = new LocalMessageBucket[3];
		PlainTextMessageCodec codec = new PlainTextMessageCodec();

		for (int i = 0; i < buckets.length; i++) {
			LocalMessageBucket bucket = createBucket(factory, "-" + i);

			buckets[i] = bucket;
		}

		DefaultMessageTree tree = new DefaultMessageTree();
		int count = 2000;

		for (int i = 0; i < count; i++) {
			MessageId id = MessageId.parse(tree.getMessageId());
			
			tree.setMessageId(factory.getNextId());
			ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
			codec.encode(tree,buf);
			buckets[i % buckets.length].storeMessage(buf,id);
		}

		for (int i = 0; i < count; i++) {
			MessageTree t = buckets[i % buckets.length].findByIndex(i);

			Assert.assertNotNull("Can't find message tree(" + i + ").", t);

			int index = MessageId.parse(t.getMessageId()).getIndex();

			Assert.assertEquals(i, index);
		}

		for (int i = 0; i < buckets.length; i++) {
			buckets[i].close();
		}
	}

	private LocalMessageBucket createBucket(MessageIdFactory factory, String id) throws Exception, IOException {
		LocalMessageBucket bucket = (LocalMessageBucket) lookup(MessageBucket.class, LocalMessageBucket.ID);

		bucket.setMessageCodec(MockCodec.INSTANCE);
		bucket.setBaseDir(new File("target/bucket/hdfs/dump"));
		bucket.initialize("dump" + id);
		factory.setIpAddress("7f000001");
		factory.initialize("Test");
		return bucket;
	}

	static enum MockCodec implements MessageCodec {
		INSTANCE;

		@Override
		public MessageTree decode(ChannelBuffer buf) {
			DefaultMessageTree tree = new DefaultMessageTree();

			decode(buf, tree);
			return tree;
		}

		@Override
		public void decode(ChannelBuffer buf, MessageTree tree) {
			int length = buf.readInt();
			byte[] bytes = new byte[length];

			buf.readBytes(bytes);
			tree.setMessageId(new String(bytes));

			int size = buf.readInt();
			byte[] data = new byte[size];

			buf.readBytes(data);
		}

		@Override
		public void encode(MessageTree tree, ChannelBuffer buf) {
			byte[] bytes = tree.getMessageId().getBytes();

			buf.writeInt(0); // placeholder
			buf.writeInt(bytes.length);
			buf.writeBytes(bytes);

			Random random = new Random();

			int size = random.nextInt(4096);
			byte[] data = new byte[size];

			for (int i = 0; i < size; i++) {
				data[i] = (byte) (i * size + 1);
			}

			// random.nextBytes(data);
			buf.writeInt(data.length);
			buf.writeBytes(data);
		}
	}

	static class MockMessageIdFactory extends MessageIdFactory {
		@Override
		protected long getTimestamp() {
			return 1343532130488L;
		}
	}
}
