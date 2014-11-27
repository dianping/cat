package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.spi.core.MessagePathBuilder;
import com.dianping.cat.storage.report.ReportBucket;
import com.dianping.cat.storage.report.ReportBucketManager;
import com.dianping.cat.storage.report.DefaultReportBucketManager;
import com.dianping.cat.storage.report.LocalReportBucket;

class StorageComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ReportBucketManager.class, DefaultReportBucketManager.class));

		all.add(C(ReportBucket.class, String.class.getName() + "-report", LocalReportBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(ServerConfigManager.class, MessagePathBuilder.class));

		return all;
	}
}
