package com.dianping.cat.message.spi.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.message.CatTestCase;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.MockMessageBuilder;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class MessageCodecUnexceptedCharTest extends CatTestCase {
	public static final String ID = "plain-text";

	@Test
	public void testCodePerformance() throws Exception {
		MessageCodec codec = lookup(MessageCodec.class, ID);
		MessageTree tree = buildMessage();
		ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10240);
		
		codec.encode(tree, buf);
		MessageTree result = new DefaultMessageTree();
		codec.decode(buf, result);
		Assert.assertEquals(tree.toString(), result.toString());
	}

	public MessageTree buildMessage() {
		Message message = new MockMessageBuilder() {
			@Override
			public MessageHolder define() {
				TransactionHolder t = t("\n\nWEBC$$$$LUSTER\t\n", "GET\t\n",
				      "This&123123&1231&3&\n\n\n\n&\t\t\t&&&&\n\n\n\n\n\n is test data\t\t\n\n", 112819) //
				      .at(1348374838231L) //
				      .after(1300).child(t("QUICKIESERVICE", "gimme_stuff", 1571)) //
				      .after(100).child(e("SERVICE", "event1", "This\n\n\n\n\n\n is test data\t\t\n\n")) //
				      .after(100).child(h("SERVICE", "heartbeat1")) //
				      .after(100).child(t("WEB SERVER", "GET", 109358) //
				            .after(1000).child(t("SOME SERVICE", "get", 4345) //
				                  .after(4000).child(t("MEMCACHED", "Get", 279))) //
				            .mark().after(200).child(t("MEMCACHED", "Inc", 319)) //
				            .reset().after(500).child(t("BIG ASS SERVICE", "getThemDatar", 97155) //
				                  .after(1000).mark().child(t("SERVICE", "getStuff", 3760)) //
				                  .reset().child(t("DATAR", "findThings", 94537)) //
				                  .after(200).child(t("THINGIE", "getMoar", 1435)) //
				            ) //
				            .after(100).mark().child(t("OTHER DATA SERVICE", "get", 4394) //
				                  .after(1000).mark().child(t("MEMCACHED", "Get", 378)) //
				                  .reset().child(t("MEMCACHED", "Get", 3496)) //
				            ) //
				            .reset().child(t("FINAL DATA SERVICE", "get", 4394) //
				                  .after(1000).mark().child(t("MEMCACHED", "Get", 386)) //
				                  .reset().child(t("MEMCACHED", "Get", 322)) //
				                  .reset().child(t("MEMCACHED", "Get", 322)) //
				            ) //
				      ) //
				;

				return t;
			}
		}.build();

		MessageTree tree = new DefaultMessageTree();
		tree.setDomain("cat");
		tree.setHostName("test");
		tree.setIpAddress("test");
		tree.setThreadGroupName("test");
		tree.setThreadId("test");
		tree.setThreadName("test");
		tree.setMessage(message);
		return tree;
	}
}
