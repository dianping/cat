package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.internal.DefaultBucket;
import com.dianping.cat.storage.internal.DefaultBucketManager;
import com.dianping.cat.storage.internal.DefaultMessageBucket;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

class StorageComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(Bucket.class, String.class.getName(), DefaultBucket.class) //
		      .is(PER_LOOKUP));
		all.add(C(Bucket.class, byte[].class.getName(), DefaultBucket.class) //
		      .is(PER_LOOKUP));
		all.add(C(Bucket.class, MessageTree.class.getName(), DefaultMessageBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(MessageCodec.class, "plain-text"));
		all.add(C(BucketManager.class, DefaultBucketManager.class) //
		      .config(E("baseDir").value("target/bucket/")));

		return all;
	}
}
