package com.dianping.cat.message.spi.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class PlainTextPerformanceCodecTest {

	private int count = 100000;

	@Test
	public void test() throws InterruptedException {
		MessageTree tree = buildMessages();

		PlainTextMessageCodec codec = new PlainTextMessageCodec();
		ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8192);
		codec.encode(tree, buf);

		buf.readInt();
		MessageTree tree2 = new DefaultMessageTree();
		codec.decode(buf, tree2);

		Thread.sleep(1000);
	}

	@Test
	public void testMany() throws InterruptedException {
		MessageTree tree = buildMessages();

		PlainTextMessageCodec codec = new PlainTextMessageCodec();
		ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8192);
		codec.encode(tree, buf);

		buf.readInt();
		buf.markReaderIndex();

		long current = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			MessageTree tree2 = new DefaultMessageTree();
			codec.decode(buf, tree2);
			buf.resetReaderIndex();
		}
		System.out.println("Cost:" + (System.currentTimeMillis() - current));

		Thread.sleep(1000);
	}

	@Test
	public void testManyOld() throws InterruptedException {
		MessageTree tree = buildMessages();
		PlainTextMessageCodec codec = new PlainTextMessageCodec();
		ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8192);

		codec.encode(tree, buf);

		buf.readInt();
		buf.markReaderIndex();

		long current = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			MessageTree tree2 = new DefaultMessageTree();
			codec.decode(buf, tree2);
			buf.resetReaderIndex();
		}
		System.out.println("Cost:" + (System.currentTimeMillis() - current));

		Thread.sleep(1000);
	}

	private MessageTree buildMessages() {
		Transaction t = Cat.newTransaction("type1", "name1\t\n\t\n\\");
		Transaction t2 = Cat.newTransaction("type2", "name\t\n\t\n2\\");
		Transaction t3 = Cat.newTransaction("type3", "name3\t\n\t\n\\");
		Transaction t4 = Cat.newTransaction("type4", "name4\t\n\t\n\\");
		Transaction t5 = Cat.newTransaction("type4", "name4\t\n\t\n\\");
		Transaction t6 = Cat.newTransaction("type4", "name4\t\n\t\n\\");
		Transaction t7 = Cat.newTransaction("type4", "name4\t\n\t\n\\");
		Transaction t8 = Cat.newTransaction("type4", "name4\t\n\t\n\\");

		Cat.logEvent("type1\t\n", "name\t\n", "sdfsdf\t\n", convertException(new NullPointerException()));
		Cat.logHeartbeat("type1\t\n", "name\t\n", "sdfsdf\t\n", convertException(new NullPointerException()));

		Cat.logError(new RuntimeException());

		for (int i = 0; i < 50; i++) {
			Cat.logEvent("type1\t\n", "name\t\n", "sdfsdf\t\n", "");
		}
		for (int i = 0; i < 10; i++) {
			Cat.logError(new RuntimeException());
		}

		t5.complete();
		t6.complete();
		t7.complete();
		t8.complete();
		t2.addData(convertException(new NullPointerException()));
		t2.setStatus(convertException(new NullPointerException()));
		t4.complete();
		t3.complete();
		t2.complete();
		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

		t.setStatus("sfsf\t\n");
		((DefaultTransaction) t).setDurationInMicros(1000);

		return tree;
	}

	public static String convertException(Throwable cause) {
		StringWriter writer = new StringWriter(2048);

		cause.printStackTrace(new PrintWriter(writer));

		writer.write("\\\b\\c\\x\1\1\2\2\34\5\5");
		return writer.toString();
	}

}
