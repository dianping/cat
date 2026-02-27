﻿using System;
using System.IO;
using Org.Unidal.Cat;
using Org.Unidal.Cat.Message;
using System.Threading;

namespace CatClientTest
{
    class TruncateTransactionTest
    {
        public static void Test1()
        {
            ITransaction transaction = Cat.NewTransaction("TruncateTransactionTest", "Root");
            for (int i = 0; i < 1000; i++)
            {
                // Cat.LogEvent("My event 4", "Level " + i);
                ITransaction childTransaction = Cat.NewTransaction("TruncateTransactionTest", "Child " + i);
                childTransaction.Status = CatConstants.SUCCESS;
                childTransaction.Complete();
            }
            transaction.Status = CatConstants.SUCCESS;
            transaction.Complete();
            Console.WriteLine("End of truncate transaction test");
            Thread.Sleep(1000);
        }

        public static void Test()
        {
            ITransaction transaction = Cat.NewTransaction("Test TruncateTransaction 0", "Root transaction");
            Thread.Sleep(15000);
            
            //Cat.LogEvent("Child Event", "Child Event");
            ITransaction childTransaction = Cat.NewTransaction("Child Transaction", "Child transaction");
            childTransaction.Status = CatConstants.SUCCESS;
            childTransaction.Complete();

            transaction.Status = CatConstants.SUCCESS;
            transaction.Complete();
            Console.WriteLine("End of truncate transaction test");
        }
    }
}
