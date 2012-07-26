package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.DefaultBucketManager;
import com.dianping.cat.storage.message.LocalLogviewBucket;
import com.dianping.cat.storage.message.LocalMessageBucket;
import com.dianping.cat.storage.report.LocalReportBucket;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

class StorageComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(BucketManager.class, DefaultBucketManager.class) //
		      .req(MessagePathBuilder.class));

		all.add(C(Bucket.class, MessageTree.class.getName() + "-logview", LocalLogviewBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(ServerConfigManager.class, MessagePathBuilder.class) //
		      .req(MessageCodec.class, PlainTextMessageCodec.ID));
		all.add(C(Bucket.class, MessageTree.class.getName() + "-message", LocalMessageBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(ServerConfigManager.class, MessagePathBuilder.class) //
		      .req(MessageCodec.class, PlainTextMessageCodec.ID));
		all.add(C(Bucket.class, String.class.getName() + "-report", LocalReportBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(ServerConfigManager.class, MessagePathBuilder.class));

		return all;
	}
}
