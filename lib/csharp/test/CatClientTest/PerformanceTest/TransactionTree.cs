using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Org.Unidal.Cat.Message;
using Org.Unidal.Cat;
using System.Threading;
using Org.Unidal.Cat.Util;


namespace CatClientTest.PerformanceTest
{
    class TestCompleteTransactionTree
    {
        public static void Test()
        {
            long start = MilliSecondTimer.UnixNowMilliSeconds();
            Console.WriteLine(Conf.AsString());
            PerfTestUtil.WriteLine("Test starts");
            IList<Thread> threads = new List<Thread>();
            for (int i = 0; i < Conf.N_THREADS; i++)
            {
                Thread thread = new Thread(TransactionTreeWorker.DoWork);
                threads.Add(thread);
            }

            foreach (Thread thread in threads)
            {
                thread.Start();
            }

            foreach (Thread thread in threads)
            {
                thread.Join();
            }
            PerfTestUtil.WriteLine("Test ends. latency: "
                + (MilliSecondTimer.UnixNowMilliSeconds() - start) + " ms.\n"
                + "Number of transactions created: " + TransactionTreeWorker.nTransactions + "\n");
        }
    }

    class Conf
    {
        public const int N_THREADS = 1;
        public const int N_TRANSACTIONS = 5;

        public static string AsString()
        {
            return "Number of threads: " + N_THREADS + "\n"
                + "Number of transactions in total: " + N_TRANSACTIONS + "\n";
        }
    }

    class TransactionTreeWorker
    {
        public static int nTransactions = 0;

        public static void DoWork()
        {
            long start = MilliSecondTimer.UnixNowMilliSeconds();
            PerfTestUtil.WriteLine("Thread " + Thread.CurrentThread.ManagedThreadId + " starts");

            CreateSubtree(0);

            PerfTestUtil.WriteLine("Thread " + Thread.CurrentThread.ManagedThreadId + " ends. thread latency: "
                + (MilliSecondTimer.UnixNowMilliSeconds() - start) + " ms.");
        }

        private static ITransaction CreateSubtree(int level)
        {
            if (level > Math.Log(Conf.N_TRANSACTIONS, 2))
                return null;;

            if (nTransactions >= Conf.N_TRANSACTIONS)
                return null;

            ITransaction child = Cat.NewTransaction("Perf test transaction C", "Transaction at level " + level);
            Interlocked.Increment(ref nTransactions);

            // i < 2 means we are creating a binary tree.
            for (int i = 0; i < 2; i++)
            {
                CreateSubtree(level + 1);
            }

            child.Status = CatConstants.SUCCESS;
            child.Complete();

            return child;
        }
    }
}
