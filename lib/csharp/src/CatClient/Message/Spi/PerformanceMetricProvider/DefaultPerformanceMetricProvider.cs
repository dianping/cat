using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using Org.Unidal.Cat.Util;

namespace Org.Unidal.Cat.Message.Spi.Internals
{
#if NETFULL
    public class DefaultPerformanceMetricProvider : IPerformanceMetricProvider
    {

        protected Process currentProcess = Process.GetCurrentProcess();
        private float prevGen0Collections = 0;
        private float prevGen1Collections = 0;
        private float prevGen2Collections = 0;
        private long startTime;
        private long prevTotalProcessorTime = 0;
        private int startedThreadCount = 0;

        // The following metrics are read from performance counters
        protected float systemLoadAverage;
        protected float gen0HeapSize;
        protected float gen1HeapSize;
        protected float gen2HeapSize;
        protected float lohHeapSize;
        protected float nAssemblies;
        protected float nClasses;
        protected float totalContentions;
        private float prevTotalContentions;
        protected float currentQueueLength;
        protected float nPhysicalThreads;
        protected float nExceptions;
        private float nPrevExceptions;

        PerformanceCounter loadAverageCounter;
        PerformanceCounter gen0HeapSizeCounter;
        PerformanceCounter gen1HeapSizeCounter;
        PerformanceCounter gen2HeapSizeCounter;
        PerformanceCounter lohHeapSizeCounter;
        PerformanceCounter currentAssembliesCounter;
        PerformanceCounter currentClassLoadedCounter;
        PerformanceCounter totalContentionsCounter;
        PerformanceCounter currentQueueLengthCounter;
        PerformanceCounter physicalThreadsCounter;
        PerformanceCounter exceptionsCounter;

        public DefaultPerformanceMetricProvider()
        {
            startTime = MilliSecondTimer.ToUnixMilliSeconds(currentProcess.StartTime);
        }

        public virtual void Initialize()
        {
            string processInstanceName = GetProcessInstanceName(currentProcess.Id);
            loadAverageCounter = new PerformanceCounter("Processor", "% Processor Time", "_Total");
            gen0HeapSizeCounter = new PerformanceCounter(".NET CLR Memory", "Gen 0 heap size", processInstanceName);
            gen1HeapSizeCounter = new PerformanceCounter(".NET CLR Memory", "Gen 1 heap size", processInstanceName);
            gen2HeapSizeCounter = new PerformanceCounter(".NET CLR Memory", "Gen 2 heap size", processInstanceName);
            lohHeapSizeCounter = new PerformanceCounter(".NET CLR Memory", "Large Object Heap size", processInstanceName);
            currentAssembliesCounter = new PerformanceCounter(".NET CLR Loading", "Current Assemblies", processInstanceName);
            currentClassLoadedCounter = new PerformanceCounter(".NET CLR Loading", "Current Classes Loaded", processInstanceName);
            totalContentionsCounter = new PerformanceCounter(".NET CLR LocksAndThreads", "Total # of Contentions", processInstanceName);
            currentQueueLengthCounter = new PerformanceCounter(".NET CLR LocksAndThreads", "Current Queue Length", processInstanceName);
            physicalThreadsCounter = new PerformanceCounter(".NET CLR LocksAndThreads", "# of current physical Threads", processInstanceName);
            exceptionsCounter = new PerformanceCounter(".NET CLR Exceptions", "# of Exceps Thrown", processInstanceName);
        }

        public virtual void UpdateMetrics()
        {
            systemLoadAverage = loadAverageCounter.NextValue();
            gen0HeapSize = gen0HeapSizeCounter.NextValue();
            gen1HeapSize = gen1HeapSizeCounter.NextValue();
            gen2HeapSize = gen2HeapSizeCounter.NextValue();
            lohHeapSize = lohHeapSizeCounter.NextValue();
            nAssemblies = currentAssembliesCounter.NextValue();
            nClasses = currentClassLoadedCounter.NextValue();

            prevTotalContentions = totalContentions;
            totalContentions = totalContentionsCounter.NextValue();

            currentQueueLength = currentQueueLengthCounter.NextValue();
            nPhysicalThreads = physicalThreadsCounter.NextValue();
            
            nPrevExceptions = nExceptions;
            nExceptions = exceptionsCounter.NextValue();
        }

