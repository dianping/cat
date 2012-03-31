package com.dianping.cat.storage.message;

import java.io.IOException;
import java.util.Date;

import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.MesageTreeBucketTestCase;

public class LocalLogviewBucketTest extends MesageTreeBucketTestCase {

	@SuppressWarnings("unchecked")
	protected Bucket<MessageTree> createBucket() throws Exception, IOException {
		Bucket<MessageTree> bucket = lookup(Bucket.class, MessageTree.class.getName() + "-logview");
		bucket.initialize(null, "cat", new Date());
		return bucket;
	}
}
