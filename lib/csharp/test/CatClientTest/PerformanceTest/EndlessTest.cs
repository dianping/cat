using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Org.Unidal.Cat.Message;
using Org.Unidal.Cat;
using System.Threading;
using System.Diagnostics;

namespace CatClientTest.PerformanceTest
{
    class EndlessTest
    {
        public static void Test()
        {
            Process currentProcess = Process.GetCurrentProcess();
            long nException = 0;
            int i = 0;
            const int SLEEP_PERIOD_MILLISECOND = 200;
            const string ID = "E";
            //ITransaction root = Cat.NewTransaction("Endless Test Root transaction " + ID, "Root transaction. Sleep period [" + SLEEP_PERIOD_MILLISECOND + " ms]");
            while (true)
            {
                ITransaction child = null;
                try
                {
                   child = Cat.NewTransaction("Endless Test Child transaction " + ID, "Child transaction " + i);
                }
                catch (Exception ex)
                {
                    PerfTestUtil.WriteLine("Exception occured in EndlessTest:\n" + ex);
                    nException++;
                }
                finally
                {
                    if (i % 1000 == 0)
                    {
                        string progress = "Endless Test progress (" + ID + "). Sleep period [" + SLEEP_PERIOD_MILLISECOND + " ms] Iteration [" + i + "] nException[" + nException + "] "
                            + Cat.ToText() + " PrivateMemorySize64[" + currentProcess.PrivateMemorySize64 + "] WorkingSet64[" + currentProcess.WorkingSet64 + "] GC.GetTotalMemory[" + GC.GetTotalMemory(false) + "] "
                            + "VirtualMemorySize64[" + currentProcess.VirtualMemorySize64 + "] PagedMemorySize64 [" + currentProcess.PagedMemorySize64 + "]";
                        Cat.LogEvent("Endless Test progress", progress);
                        PerfTestUtil.WriteLine(progress);
                    }

                    if (null!=child)
                    {
                        child.Status = CatConstants.SUCCESS;
                        child.Complete();
                    }

                    i++;
                    Thread.Sleep(SLEEP_PERIOD_MILLISECOND);
                }
            }
            //root.Status = CatConstants.SUCCESS;
            //root.Complete();
            //Console.WriteLine("End of Endless test. Should never reach here.");
        }
    }
}
