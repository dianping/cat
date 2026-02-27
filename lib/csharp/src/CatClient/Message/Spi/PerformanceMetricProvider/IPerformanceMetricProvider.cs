using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Org.Unidal.Cat.Message.Spi.Internals
{
    // Provide the metric for the last minute.
    // Each method should return the metric value accmulated within just one minte.
    // Such as:
    // (1) GC count within the last minute, not the total GC count in the whole history.
    // (2) Average CPU utilization (%) within the last minute, not the average CPU utilization of the whole history.
    // The implementation should catch exceptions granularily for each metric, so that failure to read one metric should not cause failure to read another metric.
    public interface IPerformanceMetricProvider
    {
        void Initialize();
        void UpdateMetrics();
        float GetGen0Collections();
        float GetGen1Collections();
        float GetGen2Collections();
        float GetSystemLoadAverage();
        float GetGen0HeapSize();
        float GetGen1HeapSize();
        float GetGen2HeapSize();
        float GetLohHeapSize();
        float GetNumAssemblies();
        float GetNumClasses();
        float GetTotalContentions();
        float GetCurrentQueueLength();
        float GetNumPhysicalThreads();
        float GetNumExceptions();
        long GetTotalProcessorTime();
        long GetWorkingSetSize();
        long GetPrivateMemorySize();
        long GetTotalMemory();
        long GetUpTime();
        int GetCurrentThreadCount();
        int GetStartedThreadCount();

        // Provide a callback for CAT client to get the last exception when reading metrics.
        // CAT client will report the exception in the heartbeat message.
        Exception LastException { get; set; }
    }
}
