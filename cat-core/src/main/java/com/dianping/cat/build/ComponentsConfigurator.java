package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.internal.DefaultMessageManager;
import com.dianping.cat.message.internal.DefaultMessageProducer;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.io.DefaultTransportManager;
import com.dianping.cat.message.io.InMemoryQueue;
import com.dianping.cat.message.io.InMemoryReceiver;
import com.dianping.cat.message.io.InMemorySender;
import com.dianping.cat.message.io.MessageReceiver;
import com.dianping.cat.message.io.MessageSender;
import com.dianping.cat.message.io.TcpSocketReceiver;
import com.dianping.cat.message.io.TcpSocketSender;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageConsumerRegistry;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageStorage;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.BufferWriter;
import com.dianping.cat.message.spi.codec.EscapingBufferWriter;
import com.dianping.cat.message.spi.codec.HtmlEncodingBufferWriter;
import com.dianping.cat.message.spi.codec.HtmlMessageCodec;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.consumer.DummyConsumer;
import com.dianping.cat.message.spi.consumer.DumpToHtmlConsumer;
import com.dianping.cat.message.spi.internal.DefaultMessageConsumerRegistry;
import com.dianping.cat.message.spi.internal.DefaultMessageHandler;
import com.dianping.cat.message.spi.internal.DefaultMessagePathBuilder;
import com.dianping.cat.message.spi.internal.DefaultMessageStorage;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketFactory;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.internal.DefaultBucket;
import com.dianping.cat.storage.internal.DefaultBucketFactory;
import com.dianping.cat.storage.internal.DefaultBucketManager;
import com.dianping.cat.storage.internal.DefaultMessageBucket;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(InMemoryQueue.class));
		all.add(C(MessageSender.class, "in-memory", InMemorySender.class) //
		      .req(InMemoryQueue.class));
		all.add(C(MessageReceiver.class, "in-memory", InMemoryReceiver.class) //
		      .req(InMemoryQueue.class));

		all.add(C(MessageManager.class, DefaultMessageManager.class));
		all.add(C(MessageProducer.class, DefaultMessageProducer.class) //
		      .req(MessageManager.class));
		all.add(C(MessageIdFactory.class) //
		      .req(MessageManager.class));
		all.add(C(MessagePathBuilder.class, DefaultMessagePathBuilder.class) //
		      .req(MessageManager.class, MessageIdFactory.class));
		all.add(C(MessageStorage.class, "html", DefaultMessageStorage.class) //
		      .req(MessagePathBuilder.class) //
		      .req(MessageCodec.class, "html"));

		all.add(C(BufferWriter.class, "escape", EscapingBufferWriter.class));
		all.add(C(BufferWriter.class, "html-encode", HtmlEncodingBufferWriter.class));

		all.add(C(MessageCodec.class, "plain-text", PlainTextMessageCodec.class) //
		      .req(BufferWriter.class, "escape"));
		all.add(C(MessageCodec.class, "html", HtmlMessageCodec.class) //
		      .req(MessagePathBuilder.class) //
		      .req(BufferWriter.class, "html-encode"));

		all.add(C(MessageConsumer.class, DummyConsumer.ID, DummyConsumer.class));
		all.add(C(MessageConsumer.class, DumpToHtmlConsumer.ID, DumpToHtmlConsumer.class) //
		      .req(MessageStorage.class, "html") //
		      .req(MessagePathBuilder.class));
		all.add(C(MessageConsumerRegistry.class, DefaultMessageConsumerRegistry.class) //
		      .req(MessageConsumer.class, new String[] { DummyConsumer.ID }, "m_consumers"));

		all.add(C(MessageSender.class, "tcp-socket", TcpSocketSender.class) //
		      .is(PER_LOOKUP) //
		      .req(MessageCodec.class, "plain-text"));
		all.add(C(MessageReceiver.class, "tcp-socket", TcpSocketReceiver.class) //
		      .is(PER_LOOKUP) //
		      .req(MessageCodec.class, "plain-text"));
		all.add(C(TransportManager.class, DefaultTransportManager.class) //
		      .req(MessageManager.class));

		all.add(C(MessageHandler.class, DefaultMessageHandler.class) //
		      .req(MessageManager.class, MessageConsumerRegistry.class));

		all.add(C(Bucket.class, String.class.getName(), DefaultBucket.class));
		all.add(C(Bucket.class, byte[].class.getName(), DefaultBucket.class));
		all.add(C(Bucket.class, MessageTree.class.getName(), DefaultMessageBucket.class) //
		      .req(MessageCodec.class, "plain-text"));
		all.add(C(BucketManager.class, DefaultBucketManager.class));
		all.add(C(BucketFactory.class, DefaultBucketFactory.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
