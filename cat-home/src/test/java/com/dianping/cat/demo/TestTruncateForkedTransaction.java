package com.dianping.cat.demo;

import com.dianping.cat.Cat;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yj.huang on 15-2-2.
 */
public class TestTruncateForkedTransaction {
    @Test
    public void testTruncateForkedTransaction() throws Exception {
        final String ID = "17";
        Transaction t = Cat.newTransaction("Test truncate forkedTransaction " + ID, "Root");

        final int N_FORKED = 1200;
        List<Thread> threadList = new ArrayList<Thread>(N_FORKED);
        for (int i = 0; i < N_FORKED; i++)
        {
            ForkedTransaction t1 = Cat.newForkedTransaction("ForkedChild", "Child" + i);
            Thread thread = new Thread(new TimedRunnable(t1, 100));
            threadList.add(thread);
        }

        for (Thread thread : threadList)
        {
            thread.start();
        }

        Thread.sleep(1000);
        t.setStatus(Message.SUCCESS);
        t.complete();

        for (Thread thread : threadList)
        {
            thread.join();
        }
        Thread.sleep(1000);
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
            try {
                m_transaction.fork();
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
}