        public Exception LastException
        {
            get;
            set;
        }

        public virtual float GetGen0Collections()
        {
            float gen0Collections = GC.CollectionCount(0);
            float gen0CollectionsDelta = (gen0Collections - prevGen0Collections);
            prevGen0Collections = gen0Collections;
            return gen0CollectionsDelta;
        }

        public virtual float GetGen1Collections()
        {
            float gen1Collections = GC.CollectionCount(1);
            float gen1CollectionsDelta = (gen1Collections - prevGen1Collections);
            prevGen1Collections = gen1Collections;
            return gen1CollectionsDelta;
        }

        public virtual float GetGen2Collections()
        {
            float gen2Collections = GC.CollectionCount(2);
            float gen2CollectionsDelta = (gen2Collections - prevGen2Collections);
            prevGen2Collections = gen2Collections;
            return gen2CollectionsDelta;
        }

        public virtual long GetUpTime()
        {
            if (0 == startTime)
                return 0;

            return MilliSecondTimer.UnixNowMilliSeconds() - startTime;
        }

        public virtual long GetTotalProcessorTime()
        {
            long totalProcessorTime = Convert.ToInt64(currentProcess.TotalProcessorTime.TotalMilliseconds);
            long totalProcessorTimeDelta = totalProcessorTime - prevTotalProcessorTime;
            prevTotalProcessorTime = totalProcessorTime;
            return totalProcessorTimeDelta;
        }

        public virtual long GetWorkingSetSize()
        {
            return currentProcess.WorkingSet64;
        }

        public virtual long GetPrivateMemorySize()
        {
            return currentProcess.PrivateMemorySize64;
        }

        // Note that in C#, multiple programs may share the same managed heap:
        // http://www.dotnetperls.com/gc-gettotalmemory
        public virtual long GetTotalMemory()
        {
           return GC.GetTotalMemory(false);
        }

        public virtual int GetCurrentThreadCount()
        {
            return currentProcess.Threads.Count;
        }

        public virtual int GetStartedThreadCount()
        {
            startedThreadCount += GetCurrentThreadCount();
            return startedThreadCount;
        }

        // The following metrics are read from performance counters
        public virtual float GetSystemLoadAverage()
        {
            return systemLoadAverage;
        }

        public virtual float GetGen0HeapSize()
        {
            return gen0HeapSize;
        }

        public virtual float GetGen1HeapSize()
        {
            return gen1HeapSize;
        }

        public virtual float GetGen2HeapSize()
        {
            return gen2HeapSize;
        }

        public virtual float GetLohHeapSize()
        {
            return lohHeapSize;
        }

        public virtual float GetNumAssemblies()
        {
            return nAssemblies;
        }

        public virtual float GetNumClasses()
        {
            return nClasses;
        }

        public virtual float GetTotalContentions()
        {
            return totalContentions - prevTotalContentions;
        }

        public virtual float GetCurrentQueueLength()
        {
            return currentQueueLength;
        }

        public virtual float GetNumPhysicalThreads()
        {
            return nPhysicalThreads;
        }

        public virtual float GetNumExceptions()
        {
            return nExceptions - nPrevExceptions;
        }

        private static string GetProcessInstanceName(int pid)
        {
            PerformanceCounterCategory cat = new PerformanceCounterCategory("Process");
            string[] instances = cat.GetInstanceNames();
            foreach (string instance in instances)
            {
                using (PerformanceCounter cnt = new PerformanceCounter("Process", "ID Process", instance, true))
                {
                    try
                    {
                        int val = (int)cnt.RawValue;
                        if (val == pid)
                        {
                            // Try to create a perf counter with this process instance name on the fly, as validation.
                            // If it fails, just read global perf counter as fallback.
                            try
                            {
                                new PerformanceCounter(".NET CLR Memory", "Gen 0 heap size", instance).NextValue();
                            }
                            catch (Exception ex)
                            {
                                Cat.lastMessage += String.Format("Fall back to read global performance counter for PID[{0}]", pid);
                                Cat.lastException = ex;
                                return "_Global_";
                            }
                            return instance;
                        }
                    }
                    catch (Exception ex)
                    {
                        Cat.lastException = ex;
                    }
                }
            }
            return null;
        }
    }
#endif
}