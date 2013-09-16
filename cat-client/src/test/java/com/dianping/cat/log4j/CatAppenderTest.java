package com.dianping.cat.log4j;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class CatAppenderTest {
	@Test
	public void testWithTransaction() throws InterruptedException {
		CatAppender appender = new CatAppender();
		Throwable throwable = new Exception();
		Category logger = Logger.getLogger(CatAppenderTest.class);
		LoggingEvent event = new LoggingEvent("test", logger, Level.ERROR, null, throwable);
		Transaction t = Cat.getProducer().newTransaction("Test", "test");

		appender.append(event);

		t.setStatus(Message.SUCCESS);
		t.complete();

		Thread.sleep(20);
	}

	@Test
	public void testWithoutTransaction() throws InterruptedException {
		CatAppender appender = new CatAppender();
		Throwable throwable = new Exception();
		Category logger = Logger.getLogger(CatAppenderTest.class);
		LoggingEvent event = new LoggingEvent("test", logger, Level.ERROR, null, throwable);

		appender.append(event);

		Thread.sleep(10);
	}
}
