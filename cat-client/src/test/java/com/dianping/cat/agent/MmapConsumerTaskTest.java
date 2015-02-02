package com.dianping.cat.agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.internal.MessageIdFactory;

public class MmapConsumerTaskTest extends ComponentTestCase {
	private void createMessage(MessageIdFactory factory, StringBuilder sb, int i) {
		String status = (i % 7 == 0) || (i % 11 == 0) ? "50" + (i % 3) : "200";
		long t0 = System.currentTimeMillis();

		// <id>\t<parent-id>\t<root-id>\n
		sb.append(String.format("%s\t%s\t%s\n", factory.getNextId(), factory.getNextId(), factory.getNextId()));

		// <name>\t<status>\t<url>\t<request-header-len>\t<upstream-url>\t<response-header-len>\t<response-body-len>\t<response-body-blocks>\t<t0>\t<t1>\t<t2>\t<3>\t<t4>\n
		sb.append(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", //
		      "NginxTest", status, "http://url/here/" + i, i % 10, "http://upstream/url/here/" + (i % 3), //
		      i % 9, i % 8, i % 7, t0, t0 + i, t0 + 3 * i, t0 + 4 * i, t0 + 5 * i));

		// \n
		sb.append("\n");
	}

	@Test
	public void generateDataFile() throws Exception {
		File idx = new File("/data/appdatas/cat/mmap.idx");
		File dat = new File("/data/appdatas/cat/mmap.dat");

		MessageIdFactory factory = lookup(MessageIdFactory.class);
		StringBuilder sb = new StringBuilder(8192);

		factory.initialize("cat");

		for (int i = 0; i < 100; i++) {
			createMessage(factory, sb, i);
		}

		FileWriter datWriter = new FileWriter(dat);

		datWriter.write(sb.toString());
		datWriter.close();

		updateMmapIndex(idx, dat.length(), dat.length(), 0);
	}

	private void updateMmapIndex(File idx, long capacity, long writerIndex, long readerIndex) throws FileNotFoundException,
	      IOException {
		RandomAccessFile raf = new RandomAccessFile(idx, "rw");
		MappedByteBuffer buffer = raf.getChannel().map(MapMode.READ_WRITE, 0, 24);

		buffer.order(ByteOrder.LITTLE_ENDIAN);

		if (capacity > 0) {
			buffer.putLong(0, capacity);
		}

		if (writerIndex >= 0) {
			buffer.putLong(8, writerIndex);
		}

		if (readerIndex >= 0) {
			buffer.putLong(16, readerIndex);
		}

		buffer.force();
		raf.close();
	}

	@Test
	public void updateWriterIndex() throws Exception {
		File idx = new File("/data/appdatas/cat/mmap.idx");
		MessageIdFactory factory = lookup(MessageIdFactory.class);
		StringBuilder sb = new StringBuilder(8192);

		factory.initialize("cat");

		for (int i = 0; i < 50; i++) {
			createMessage(factory, sb, i);
		}

		updateMmapIndex(idx, -1, sb.length(), -1);
	}
}
