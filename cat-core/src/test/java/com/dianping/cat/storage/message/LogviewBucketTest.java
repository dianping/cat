package com.dianping.cat.storage.message;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.message.LocalLogviewBucket.Meta;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class LogviewBucketTest extends ComponentTestCase {
	@Test
	public void testMeta() throws Exception {
		MessageCodec codec = lookup(MessageCodec.class, "plain-text");
		LocalLogviewBucket logview = (LocalLogviewBucket) lookup(Bucket.class, MessageTree.class.getName() + "-logview");

		logview.initialize(null, "meta-test", new Date());

		List<String> ids = new ArrayList<String>();
		final MessageIdFactory factory = new MessageIdFactory();

		factory.setDomain("Test");
		factory.setIpAddress("ip");

		for (int i = 0; i < 10; i++) {
			DefaultMessageTree tree = new DefaultMessageTree();
			tree.setMessageId(factory.getNextId());
			String id = tree.getMessageId();

			tree.setMessage(new DefaultTransaction("type", "name", null));
			logview.storeById(id, tree);
			ids.add(id);
		}

		logview.flush();

		RandomAccessFile in = new RandomAccessFile(new File(logview.getBaseDir(), logview.getLogicalPath()), "r");

		for (String id : ids) {
			Meta meta = logview.getMeta(id);

			MessageTree tree = read(in, codec, meta.getOffset(), meta.getLegnth());

			Assert.assertNotNull(tree.getMessage());
		}

		in.close();
	}

	private MessageTree read(RandomAccessFile in, MessageCodec codec, long offset, int length) throws IOException {
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);
		byte[] data = new byte[length];

		in.seek(offset);

		int size = in.read(data);

		buf.writeBytes(data, 0, size);

		MessageTree tree = codec.decode(buf);
		return tree;
	}
}
