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
    class FullSpeedSendTest
    {
        public static void Test()
        {
            try
            {
                const string UNIQUE_ID = "B9";
                const int N_TRANSACTIONS = 10240000;
                PerfTestUtil.WriteLine("Full speed send test {0} START. N_TRANSACTIONS[{1}] Sender queue size[{2}]", UNIQUE_ID, N_TRANSACTIONS, 1000);
                // Comment out rootTransaction. Because in real prod environment, truncating transaction rarely happens.
                ITransaction rootTransaction = Cat.NewTransaction("Root transaction of full speed send test " + UNIQUE_ID + " N_TRANSACTIONS [" + N_TRANSACTIONS + "]", "Root transaction of full speed send test");
                long start = MilliSecondTimer.UnixNowMilliSeconds();
                for (int i = 0; i < N_TRANSACTIONS; i++)
                {
                    // ITransaction child = Cat.NewTransaction("Child transaction of full speed send test " + UNIQUE_ID + " N_TRANSACTIONS [" + N_TRANSACTIONS + "]", "");
                    ITransaction child = Cat.NewTransaction("Child", "");
                    child.Status = CatConstants.SUCCESS;
                    child.Complete();
                }
                rootTransaction.Status = CatConstants.SUCCESS;
                rootTransaction.Complete();
                PerfTestUtil.WriteLine("Full speed send test {0} END. Latency[{1} ms]. {2}", UNIQUE_ID, (MilliSecondTimer.UnixNowMilliSeconds() - start), Cat.ToText());
            }
            catch (Exception ex)
            {
                PerfTestUtil.WriteLine("Exception occurred in full speed send test:\n " + ex);
                throw ex;
            }
        }
    }
}
