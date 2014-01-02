package com.dianping.cat;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;

public class CatEnvironmentTest {
	@Test
	public void testWithoutInitialize() throws InterruptedException {
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("TestType", "TestName");

		t.addData("data here");
		t.setStatus("TestStatus");
		t.complete();

		Thread.sleep(100);
		Assert.assertEquals(true, Cat.isInitialized());
		Cat.destroy();
	}

	@Test
	public void testWithInitialize() throws InterruptedException {
		Cat.initialize(new File("/data/appdatas/cat/client.xml"));
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("TestType", "TestName");

		t.addData("data here");
		t.setStatus("TestStatus");
		t.complete();

		Thread.sleep(100);

		Assert.assertEquals(true, Cat.isInitialized());
		Cat.destroy();
	}

	@Test
	public void testWithNoExistGlobalConfigInitialize() throws InterruptedException {
		Cat.initialize(new File("/data/appdatas/cat/clientNoExist.xml"));
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("TestType", "TestName");

		t.addData("data here");
		t.setStatus("TestStatus");
		t.complete();

		Thread.sleep(100);

		Assert.assertEquals(true, Cat.isInitialized());
		Cat.destroy();
	}
	
	
	@Test
	public void testJobTest() throws Exception{
		Cat.initialize("192.168.7.70","192.168.7.71");
		Transaction t = Cat.newTransaction("TestType", "TestName");

		t.addData("data here");
		t.setStatus("TestStatus");
		t.complete();

		Thread.sleep(10000);
	}
}
