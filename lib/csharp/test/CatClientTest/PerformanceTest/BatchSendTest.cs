using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Org.Unidal.Cat.Message;
using Org.Unidal.Cat;
using System.Threading;
using System.Diagnostics;
using Org.Unidal.Cat.Util;

namespace CatClientTest.PerformanceTest
{
    class BatchSendTest
    {
        public static void Test()
        {
            try
            {
                const string UNIQUE_ID = "S";
                // Number of batches
                const int N_BATCH = 1024;
                // Number of transactions in each batch
                const int BATCH_SIZE = 1000;
                // Sleep SLEEP_PERIOD ms after sending one batch.
                const int SLEEP_PERIOD = 50;
                
                PerfTestUtil.WriteLine("Batch send test {0} START. N_BATCH[{1}], BATCH_SIZE[{2}] SLEEP_PERIOD[{3}] Sender queue size[{4}]",
                    UNIQUE_ID, N_BATCH, BATCH_SIZE, SLEEP_PERIOD, 1000);
                ITransaction rootTransaction = Cat.NewTransaction(
                    "Root transaction of batch send test " + UNIQUE_ID + " N_BATCH [" + N_BATCH+ "] BATCH_SIZE [" + BATCH_SIZE + "] SLEEP_PERIOD[" + SLEEP_PERIOD + " "
                    + " Sender queue size[" +1000 + "]", "Root transaction of batch send test");
                long start = MilliSecondTimer.UnixNowMilliSeconds();
                for (int i = 0; i < N_BATCH; i++)
                {
                    for (int j = 0; j < BATCH_SIZE; j++)
                    {
                        ITransaction child = Cat.NewTransaction("Child transaction of batch send test " + UNIQUE_ID, "");
                        child.Status = CatConstants.SUCCESS;
                        child.Complete();
                    }
                    Thread.Sleep(SLEEP_PERIOD);
                }
                rootTransaction.Status = CatConstants.SUCCESS;
                rootTransaction.Complete();
                PerfTestUtil.WriteLine("Batch send test {0} END. Latency[{1} ms]. {2}", UNIQUE_ID, (MilliSecondTimer.UnixNowMilliSeconds() - start), Cat.ToText());
            }
            catch (Exception ex)
            {
                PerfTestUtil.WriteLine("Exception occurred in full speed send test:\n " + ex);
                throw ex;
            }
        }
    }
}
