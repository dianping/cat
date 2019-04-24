using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Org.Unidal.Cat.Message;
using Org.Unidal.Cat;
using System.Threading;

namespace CatClientTest.PerformanceTest
{
    class MultithreadTest
    {
        const int TOTAL_MESSAGES_PER_SECOND = 200000;
        const int DURATION_MINUTES = 10;
        const int N_THREADS = 2000;
        private static int nThreads = 0;
        private static int sum = 0;
        public static void Test()
        {
            Console.WriteLine(DateTime.Now + " Start of multi thread test. N_THREADS: " + N_THREADS);
            Thread[] threads = new Thread[N_THREADS];
            for (int i = 0; i < N_THREADS; i++)
            {
                threads[i] = new Thread(Work);
            }
            for (int i = 0; i < N_THREADS; i++)
            {
                threads[i].Start();
            }
            for (int i = 0; i < N_THREADS; i++)
            {
                threads[i].Join();
            }
            Console.WriteLine("Total count: " + sum);
            Console.WriteLine(DateTime.Now + " End of multi thread test. Domain: " + Cat.Domain);
        }

        private static void Work()
        {
            Interlocked.Increment(ref nThreads);
            //Console.WriteLine(nThreads + " started.");
            int count = 0;
            var start = DateTime.Now;
            while (DateTime.Now - start < TimeSpan.FromMinutes(DURATION_MINUTES))
            {
                ITransaction root = Cat.NewTransaction("Multi-thread Root", "");
                ITransaction child = Cat.NewTransaction("Multi-thread child", "");
                Cat.LogEvent("Child event", "");
                child.Status = CatConstants.SUCCESS;
                child.Complete();
                root.Status = CatConstants.SUCCESS;
                root.Complete();
                count++;
                //if (count % (TOTAL_MESSAGES_PER_SECOND / N_THREADS) == 0)
                //{
                //    Thread.Sleep(1000);
                //}
            }
            Interlocked.Add(ref sum, count);
        }
    }
}
