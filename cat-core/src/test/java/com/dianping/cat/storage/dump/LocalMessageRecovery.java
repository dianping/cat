package com.dianping.cat.storage.dump;

import java.io.File;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class LocalMessageRecovery extends ComponentTestCase {

	private MessageCodec m_codec;

	@Test
	public void test()throws Exception{
		recovery("10.1.6.108");
	}
	
	public void recovery(String ip) throws Exception {
		m_codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
		File dataFile = new File("/Users/youyong/midasMerchantServerWorker-10.1.8.77-"+ip);
		MessageBlockReader reader = new MessageBlockReader(dataFile);
		String message = readMessage(reader, 27);
		
		String id="midasMerchantServerWorker-0a01084d-382328-27";
		if(message.indexOf(id)>-1){
			System.out.println(message);
		}
	}

	private String readMessage(MessageBlockReader reader, int index) throws Exception {
		byte[] data = reader.readMessage(index);
		
		if(data==null){
			return "";
		}
		
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(data.length);
		MessageTree tree = new DefaultMessageTree();

		buf.writeBytes(data);
		m_codec.decode(buf, tree);

		return tree.toString();
	}
}
