package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.PathBuilder;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.storage.LocalMessageBucket;
import com.dianping.cat.message.storage.MessageBucket;
import com.dianping.cat.report.DefaultReportBucketManager;
import com.dianping.cat.report.LocalReportBucket;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;

class StorageComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ReportBucketManager.class, DefaultReportBucketManager.class).req(ServerConfigManager.class));

		all.add(C(ReportBucket.class, LocalReportBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(ServerConfigManager.class, PathBuilder.class));

		all.add(C(MessageBucket.class, LocalMessageBucket.ID, LocalMessageBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(MessageCodec.class, PlainTextMessageCodec.ID));

		return all;
	}
}
