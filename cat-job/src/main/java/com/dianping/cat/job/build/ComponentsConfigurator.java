package com.dianping.cat.job.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.job.DumpToHdfsConsumer;
import com.dianping.cat.job.hdfs.DefaultInputChannel;
import com.dianping.cat.job.hdfs.DefaultInputChannelManager;
import com.dianping.cat.job.hdfs.DefaultOutputChannel;
import com.dianping.cat.job.hdfs.DefaultOutputChannelManager;
import com.dianping.cat.job.hdfs.HdfsMessageStorage;
import com.dianping.cat.job.hdfs.InputChannel;
import com.dianping.cat.job.hdfs.InputChannelManager;
import com.dianping.cat.job.hdfs.OutputChannel;
import com.dianping.cat.job.hdfs.OutputChannelManager;
import com.dianping.cat.job.sql.dal.LogviewDao;
import com.dianping.cat.job.sql.dal.ReportDao;
import com.dianping.cat.job.storage.RemoteMessageBucket;
import com.dianping.cat.job.storage.RemoteStringBucket;
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

		if (isEnv("dev") || property("env", null) == null) {
			all.add(C(OutputChannel.class, DefaultOutputChannel.class).is(PER_LOOKUP) //
			      .req(MessageCodec.class, "plain-text") //
			      .config(E("maxSize").value(String.valueOf(2 * 1024 * 1024L))));
			all.add(C(OutputChannelManager.class, DefaultOutputChannelManager.class) //
			      .req(MessagePathBuilder.class));
			all.add(C(InputChannel.class, DefaultInputChannel.class).is(PER_LOOKUP) //
			      .req(MessageCodec.class, "plain-text"));
			all.add(C(InputChannelManager.class, DefaultInputChannelManager.class));
		} else {
			all.add(C(OutputChannel.class, DefaultOutputChannel.class).is(PER_LOOKUP) //
			      .req(MessageCodec.class, "plain-text") //
			      .config(E("maxSize").value(String.valueOf(128 * 1024 * 1024L))));
			all.add(C(OutputChannelManager.class, DefaultOutputChannelManager.class) //
			      .req(MessagePathBuilder.class) //
			      .config(E("baseDir").value("data"), //
			            E("serverUri").value("hdfs://192.168.7.43:9000/user/cat/")));
			all.add(C(InputChannel.class, DefaultInputChannel.class).is(PER_LOOKUP) //
			      .req(MessageCodec.class, "plain-text"));
			all.add(C(InputChannelManager.class, DefaultInputChannelManager.class) //
			      .config(E("serverUri").value("hdfs://192.168.7.43:9000/user/cat/")));
		}

		all.add(C(MessageStorage.class, "hdfs", HdfsMessageStorage.class) //
		      .req(OutputChannelManager.class));
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
