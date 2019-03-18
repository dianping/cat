using System;
using System.IO;
using System.Security;
using System.Security.Permissions;
using System.Xml;

namespace Org.Unidal.Cat.Util
{
    /// <summary>
    ///   简单记录Cat客户端的启动日志
    /// </summary>
    public class Logger
    {
        private static StreamWriter _mWriter;
        private static string _mLastPath;
        private static object _mWiterLock = new object();
        private static bool _mInitialized;
        private static bool _mLogEnable;
        private static string _mDomain;

        public static void Initialize(string domain, bool logEnable)
        {
            if (_mInitialized)
            {
                return;
            }
            _mDomain = domain;
            _mLogEnable = logEnable;

            _mInitialized = true;
        }

        public static void Debug(string pattern, params object[] args)
        {
            Log("DEBUG", pattern, args);
        }

        public static void Info(string pattern, params object[] args)
        {
            Log("INFO", pattern, args);
        }

        public static void Warn(string pattern, params object[] args)
        {
            Log("WARN", pattern, args);
        }

        public static void Error(string pattern, params object[] args)
        {
            Log("ERROR", pattern, args);
        }

        public static void Fatal(string pattern, params object[] args)
        {
            Log("FATAL", pattern, args);
        }

        private static void Log(string severity, string pattern, params object[] args)
        {
            if (!_mLogEnable)
            {
                return;
            }
            lock (_mWiterLock)
            {
                try
                {
                    string timestamp = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss.fff");
                    string message = string.Format(pattern, args);
                    string line = "[" + timestamp + "] [" + severity + "] " + message;

                    //StreamWriter writer = GetWriter();
                    if (_mWriter != null)
                    {
                        _mWriter.WriteLine(line);
                        _mWriter.Flush();
                    }
                    else
                    {
                        Console.WriteLine(line);
                    }
                }
                catch (Exception e)
                {
                    Cat.lastException = e;
                }
            }
        }

        private static StreamWriter GetWriter()
        {
            string path = DateTime.Now.ToString("yyyyMMdd");
            if (!path.Equals(_mLastPath))
            {
                if (_mWriter != null)
                {
                    try
                    {
                        _mWriter.Close();
                    }
                    catch (Exception e)
                    {
                        Console.WriteLine(e.Message);
                        Cat.lastException = e;
                    }
                }

                string logFile = "cat_" + _mDomain + "_" + path + ".log";

                try
                {
                    if (Directory.Exists(CatConstants.CAT_HOME_TEMP) && isWritable(CatConstants.CAT_HOME_TEMP))
                    {
                        _mWriter = new StreamWriter(Path.Combine(CatConstants.CAT_HOME_TEMP, logFile), true);
                        Console.WriteLine("Logger file " + Path.Combine(CatConstants.CAT_HOME_TEMP, logFile));
                    }
                    else if (Directory.Exists(CatConstants.CAT_HOME) && isWritable(CatConstants.CAT_HOME))
                    {
                        _mWriter = new StreamWriter(Path.Combine(CatConstants.CAT_HOME, logFile), true);
                        Console.WriteLine("Logger file " + Path.Combine(CatConstants.CAT_HOME, logFile));
                    }
                }
                catch (Exception e)
                {
                    Console.WriteLine("Error when openning log file: " + e.Message + " " + e.StackTrace + ".");
                    Cat.lastException = e;
                }
            }

            _mLastPath = path;
            return _mWriter;
        }

        private static bool isWritable(string filename)
        {
#if NETFULL
            var permissionSet = new PermissionSet(PermissionState.None);
            var writePermission = new FileIOPermission(FileIOPermissionAccess.Write, filename);
            permissionSet.AddPermission(writePermission);

            if (permissionSet.IsSubsetOf(AppDomain.CurrentDomain.PermissionSet))
            {
                return true;
            }
            else
            {
                return false;
            }
#else
            return false;
#endif
        }
    }
}