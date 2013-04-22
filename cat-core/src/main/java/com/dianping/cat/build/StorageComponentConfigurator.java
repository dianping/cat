package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.DefaultBucketManager;
import com.dianping.cat.storage.report.LocalReportBucket;

class StorageComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(BucketManager.class, DefaultBucketManager.class) //
		      .req(MessagePathBuilder.class));

		all.add(C(Bucket.class, String.class.getName() + "-report", LocalReportBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(ServerConfigManager.class, MessagePathBuilder.class));

		return all;
	}
}
