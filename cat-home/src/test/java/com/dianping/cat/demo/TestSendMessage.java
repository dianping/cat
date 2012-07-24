package com.dianping.cat.demo;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

public class TestSendMessage {

	@Test
	public void sendMessage() throws Exception{
		for(int i=0;i<100;i++){
		Transaction t = Cat.getProducer().newTransaction("Test", "Test");
		
		t.addData("key and value");
		t.complete();
		
		}
		Thread.sleep(1000);
	}
}
