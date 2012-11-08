package com.dianping.cat.hadoop.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucket;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.storage.dump.MessageBucket;
import com.dianping.cat.storage.dump.MessageBucketManager;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(FileSystemManager.class) //
		      .req(ServerConfigManager.class));

		all.add(C(MessageBucket.class, HdfsMessageBucket.ID, HdfsMessageBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(FileSystemManager.class) //
		      .req(MessageCodec.class, PlainTextMessageCodec.ID));
		all.add(C(MessageBucketManager.class, HdfsMessageBucketManager.ID, HdfsMessageBucketManager.class) //
		      .req(FileSystemManager.class, ServerConfigManager.class) //
		      .req(MessagePathBuilder.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
