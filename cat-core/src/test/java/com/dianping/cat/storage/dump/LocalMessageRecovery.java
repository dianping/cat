package com.dianping.cat.storage.dump;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

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
		recovery("10.1.6.126");
		recovery("10.1.6.108");
		recovery("10.1.6.145");
	}
	
	public void recovery(String ip) throws Exception {
		m_codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
		File dataFile = new File("/Users/youyong/PayOrder-10.1.7.123-"+ip);
		MessageBlockReader reader = new MessageBlockReader(dataFile);
		File writeFile = new File("/Users/youyong/result");
		OutputStream out = new FileOutputStream(writeFile);

		String target =null;
		for (int i = 0; i < 1000000; i++) {

			try {
				String message = readMessage(reader, i);
				if(message.indexOf("mid=")>-1){
					target = message;
					
					System.out.println(target);
				}
			} catch(EOFException eof){
				break;
			}
			catch (Exception e) {
				System.out.println(e);
			}
		}
		
		System.out.println(target);
		out.close();
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
