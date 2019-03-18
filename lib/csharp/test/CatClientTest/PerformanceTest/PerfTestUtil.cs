using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Org.Unidal.Cat.Util;

namespace CatClientTest.PerformanceTest
{
    class PerfTestUtil
    {
        public static void WriteLine(string format, params object[] args)
        {
            Console.WriteLine(MilliSecondTimer.UnixNowMilliSeconds() + " " + String.Format(format, args));
        }
    }
}
