using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Org.Unidal.Cat.Message;
using Org.Unidal.Cat;
using System.Threading;

namespace CatClientTest
{
    class EstimateByteSizeTest
    {
        private const string ID = "5";

        public static void Test()
        {
            // CheckConnectivityTest();
            // CheckMergedTreeSizeTest();
            CheckExactSizeTest();
        }

        private static void CheckMergedTreeSizeTest()
        {
            ITransaction root = Cat.NewTransaction("a", "a");
            ITransaction child1 = Cat.NewTransaction("a", "a");
            ITransaction child2 = Cat.NewTransaction("a", "a");
            child2.Status = CatConstants.SUCCESS;
            child2.Complete();

            for (int i = 0; i < 1200; i++)
            {
                Cat.LogEvent("a", "aa");
            }

            child1.Status = CatConstants.SUCCESS;
            child1.Complete();
            root.Status = CatConstants.SUCCESS;
            root.Complete();
        }

        private static void CheckConnectivityTest()
        {
            ITransaction root = Cat.NewTransaction("EstimateByteSizeTest", "RootTransaction");
            for (int i = 0; i < 100; i++) {
                ThreadPool.QueueUserWorkItem(Task);
            }
            root.Status = CatConstants.SUCCESS;
            root.Complete();
            Thread.Sleep(2000);
            Console.WriteLine("There should be 100000 ChildTransaction" + ID + " transactions.");
            Cat.LogEvent("EstimateByteSizeTestFinal", "End");
        }

        private static void CheckExactSizeTest()
        {
            ITransaction root = Cat.NewTransaction("a", "a");
            root.AddData("aaa");
            //Cat.LogEvent("a", "a",CatConstants.SUCCESS, "aaaa");
            //IDictionary<string, string> indexedTags = new Dictionary<string, string>();
            //indexedTags["aaaa"] = "bbbbbbb";
            //Cat.LogTags("a", indexedTags, null);

            root.Status = CatConstants.SUCCESS;
            var tree = Cat.GetThreadLocalMessageTree();
            Console.WriteLine("Estimated tree byte size: " + tree.EstimatedByteSize);
            root.Complete();

            Thread.Sleep(100);
            Console.WriteLine("Estimated tree byte size: " + tree.EstimatedByteSize);
            Thread.Sleep(100);
        }

        private static void MultiThreadTest()
        {
            ITransaction root = Cat.NewTransaction("a", "a");
            IList<Thread> threads = new List<Thread>();
            for (int i = 0; i < 100; i++)
            {
                Thread t = new Thread(Task);
                threads.Add(t);
            }
            foreach (Thread t in threads)
            {
                t.Start();
            }
            foreach (Thread t in threads)
            {
                t.Join();
            }
            root.Status = CatConstants.SUCCESS;
            root.Complete();
            Thread.Sleep(2000);
            Cat.LogEvent("EstimateByteSizeTestFinal", "End");
        }

        public static void Task(object o)
        {
            for (int i = 0; i < 1000; i++)
            {
                ITransaction taskRoot = Cat.NewTransaction("EstimateByteSizeTest", "ChildTransaction" + ID);
                try
                {
                    Cat.LogEvent("EstimateByteSizeTest", "TaskEvent");
                    taskRoot.Status = CatConstants.SUCCESS;
                }
                finally
                {
                    if (null != taskRoot)
                        taskRoot.Complete();
                }
            }
        }
    }
}
