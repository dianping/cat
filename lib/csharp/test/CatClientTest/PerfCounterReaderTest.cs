using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Org.Unidal.Cat.Message.Spi.Internals;

namespace CatClientTest
{
    public class PerfCounterReaderTest
    {
        public static void Test()
        {
            //float processorTime = 0;
            //float gen0HeapSize = 0;
            //float gen1HeapSize = 0;
            //float gen2HeapSize = 0;
            //float lohHeapSize = 0;
            //float timeInGC = 0;
            //float nAssemblies = 0;
            //float nClasses = 0;
            //float totalContentions = 0;
            //float currentQueueLength = 0;
            //float nPhysicalThreads = 0;
            //float nExceptions = 0;

#if NETFULL
            IPerformanceMetricProvider provider = new DefaultPerformanceMetricProvider();
            provider.UpdateMetrics();
#endif

            //Console.WriteLine("processor time: " + processorTime);
        }
    }
}
