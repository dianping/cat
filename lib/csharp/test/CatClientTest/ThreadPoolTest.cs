using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Org.Unidal.Cat;
using System.Threading;
using Org.Unidal.Cat.Message;

namespace CatClientTest.PerformanceTest
{
    class ThreadPoolTest
    {
        public static void Test()
        {
            const int N_TASKS = 1;
            ITransaction rootTransaction = null; 
            try {
                rootTransaction  = Cat.NewTransaction("ThreadPoolTest", "ThreadPoolTest");
                for (int i = 0; i < N_TASKS; i++)
                {
                    IForkedTransaction forkedTransaction = Cat.NewForkedTransaction("Task", "Task" + i);
                    ThreadPool.QueueUserWorkItem(Task, forkedTransaction);
                }
                Cat.LogEvent("ThreadPoolTest", "SimpleEvent");
                rootTransaction.Status = CatConstants.SUCCESS;
            } finally {
                if (null != rootTransaction)
                    rootTransaction.Complete();
            }
            Thread.Sleep(10000);
        }

        private static void Task(object forkedTransaction)
        {
            IForkedTransaction transaction = null;
            try { 
                transaction = (IForkedTransaction)forkedTransaction;
                transaction.Fork();
                Cat.LogEvent("TaskEvent", "TaskEvent");

                IForkedTransaction subTask1Tranasction = Cat.NewForkedTransaction("SubTask1", "SubTask1");
                ThreadPool.QueueUserWorkItem(SubTask1, subTask1Tranasction);
                IForkedTransaction subTask2Tranasction = Cat.NewForkedTransaction("SubTask2", "SubTask2");
                ThreadPool.QueueUserWorkItem(SubTask2, subTask2Tranasction);

                transaction.Status = CatConstants.SUCCESS;
            }
            finally {
                if (null != transaction)
                    transaction.Complete();
            }
        }

        private static void SubTask1(object forkedTransaction)
        {
            IForkedTransaction transaction = null;
            try
            {
                transaction = (IForkedTransaction)forkedTransaction;
                transaction.Fork();
                Cat.LogEvent("SubTask1Event", "SubTask1Event");
                transaction.Status = CatConstants.SUCCESS;
            }
            finally {
                if (null != transaction)
                    transaction.Complete();
            }
        }

        private static void SubTask2(object forkedTransaction)
        {
            IForkedTransaction transaction = null;
            try
            {
                transaction = (IForkedTransaction)forkedTransaction;
                transaction.Fork();
                Cat.LogEvent("SubTask2Event", "SubTask2Event");
                transaction.Status = CatConstants.SUCCESS;
            }
            finally
            {
                if (null != transaction)
                    transaction.Complete();
            }
        }
    }
}
