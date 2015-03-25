package com.dianping.cat.storage.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.message.storage.MessageBlock;
import com.dianping.cat.message.storage.MessageBlockReader;
import com.dianping.cat.message.storage.MessageBlockWriter;

public class MessageBlockTest {

	private String baseDir = "target/bucket/hdfs/dump/test";

	@Test
	public void testReadAndWrite() throws IOException {
		File dataFile = new File(baseDir);
		
		dataFile.delete();

		MessageBlockWriter write = new MessageBlockWriter(dataFile);
		MessageBlockReader reader = new MessageBlockReader(dataFile);

		String data1 = "This is test data1";

		ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
		DataOutputStream out = new DataOutputStream(new GZIPOutputStream(baos));

		out.writeInt(data1.getBytes().length);
		out.writeBytes(data1);
		out.close();

		byte[] results = baos.toByteArray();

		MessageBlock block = new MessageBlock(baseDir);
		block.setData(results);
		block.addIndex(1, 0);
		
		write.writeBlock(block);

		Assert.assertEquals(data1, new String(reader.readMessage(1)));
	}
}
