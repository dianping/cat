package com.dianping.cat.demo;

import org.junit.Test;
import org.unidal.helper.Threads;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;

public class ThreadTest {

	@Test
	public void test() throws InterruptedException {
		Transaction t = Cat.newTransaction("test3", "test3");
		
		String id= Cat.getProducer().createMessageId();
		
		Threads.forGroup("cat").start(new Task(id));
		
		
		Cat.logEvent("RemoteLink", "ChildThread3", Event.SUCCESS, id);
		
		t.complete();
		
		Thread.sleep(1000);
	}
	
	public static class Task implements Runnable{
		
		private String m_messageId;

		public Task(String id){
			m_messageId = id;
		}
		
		@Override
      public void run() {

			Transaction t = Cat.newTransaction("test2", "test2");
			
			Cat.getManager().getThreadLocalMessageTree().setMessageId(m_messageId);

			t.complete();
      }
	}
}
