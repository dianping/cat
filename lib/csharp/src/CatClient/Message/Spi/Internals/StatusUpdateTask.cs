using System;
using System.Diagnostics;
using System.Globalization;
#if NETFULL
using System.Management;
#endif
using System.Threading;
using System.Text;
using Org.Unidal.Cat.Util;
using System.Xml;
using System.IO;
using System.Reflection;
using System.Collections.Generic;
using Org.Unidal.Cat.Configuration;

namespace Org.Unidal.Cat.Message.Spi.Internals
{
    public class StatusUpdateTask
    {
        private const int LONG_INTERVAL_HEARBEAT_MINUTES = 20;
        private readonly IMessageStatistics _mStatistics;
        private long physicalMemory = 0;
        private long startTime = 0;
        private string dotNetVersion = String.Empty;
        private string ntVersion = String.Empty;
        private int processorCount = 1;
        private string userName = String.Empty;
        private string arch = String.Empty;

        private static Process currentProcess;

        private string fileVersion;
        private string componentVersions = "";
        private string componentsChecksum = "";

        private AbstractClientConfig config;
        private IPerformanceMetricProvider perfMetricProvider;

        public StatusUpdateTask(IMessageStatistics mStatistics, AbstractClientConfig c)
        {
            try
            {
                _mStatistics = mStatistics;
                config = c;
                currentProcess = Process.GetCurrentProcess();

                Assembly executingAssembly = Assembly.GetExecutingAssembly();
                fileVersion = FileVersionInfo.GetVersionInfo(executingAssembly.Location).FileVersion.ToString();

                physicalMemory = GetPhysicalMemory();

                if (null != currentProcess) startTime = MilliSecondTimer.ToUnixMilliSeconds(currentProcess.StartTime);

                dotNetVersion = Environment.Version.ToString();
                
                if (null != currentProcess) userName = currentProcess.StartInfo.UserName;
                
                arch = Environment.GetEnvironmentVariable("PROCESSOR_ARCHITECTURE");

                ntVersion = Environment.OSVersion.Version.Major + "." + Environment.OSVersion.Version.Minor;

                processorCount = Environment.ProcessorCount;

#if NETFULL
                perfMetricProvider = new DefaultPerformanceMetricProvider();
                perfMetricProvider.Initialize();
#endif
            }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        public void Run(object o)
        {
            try
            {
                // Get DLL versions
                collectComponentVersions();
                Random random = new Random();
                int initialSleep = random.Next(CatConstants.HEARTBEAT_MIN_INITIAL_SLEEP_MILLISECONDS, CatConstants.HEARTBEAT_MAX_INITIAL_SLEEP_MILLISECONDS);
                Console.WriteLine("Heartbeat initial sleep: " + initialSleep + " ms");
                Thread.Sleep(initialSleep);
            }
            catch (Exception ex) { Cat.lastException = ex; return; }

            // In Java, try to avoid send heartbeat at 59-01 second, which is missing here in .NET

            // In Java, try to build class paths, which is a list of jar file names.
            try
            {
                ITransaction reboot = Cat.NewTransaction("System", "Reboot");
                reboot.Status = CatConstants.SUCCESS;
                Cat.LogEvent("Reboot", NetworkInterfaceManager.HostIP, CatConstants.SUCCESS);
                reboot.Complete();

                DateTime lastSendVersionsTimestamp = default(DateTime);
                while (true)
                {
                    ITransaction t = Cat.NewTransaction("System", "Status");
                    t.AddData(CatConstants.DUMP_LOCKED, false);
                    IHeartbeat h = Cat.NewHeartbeat("Heartbeat", NetworkInterfaceManager.HostIP);
                    try
                    {
                        var now = DateTime.Now;
                        bool isLongIntevalHeartbeat = false;
                        if (default(DateTime) == lastSendVersionsTimestamp 
                            || now.Hour != lastSendVersionsTimestamp.Hour
                            || (now - lastSendVersionsTimestamp >= TimeSpan.FromMinutes(LONG_INTERVAL_HEARBEAT_MINUTES)))
                        {
                            isLongIntevalHeartbeat = true;
                            lastSendVersionsTimestamp = now;
                        }
                        h.AddData(BuildStatusData(isLongIntevalHeartbeat));
                        h.Status = CatConstants.SUCCESS;

                        if (isLongIntevalHeartbeat)
                        {
                            string configHeartBeatMessage = config.GetConfigHeartbeatMessage();
                            if (!String.IsNullOrWhiteSpace(configHeartBeatMessage)) {
                                Cat.LogEvent("Cat.Client.InconsistentAppId", configHeartBeatMessage);
                            }
                        }
                    }
                    catch (Exception ex)
                    {
                        Cat.lastException = ex;
                        h.SetStatus(ex);
                        Cat.LogError(ex);
                    }
                    finally
                    {
                        h.Complete();
                    }

                    if (!String.IsNullOrEmpty(fileVersion))
                    {
                        Cat.LogEvent("Cat.Client.Version", fileVersion);
                    }
                    
                    t.Status = CatConstants.SUCCESS;
                    t.Complete();

                    // Append Cat.lastException if not null
                    if (null != Cat.lastException)
                    {
                        Exception ex = Cat.lastException;
                        Cat.lastException = null;
                        Cat.LogEvent("Cat.Client.LastException", ex.GetType().Name, CatConstants.SUCCESS, ex.ToString()); 
                    }

                    // Append Cat.lastMessage if not null
                    if (!String.IsNullOrWhiteSpace(Cat.lastMessage))
                    {
                        Cat.LogEvent("Cat.Client.LastMessage", "msg", CatConstants.SUCCESS, "message=" + Cat.lastMessage); 
                        Cat.lastMessage = null;
                    }

                    // Sleep to the 30th second of the next minute, not to the 30th second of the current minute.
                    var sleepInSeconds = 90 - DateTime.Now.Second;
                    Thread.Sleep(sleepInSeconds * 1000);
                }
            }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        private string BuildStatusData(bool needSendVersions = false) 
        {
            try { perfMetricProvider.UpdateMetrics(); } catch (Exception ex) { Cat.lastException = ex; }

            /****** Step 1: collect (most) platform / hardware info ******/
            float gen0CollectionsDelta = 0;
            try { gen0CollectionsDelta = perfMetricProvider.GetGen0Collections(); } catch (Exception ex) { Cat.lastException = ex; }

            float gen1CollectionsDelta = 0;
            try { gen1CollectionsDelta = perfMetricProvider.GetGen1Collections(); } catch (Exception ex) { Cat.lastException = ex; }

            float gen2CollectionsDelta = 0;
            try { gen2CollectionsDelta = perfMetricProvider.GetGen2Collections(); } catch (Exception ex) { Cat.lastException = ex; }

            long upTime = 0;
            try { upTime = perfMetricProvider.GetUpTime(); } catch (Exception ex) { Cat.lastException = ex; }

            long totalProcessorTimeDelta = 0;
            try { totalProcessorTimeDelta = perfMetricProvider.GetTotalProcessorTime(); } catch (Exception ex) { Cat.lastException = ex; }

            long workingSetSize = 0;
            try { workingSetSize = perfMetricProvider.GetWorkingSetSize(); } catch (Exception ex) { Cat.lastException = ex; }

            long privateMemorySize = 0;
            try { privateMemorySize = perfMetricProvider.GetPrivateMemorySize(); } catch (Exception ex) { Cat.lastException = ex; }

            long heapTotalMemory = 0;
            try { heapTotalMemory = perfMetricProvider.GetTotalMemory(); } catch (Exception ex) { Cat.lastException = ex; }

            int threadCount = 0;
            try { threadCount = perfMetricProvider.GetCurrentThreadCount(); } catch (Exception ex) { Cat.lastException = ex; }
            
            int startedCount = 0;
            try { startedCount = perfMetricProvider.GetStartedThreadCount(); } catch (Exception ex) { Cat.lastException = ex; }

            float systemLoadAverage = 0;
            try { systemLoadAverage = perfMetricProvider.GetSystemLoadAverage(); } catch (Exception ex) { Cat.lastException = ex; }

            float gen0HeapSize = 0;
            try { gen0HeapSize = perfMetricProvider.GetGen0HeapSize(); } catch (Exception ex) { Cat.lastException = ex; }

            float gen1HeapSize = 0;
            try { gen1HeapSize = perfMetricProvider.GetGen1HeapSize(); } catch (Exception ex) { Cat.lastException = ex; }

            float gen2HeapSize = 0;
            try { gen2HeapSize = perfMetricProvider.GetGen2HeapSize(); } catch (Exception ex) { Cat.lastException = ex; }

            float lohHeapSize = 0;
            try { lohHeapSize = perfMetricProvider.GetLohHeapSize(); } catch (Exception ex) { Cat.lastException = ex; }

            float nAssemblies = 0;
            try { nAssemblies = perfMetricProvider.GetNumAssemblies(); } catch (Exception ex) { Cat.lastException = ex; }

            float nClasses = 0;
            try { nClasses = perfMetricProvider.GetNumClasses(); } catch (Exception ex) { Cat.lastException = ex; }

            float totalContentionsDelta = 0;
            try { totalContentionsDelta = perfMetricProvider.GetTotalContentions(); } catch (Exception ex) { Cat.lastException = ex; }

            float currentQueueLength = 0;
            try { currentQueueLength = perfMetricProvider.GetCurrentQueueLength(); } catch (Exception ex) { Cat.lastException = ex; }

            float nPhysicalThreads = 0;
            try { nPhysicalThreads = perfMetricProvider.GetNumPhysicalThreads(); } catch (Exception ex) { Cat.lastException = ex; }

            float nExceptionsDelta = 0;
            try { nExceptionsDelta = perfMetricProvider.GetNumExceptions(); } catch (Exception ex) { Cat.lastException = ex; }

            // TODO: thread dump is not available

            /****** Step 2: Build XML ******/

            var xml = new XmlDocument();
            XmlDeclaration xmlDecl = xml.CreateXmlDeclaration("1.0", "utf-8", null);
            xml.AppendChild(xmlDecl);

            var root = createXmlElement(xml, "status", new Pair<string, string>("timestamp", DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss.fff")));

            var runTime = createXmlElement(xml, "runtime", new Pair<string, string>("start-time", startTime.ToString()),
                new Pair<string, string>("up-time", upTime.ToString()),
                new Pair<string, string>("java-version", dotNetVersion),
                new Pair<string, string>("user-name", userName),
                new Pair<string, string>("file-version", fileVersion),
                new Pair<string, string>("last-flush-mark", MessageIdFactory._mLastMarkFlush.ToString()));

            if (needSendVersions)
            {
                var versions = createXmlElement(xml, "net-components",
                    new Pair<string, string>("checksum", this.componentsChecksum));
                versions.InnerText = this.componentVersions;
                runTime.AppendChild(versions);
            }

            root.AppendChild(runTime);

            var userDir = xml.CreateElement("", "user-dir", "");
            userDir.InnerText = AppDomain.CurrentDomain.BaseDirectory;
            runTime.AppendChild(userDir);

            var os = createXmlElement(xml, "os", new Pair<string, string>("name", "Windows"),
                new Pair<string, string>("arch", arch),
                new Pair<string, string>("version", ntVersion),
                new Pair<string, string>("available-processors", processorCount + ""),
                new Pair<string, string>("system-load-average", systemLoadAverage + ""),
                new Pair<string, string>("process-time", totalProcessorTimeDelta.ToString()),
                new Pair<string, string>("total-physical-memory", physicalMemory + ""),
                new Pair<string, string>("assemblies-loaded", nAssemblies.ToString()),
                new Pair<string, string>("class-loaded", nClasses.ToString()));
            root.AppendChild(os);

            var disk = xml.CreateElement("", "disk", "");
            root.AppendChild(disk);

            try
            {
                DriveInfo[] drives = DriveInfo.GetDrives();
                foreach (DriveInfo drive in drives)
                {
                    try
                    {
                        if (!drive.IsReady)
                            continue;
                        var diskVolume = createXmlElement(xml, "disk-volume", new Pair<string, string>("id", drive.Name),
                         new Pair<string, string>("total", drive.TotalSize.ToString()),
                         new Pair<string, string>("free", drive.TotalFreeSpace.ToString()),
                         new Pair<string, string>("usable", (drive.TotalSize - drive.TotalFreeSpace).ToString()));
                        disk.AppendChild(diskVolume);
                    }
                    catch (Exception) {
                        // We do not keep this exception,
                        // in order to avoid too many error message: System.Cat.LastException 设备未就绪。 at System.IO.__Error.WinIOError(Int32 errorCode, String maybeFullPath) 
                    }   
                }
            }
            catch (Exception) {
                // We do not keep this exception,
                // in order to avoid too many error message: System.Cat.LastException 设备未就绪。 at System.IO.__Error.WinIOError(Int32 errorCode, String maybeFullPath) 
            }

            var memory = createXmlElement(xml, "memory",new Pair<string, string>("private-memory-size", privateMemorySize.ToString()),
                new Pair<string, string>("working-set-size", workingSetSize.ToString()),
                new Pair<string, string>("heap-total-memory", heapTotalMemory.ToString()));

            var gen0GC = createXmlElement(xml, "gc", new Pair<string, string>("name", "Gen 0"),
                new Pair<string, string>("count", gen0CollectionsDelta.ToString()));

            var gen1GC = createXmlElement(xml, "gc", new Pair<string, string>("name", "Gen 1"),
                new Pair<string, string>("count", gen1CollectionsDelta.ToString()));

            var gen2GC = createXmlElement(xml, "gc", new Pair<string, string>("name", "Gen 2"),
                new Pair<string, string>("count", gen2CollectionsDelta.ToString()));

            memory.AppendChild(gen0GC);
            memory.AppendChild(gen1GC);
            memory.AppendChild(gen2GC);

            root.AppendChild(memory);

            var thread = createXmlElement(xml, "thread", new Pair<string, string>("count", threadCount.ToString()),
                new Pair<string, string>("total-started-count", startedCount.ToString()));
            root.AppendChild(thread);

            var dump = xml.CreateElement("", "dump", "");
            dump.InnerText = string.Empty;
            thread.AppendChild(dump);

            var message = createXmlElement(xml, "message", new Pair<string, string>("produced", _mStatistics.Produced + ""),
                new Pair<string, string>("overflowed", _mStatistics.Overflowed + ""),
                new Pair<string, string>("bytes", _mStatistics.Bytes + ""));
            root.AppendChild(message);

            var systemExtension = createExtension(xml, "System", new Pair<string, float>("LoadAverage", systemLoadAverage));
            root.AppendChild(systemExtension);

            try
            {
                DriveInfo[] drives = DriveInfo.GetDrives();
                int lenth = drives.Length;
                Pair<string, float>[] pairArray = new Pair<string, float>[lenth];
                for (int i = 0; i < lenth; i++)
                {
                    DriveInfo drive = drives[i];
                    pairArray[i] = new Pair<string, float>(drive.Name + " Free", drive.TotalFreeSpace);
                }
                var diskExtension = createExtension(xml, "Disk", pairArray);
                root.AppendChild(diskExtension);
            }
            catch (Exception) {
                // We do not keep this exception,
                // in order to avoid too many error message: System.Cat.LastException 设备未就绪。 at System.IO.__Error.WinIOError(Int32 errorCode, String maybeFullPath) 
            }

            var catUsageExtension = createExtension(xml, "CatUsage", new Pair<string, float>("Produced", _mStatistics.Produced),
                new Pair<string, float>("Overflowed", _mStatistics.Overflowed),
                new Pair<string, float>("Bytes", _mStatistics.Bytes));
            root.AppendChild(catUsageExtension);

            var heapExtension = createExtension(xml, "HeapUsage", new Pair<string, float>("Gen-0-Collections", gen0CollectionsDelta),
                new Pair<string, float>("Gen-1-Collections", gen1CollectionsDelta),
                new Pair<string, float>("Gen-2-Collections", gen2CollectionsDelta),
                new Pair<string, float>("Gen-0-HeapSize", gen0HeapSize),
                new Pair<string, float>("Gen-1-HeapSize", gen1HeapSize),
                new Pair<string, float>("Gen-2-HeapSize", gen2HeapSize),
                new Pair<string, float>("Large-Object-HeapSize", lohHeapSize));
            root.AppendChild(heapExtension);

            var locksAndThreadsExtension = createExtension(xml, "LocksAndThreads", new Pair<string, float>("Total-Contentions", totalContentionsDelta),
                new Pair<string, float>("Current-Queue-Length", currentQueueLength),
                new Pair<string, float>("Current-Physical-Threads", nPhysicalThreads),
                new Pair<string, float>("Exceptions-Thrown", nExceptionsDelta));
            root.AppendChild(locksAndThreadsExtension);

            xml.AppendChild(root);
            
            string ret =  XMLDocumentToString(xml);

            return ret;
        }

        private string XMLDocumentToString(XmlDocument doc)
        {
            MemoryStream stream = new MemoryStream();
            StreamReader sr = null;
            try
            {
                XmlTextWriter writer = new XmlTextWriter(stream, null);
                writer.Formatting = Formatting.Indented;
                doc.Save(writer); //转换

                sr = new StreamReader(stream, System.Text.Encoding.UTF8);
                stream.Position = 0;
                string xmlString = sr.ReadToEnd();
                return xmlString;
            }
            catch (Exception ex) { Cat.lastException = ex; }
            finally
            {
                if (sr != null)
                {
                    try
                    {
                        sr.Close();
                    }
                    catch (Exception ex) { Cat.lastException = ex; }
                }
                try
                {
                    sr.Close();
                    stream.Close();
                }
                catch (Exception ex) { Cat.lastException = ex; }
            }
            return "no data";
        }

        /// <summary>
        ///   获取系统内存大小
        /// </summary>
        /// <returns> 内存大小(单位M) </returns>
        private static long GetPhysicalMemory()
        {
#if !NETFULL
            return 0;
#else

            ManagementObjectSearcher searcher = new ManagementObjectSearcher(); //用于查询一些如系统信息的管理对象 
            searcher.Query = new SelectQuery("Win32_PhysicalMemory ", "", new[] { "Capacity" }); //设置查询条件 
            ManagementObjectCollection collection = searcher.Get(); //获取内存容量 
            ManagementObjectCollection.ManagementObjectEnumerator em = collection.GetEnumerator();

            long capacity = 0;
            while (em.MoveNext())
            {
                ManagementBaseObject baseObj = em.Current;
                if (baseObj.Properties["Capacity"].Value != null)
                {
                    try
                    {
                        capacity += long.Parse(baseObj.Properties["Capacity"].Value.ToString());
                    }
                    catch (Exception ex)
                    {
                        Cat.lastException = ex;
                        return 0;
                    }
                }
            }

            if (null != em)
            {
                em.Dispose();
                em = null;
            }
            if (null != collection)
            {
                collection.Dispose();
                collection = null;
            }
            if (null != searcher)
            {
                searcher.Dispose();
                searcher = null;
            }

            return capacity;
#endif
        }

        private XmlElement createExtension(XmlDocument xml, string extensionId, params Pair<string, float>[] idAndValues)
        {
            var extension = xml.CreateElement("", "extension", "");
            extension.SetAttribute("id", extensionId);
            if (idAndValues != null)
            {
                foreach (var idAndValue in idAndValues)
                {
                    var extensionDetail = xml.CreateElement("", "extensionDetail", "");
                    extensionDetail.SetAttribute("id", idAndValue.key);
                    extensionDetail.SetAttribute("value", idAndValue.value.ToString());
                    extension.AppendChild(extensionDetail);
                }
            }
            return extension;
        }

        private XmlElement createXmlElement(XmlDocument xml, string elementId, params Pair<string, string>[] keyAndValues)
        {
            var element = xml.CreateElement("", elementId, "");
            if (keyAndValues != null)
            {
                foreach (var idAndValue in keyAndValues)
                {
                    element.SetAttribute(idAndValue.key,idAndValue.value);
                }
            }
            return element;
        }

        private void collectComponentVersions()
        {
            const string NEW_LINE = "\n";
            const string SEMI_COLON = ";";
            StringBuilder sb = new StringBuilder(4096);
            StringBuilder md5base = new StringBuilder(4096);
            List<Assembly> assemblies = new List<Assembly>();
            foreach (var assembly in AppDomain.CurrentDomain.GetAssemblies())
            {
                if (null == assembly || assembly.IsDynamic)
                {
                    continue;
                }
                assemblies.Add(assembly);
            }
            // Sort all the assemblies, so that the checksum over all of them is the same across all machines.
            assemblies.Sort(delegate(Assembly a, Assembly b)
            {
                if (null == a && null == b) return 0;
                else if (null == a) return -1;
                else if (null == b) return 1;
                else
                {
                    try
                    {
                        string aFileName = GetFileNameFromLocation(a.Location);
                        string bFileName = GetFileNameFromLocation(b.Location);
                        return aFileName.CompareTo(bFileName);
                    }
                    catch (Exception ex1)
                    {
                        try
                        {
                            Cat.lastException = ex1;
                            return a.FullName.CompareTo(b.FullName);
                        }
                        catch (Exception ex2)
                        {
                            Cat.lastException = ex2;
                            return 0;
                        }
                    }
                }
            });
            foreach (var assembly in assemblies)
            {
                try {
                    string location = assembly.Location;
                    FileVersionInfo fileVersionInfo = FileVersionInfo.GetVersionInfo(assembly.Location);
                    string fileVersion = fileVersionInfo.FileVersion.ToString();
                    string productVersion = fileVersionInfo.ProductVersion.ToString();
                    string fileName = GetFileNameFromLocation(location);
                    sb.Append(location).Append("?");
                    md5base.Append(fileName).Append("?"); // md5base does not consider full path but only file name.

                    sb.Append("v=").Append(fileVersion).Append(SEMI_COLON);
                    // md5base does not consider fileVersion but only productVersion

                    sb.Append("pv=").Append(productVersion).Append(SEMI_COLON);
                    md5base.Append(productVersion).Append(SEMI_COLON);

                    FileInfo fileInfo = new FileInfo(location);
                    if (fileInfo.Exists)
                    {
                        sb.Append("size=").Append(fileInfo.Length).Append(NEW_LINE);
                        md5base.Append(fileInfo.Length).Append(NEW_LINE);
                    }
                    else
                    {
                        sb.Append(NEW_LINE);
                        md5base.Append(NEW_LINE);
                    }
                } catch (Exception ex) {
                    Cat.lastException = ex;
                }
            };
            componentVersions = sb.ToString();
            try
            {
                componentsChecksum = MD5Util.Compute(md5base.ToString());
            }
            catch (Exception ex)
            {
                Cat.lastException = ex;
            }
        }

        private string GetFileNameFromLocation(string location)
        {
            if (null == location)
                return "";

            int lastSlash = location.LastIndexOf("\\");
            if (lastSlash >= 0)
                return location.Substring(lastSlash + 1);
            else
                return location;
        }
    }
}