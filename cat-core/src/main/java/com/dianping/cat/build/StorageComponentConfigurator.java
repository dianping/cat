package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.internal.DefaultBucketManager;
import com.dianping.cat.storage.internal.LocalMessageBucket;
import com.dianping.cat.storage.internal.LocalStringBucket;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

class StorageComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(Bucket.class, String.class.getName(), LocalStringBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(MessagePathBuilder.class));
		all.add(C(Bucket.class, MessageTree.class.getName(), LocalMessageBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(MessageCodec.class, "plain-text"));
		all.add(C(BucketManager.class, DefaultBucketManager.class));

		return all;
	}
}
