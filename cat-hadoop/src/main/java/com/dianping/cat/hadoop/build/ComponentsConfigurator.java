package com.dianping.cat.hadoop.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.hadoop.DumpToHdfsConsumer;
import com.dianping.cat.hadoop.dal.LogviewDao;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.hdfs.DefaultInputChannel;
import com.dianping.cat.hadoop.hdfs.DefaultInputChannelManager;
import com.dianping.cat.hadoop.hdfs.DefaultOutputChannel;
import com.dianping.cat.hadoop.hdfs.DefaultOutputChannelManager;
import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.dianping.cat.hadoop.hdfs.HdfsMessageStorage;
import com.dianping.cat.hadoop.hdfs.InputChannel;
import com.dianping.cat.hadoop.hdfs.InputChannelManager;
import com.dianping.cat.hadoop.hdfs.OutputChannel;
import com.dianping.cat.hadoop.hdfs.OutputChannelManager;
import com.dianping.cat.hadoop.storage.RemoteMessageBucket;
import com.dianping.cat.hadoop.storage.RemoteStringBucket;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageStorage;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(FileSystemManager.class) //
		      .req(ServerConfigManager.class));

		all.add(C(OutputChannel.class, DefaultOutputChannel.class).is(PER_LOOKUP) //
		      .req(MessageCodec.class, "plain-text"));
		all.add(C(OutputChannelManager.class, DefaultOutputChannelManager.class) //
		      .req(FileSystemManager.class));
		all.add(C(InputChannel.class, DefaultInputChannel.class).is(PER_LOOKUP) //
		      .req(MessageCodec.class, "plain-text"));
		all.add(C(InputChannelManager.class, DefaultInputChannelManager.class) //
		      .req(FileSystemManager.class));

		all.add(C(MessageStorage.class, "hdfs", HdfsMessageStorage.class) //
		      .req(OutputChannelManager.class) //
		      .req(MessagePathBuilder.class));
		all.add(C(MessageConsumer.class, DumpToHdfsConsumer.ID, DumpToHdfsConsumer.class) //
		      .req(MessageStorage.class, "hdfs"));

		all.add(C(Bucket.class, String.class.getName() + "-remote", RemoteStringBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(ReportDao.class));
		all.add(C(Bucket.class, MessageTree.class.getName() + "-remote", RemoteMessageBucket.class) //
		      .is(PER_LOOKUP) //
		      .req(OutputChannelManager.class, InputChannelManager.class) //
		      .req(LogviewDao.class, MessagePathBuilder.class));

		all.addAll(new DatabaseConfigurator().defineComponents());

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
