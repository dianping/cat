package com.dianping.cat.hadoop.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucket;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.hadoop.hdfs.HdfsUploader;
import com.dianping.cat.message.PathBuilder;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.storage.MessageBucket;
import com.dianping.cat.message.storage.MessageBucketManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(FileSystemManager.class) //
		      .req(ServerConfigManager.class));

		all.add(C(HdfsUploader.class) //
		      .req(FileSystemManager.class, ServerConfigManager.class));

		all.add(C(MessageBucket.class, HdfsMessageBucket.ID, HdfsMessageBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(FileSystemManager.class) //
		      .req(MessageCodec.class, PlainTextMessageCodec.ID));
		all.add(C(MessageBucketManager.class, HdfsMessageBucketManager.ID, HdfsMessageBucketManager.class) //
		      .req(FileSystemManager.class, ServerConfigManager.class) //
		      .req(PathBuilder.class));

		return all;
	}
}
