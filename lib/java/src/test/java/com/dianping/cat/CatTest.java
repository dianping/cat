package com.dianping.cat;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CatTest {
    @Before
    public void initCat() {
        Cat.initializeByDomainForce("cat");
    }

    @Test
    public void transaction() {
        Transaction transaction = Cat.newTransaction("test", "test");
        transaction.setStatus(Message.SUCCESS);
        transaction.complete();
    }

    @Test
    public void event() {
        Cat.logEvent("test", "test");
    }

    @Test
    public void metric() {
        Cat.logMetricForCount("myKey", 1);
    }

    @After
    public void waitForSend() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        }
    }

}