using Org.Unidal.Cat.Configuration;
using Org.Unidal.Cat.Message.Spi;
using System.Collections.Generic;
using System.IO;
using System.Xml;
using Org.Unidal.Cat.Message.Spi.Internals;
using Org.Unidal.Cat.Message.Internals;
using Org.Unidal.Cat.Util;
using Org.Unidal.Cat.Message;
using System;
using System.Text;
using System.Threading;
using System.Globalization;

namespace Org.Unidal.Cat
{
    public class Cat
    {
        private static readonly Cat Instance = new Cat();

        private bool _mInitialized;

        private IMessageManager _mManager;

        private IMessageProducer _mProducer;

        public static Exception lastException = null;

        public static string lastMessage = null;

        static Cat() {
            // Explict initialization, in order to avoid lock contention in lazy initialization of CheckAndInitialize().
            Initialize();
        }

        public static bool Enabled { get; internal set; } = false;

        private static void CheckAndInitialize() {
            if (!Instance._mInitialized) 
            {
                lock (Instance) 
                {
                    if (!Instance._mInitialized)
                    {
                        Initialize();
                    }
                }
            }
        }

        // Keep GetManager() public for creating unused default/forked transaction.
        public static IMessageManager GetManager()
        {
            try
            {
                CheckAndInitialize();
                return Instance._mManager;
            }
            catch (Exception ex)
            {
                Cat.lastException = ex;
                return new NullMessageManager();
            }
        }

        private static IMessageProducer GetProducer()
        {
            try
            {
                CheckAndInitialize();
                return Instance._mProducer;
            }
            catch (Exception ex)
            {
                Cat.lastException = ex;
                return new NullMessageProducer();
            }
        }

        public static string Domain { 
            get {
                if (IsInitialized())
                    return Instance._mManager.ClientConfig.Domain.Id;
                else
                    return CatConstants.UNKNOWN_DOMAIN;
            } 
        }

        public static void Initialize()
        {
            try
            {
                if (Instance._mInitialized)
                {
                    return;
                }

                DefaultMessageManager manager = new DefaultMessageManager();

#if NETFULL
                var configFilePath = System.Configuration.ConfigurationManager.AppSettings[CatConstants.LOCAL_CLIENT_CONFIG];
#else
                var configFilePath = string.Empty;
#endif
                AbstractClientConfig config = new LocalClientConfig(configFilePath);

                manager.Initialize(config);
                Instance._mProducer = new DefaultMessageProducer(manager);
                Instance._mManager = manager;
                Instance._mInitialized = true;
            }
            catch (Exception ex) {
                Instance._mProducer = new NullMessageProducer();
                Instance._mManager = new NullMessageManager();
                Cat.lastException = ex;
                Instance._mInitialized = true;
            }
        }

        public static bool IsInitialized()
        {
            try { return Instance._mInitialized; }
            catch (Exception ex) { Cat.lastException = ex; return false; }
        }

        public static ITransaction NewTransaction(string type, string name)
        {
            try {
                var trans = Cat.GetProducer().NewTransaction(type, name);
                trans.Standalone = Cat.GetManager().PeekTransaction() != null;

                return trans;
            }
            catch (Exception ex) { Cat.lastException = ex; return new NullTransaction(); }
        }

