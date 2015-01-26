package com.dianping.cat.demo;

import com.dianping.cat.Cat;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageIdFactory;
import org.junit.Test;
import org.unidal.helper.Threads;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by yj.huang on 15-1-26.
 */
public class NormalTest {

    // yj.huang
    public static void main(String[] args) throws Exception {
        System.out.println("Normal test start.");

        try {
            NormalTest test = new NormalTest();
            // test.testTaggedTransaction();
            test.sendMessage();
            // test.testForkedTransaction();
            // test.testTruncateTransaction();
            // test.testMessageIdFactoryPerf();

            Thread.sleep(5000);
        } finally {
            System.out.println("End of normal test");
        }
    }

    @Test(timeout = 500)
    public void testMessageIdFactoryPerf() throws IOException {
        final int ITER = 5;
        final int MESSAGES_PER_ITER = 30000;

        long start = System.currentTimeMillis();
        String messageId = "";
        System.out.println(System.currentTimeMillis() + " Message ID factory performance test START. Iterations: " + ITER + " Messages per iteration: " + MESSAGES_PER_ITER);
        for (int i = 0; i < ITER; i++)
        {
            MessageIdFactory factory = new MessageIdFactory();
            factory.initialize("MessageIdFactoryTest");
            System.out.println(System.currentTimeMillis() + " Iteration " + i + " starts.");
            for (int j = 0; j < MESSAGES_PER_ITER; j++)
            {
                messageId = factory.getNextId();
            }
            System.out.println(System.currentTimeMillis() + " Iteration " + i + " ends with: " + messageId);
        }
        System.out.println(System.currentTimeMillis() + " Message ID factory performance test END. Latency: " + (System.currentTimeMillis() - start) + " ms.");
    }


    @Test
    public void sendMessage() throws Exception {
        Transaction t = Cat.newTransaction("Test sendMessage 2", "Test sendMessage 2");
        t.addData("key and value");

        for (int i = 0; i < 100; i++)
        {
            Cat.logEvent("great event", "great event " + i);
        }

        t.setStatus("0");
        t.complete();
    }

    @Test
    public void testForkedTransaction() throws Exception {
        Transaction t = Cat.newTransaction("Test ForkedTransaction 15", "Root");
        ForkedTransaction t1 = Cat.newForkedTransaction("ForkedChild", "Child1");
        ForkedTransaction t2 = Cat.newForkedTransaction("ForkedChild", "Child2");

        Thread thread1 = Threads.forGroup().start(new TimedRunnable(t1, 3000), false); // will run away
        Thread thread2 = Threads.forGroup().start(new TimedRunnable(t2, 100), false); // will be back in time

        Thread.sleep(1000);

        // Interrupt thread 1 to do error handling by linking away the forked transaction
        System.out.println("Interrupt thread 1");
        thread1.interrupt();
        t.setStatus(Message.SUCCESS);
        t.complete();

        thread1.join();
        thread2.join();
        Thread.sleep(3000);
    }

    @Test
    public void testTaggedTransaction() throws Exception {
        Transaction t = Cat.newTransaction("TaggedRoot", "Root");
        Cat.newTaggedTransaction("TaggedChild", "Child1", "Tag1");
        Cat.newTaggedTransaction("TaggedChild", "Child2", "Tag2");

        Threads.forGroup().start(new TaggedRunnable(500, "Tag1"));
        Threads.forGroup().start(new TaggedRunnable(100, "Tag2"));

        TimeUnit.MILLISECONDS.sleep(200);

        t.setStatus(Message.SUCCESS);
        t.complete();
    }

    @Test
    public void testTruncateTransaction() throws Exception {
        Transaction transaction = Cat.newTransaction("Test TruncateTransaction", "Root transaction");
        for (int i = 0; i < 1200; i++)
        {
            Cat.logEvent("My event", "Level " + i);
        }
        transaction.setStatus(Message.SUCCESS);
        transaction.complete();
        Thread.sleep(2000);
    }

    static class TimedRunnable implements Runnable {
        private ForkedTransaction m_transaction;

        private int m_timeout;

        public TimedRunnable(ForkedTransaction t, int timeout) {
            m_transaction = t;
            m_timeout = timeout;
        }

        @Override
        public void run() {
            m_transaction.fork();

            try {
                Thread.sleep(m_timeout);

                Cat.logEvent("TimedRunnable", "Timeout." + m_timeout + " forked transaction: " + m_transaction.getName());

                m_transaction.setStatus(Message.SUCCESS);
            } catch (Exception e) {
                System.out.println("Interrupted. forked transaction: " + m_transaction.getName() );
                Cat.logError("Exception. forked transaction: " + m_transaction.getName(), e);
                m_transaction.setStatus(e);
            } finally {
                m_transaction.complete();
            }
        }
    }

    static class TaggedRunnable implements Runnable {
        private int m_timeout;

        private String m_tag;

        public TaggedRunnable(int timeout, String tag) {
            m_timeout = timeout;
            m_tag = tag;
        }

        @Override
        public void run() {
            Transaction t = Cat.newTransaction("TaggedThread", m_tag);

            try {
                TimeUnit.MILLISECONDS.sleep(m_timeout);

                t.setStatus(Message.SUCCESS);
                Cat.getManager().bind(m_tag, "Child Tagged Thread");
            } catch (Exception e) {
                Cat.logError(e);
                t.setStatus(e);
            } finally {
                t.complete();
            }
        }
    }
}
