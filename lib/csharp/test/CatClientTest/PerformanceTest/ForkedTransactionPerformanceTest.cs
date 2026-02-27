using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Org.Unidal.Cat.Message;
using Org.Unidal.Cat;
using System.Threading;

namespace CatClientTest.PerformanceTest
{
    class ForkedTransactionPerformanceTest
    {
        public static void Test()
        {
            try
            {
                const int ITER = 10000;
                Cat.Initialize();

                DateTime start = DateTime.Now;
                for (int i = 0; i < ITER; i++)
                {
                    CreateOneForkedTransaction(i);
                }
                Console.WriteLine("End to create forked transactions. latency: {0} ms", (DateTime.Now - start).TotalMilliseconds);

                start = DateTime.Now;

                for (int i = 0; i < ITER; i++)
                {
                    CreateOneNormalTransaction(i);
                }
                Console.WriteLine("End to create normal transactions. latency: {0} ms", (DateTime.Now - start).TotalMilliseconds);
                
            }
            catch (Exception ex)
            {
                Console.WriteLine("*** We have app-level exception: " + ex);
                throw ex;
            }
        }

        private static void CreateOneNormalTransaction(int i)
        {
            ITransaction t = Cat.NewTransaction("Iteration", "Iteration" + i);
            ITransaction child = Cat.NewTransaction("NormalTransaction", "NormalTransaction" + i);

            Cat.LogEvent("MyEvent", "MyEvent");

            child.Status = CatConstants.SUCCESS;
            child.Complete();

            t.Status = CatConstants.SUCCESS;
            t.Complete();
        }

        private static void CreateOneForkedTransaction(int i)
        {
            ITransaction t = Cat.NewTransaction("Iteration", "Iteration" + i);
            //IForkedTransaction forked = Cat.NewForkedTransaction("ForkedTransaction", "ForkedTransaction" + i);

            //Thread thread = new Thread(TimedWork.DoWork);
            // thread.Start(new Params(forked));
                
            t.Status = CatConstants.SUCCESS;
            t.Complete();

            //thread.Join();
        }
    }

    class TimedWork
    {
        public static void DoWork(Object obj)
        {
            var parameters = (Params)obj;
            var transaction = parameters._mTransaction;
            transaction.Fork();
            Cat.LogEvent("MyEvent", "MyEvent");
            transaction.Status = CatConstants.SUCCESS;
            transaction.Complete();
        }
    }

    class Params
    {
        public IForkedTransaction _mTransaction;

        public Params(IForkedTransaction transaction)
        {
            this._mTransaction = transaction;
        }
    }
}