        public static void Complete(object transaction)
        {
            try { if (null != transaction && transaction is ITransaction) ((ITransaction)transaction).Complete(); }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        public static void LogError(string message, Exception cause)
        {
            try {   Cat.GetProducer().LogError(message, cause); }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        public static void LogError(Exception cause)
        {
            try { Cat.GetProducer().LogError(null, cause); }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        public static void LogSizeEvent(string name, long size)
        {
            try {
                string scale = SizeUtils.GetSizeScale(size);
                Cat.GetProducer().LogEvent(name, scale, CatConstants.SUCCESS, "size=" + size);
            }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        public static IForkedTransaction NewForkedTransaction(String type, String name)
        {
            try {
                var forkTran = Cat.GetProducer().NewForkedTransaction(type, name);
                forkTran.Fork();
                return forkTran;
            }
            catch (Exception ex) { Cat.lastException = ex; return new NullTransaction(); }
        }

        public static ITaggedTransaction NewTaggedTransaction(String type, String name, String tag)
        {
            try { return Cat.GetProducer().NewTaggedTransaction(type, name, tag); }
            catch (Exception ex) { Cat.lastException = ex; return new NullTransaction(); }
        }

        public static void Bind(string tag, string title)
        {
            try { Cat.GetManager().Bind(tag, title); }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        public static void LogHeartbeat(String type, String name, String status, String nameValuePairs)
        {
            try { Cat.GetProducer().LogHeartbeat(type, name, status, nameValuePairs); }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        public static void LogMetricForCount(string name)
        {
            try
            {
                Cat.GetProducer().LogMetric(name, "C", "1");
            }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        public static void LogMetricForCount(string name, int quantity)
        {
            try
            {
                Cat.GetProducer().LogMetric(name, "C", quantity.ToString());
            }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        public static void LogMetricForDuration(string name, long durationInMillis)
        {
            try
            {
                Cat.GetProducer().LogMetric(name, "T", durationInMillis.ToString());
            }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        public static void LogMetricForSum(string name, double value)
        {
            try
            {
                // "{0:F2}" is consistent with String.format("%.2f", value) in Java
                Cat.GetProducer().LogMetric(name, "S", String.Format(CultureInfo.InvariantCulture, "{0:F2}",value));
            }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        public static void LogMetricForSum(string name, double sum, int quantity)
        {
            try
            {
                // "{0},{1:F2}" is consistent with String.format("%s,%.2f", quantity, sum) in Java
                Cat.GetProducer().LogMetric(name, "S,C", String.Format(CultureInfo.InvariantCulture, "{0},{1:F2}", quantity, sum));
            }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        public static void LogEvent(string type, string name, string status, IDictionary<string, string> nameValuePairs)
        {
            try
            {
                if (null != nameValuePairs && nameValuePairs.Count > 0)
                {
                    bool isFirst = true;
                    StringBuilder sb = new StringBuilder();
                    foreach (KeyValuePair<string, string> kvp in nameValuePairs)
                    {
                        sb.Append(isFirst ? "" : "&").Append(kvp.Key).Append("=").Append(kvp.Value);
                        isFirst = false;
                    }
                    Cat.GetProducer().LogEvent(type, name, status, sb.ToString());
                }
                else
                {
                    Cat.GetProducer().LogEvent(type, name, status, null);
                }
            }
            catch (Exception ex) { Cat.lastException = ex; }

        }

        public static void LogEvent(string type, string name, string status, string nameValuePairs)
        {
            try { Cat.GetProducer().LogEvent(type, name, status, nameValuePairs); }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        public static void LogEvent(string type, string name, string status)
        {
            try { Cat.GetProducer().LogEvent(type, name, status, null); }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        public static void LogEvent(string type, string name)
        {
            try { Cat.GetProducer().LogEvent(type, name, CatConstants.SUCCESS, null); }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        public static void Setup()
        {
            try { Cat.GetManager().Setup(); }
            catch (Exception ex) { Cat.lastException = ex; };
        }

        public static String CreateMessageId()
        {
            try { return Cat.GetManager().CreateMessageId(); }
            catch (Exception ex) { Cat.lastException = ex; return NullMessageTree.UNKNOWN_MESSAGE_ID; }
        }

        public static IMessageTree GetThreadLocalMessageTree() 
        {
            try { return Cat.GetManager().ThreadLocalMessageTree; }
            catch (Exception ex) { Cat.lastException = ex; return new NullMessageTree(); }
        }

        public static IHeartbeat NewHeartbeat(string type, string name)
        {
            try { return Cat.GetProducer().NewHeartbeat(type, name, null); }
            catch (Exception ex) { Cat.lastException = ex; return new NullHeartbeat(); }
        }

        public static string ToText()
        {
            try
            {
                string ret = "CAT Agent";
                var manager = Cat.GetManager();
                if (manager is DefaultMessageManager) {
                    ret += " Max queue size: " + ((DefaultMessageManager)manager).ClientConfig.MaxQueueSize;
                    ret += " Max queue byte size: " + ((DefaultMessageManager)manager).ClientConfig.MaxQueueByteSize;
                    ret += " Statistics: " + ((DefaultMessageManager)manager).Statistics;
                }
                return ret;
            }
            catch (Exception ex) { Cat.lastException = ex; return ""; }
        }
    }
}