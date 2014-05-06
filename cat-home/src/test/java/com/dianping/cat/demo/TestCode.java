package com.dianping.cat.demo;


import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class TestCode {
	
	
	public void logError(Throwable able){
		  Transaction t = Cat.newTransaction("Neocortex", "Error");
		  
		  Cat.logError(able);
		  

        MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

        ((DefaultMessageTree) tree).setDomain("NeoCortex");
		  t.complete();
	}

    @Test
    public void test1() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            test();
        }

        Thread.sleep(1000 * 10);
    }

    public void test() throws InterruptedException {
        Transaction t = Cat.newTransaction("Neocortex", "function1");
        try {
            int a = functionA();

            if (a < 0) {
                Cat.logError(new RuntimeException("sdsf"));
            }

            MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

            ((DefaultMessageTree) tree).setDomain("NeoCortex");
            t.setStatus(Transaction.SUCCESS);
        } catch (Exception e) {

            Cat.logError(e);
            t.setStatus(e);

        } finally {
            t.complete();
        }


    }

    private int functionA() {

        return (int) (Math.random() * 100) - 50;
    }

}
