using System;
using System.Runtime.InteropServices;

namespace Org.Unidal.Cat.Util
{
    ///<summary>
    ///  This timer provides milli-second precise system time.
    ///</summary>
    public class MilliSecondTimer
    {
        private static long baseline = new DateTime(1970, 1, 1, 0, 0, 0).Ticks;

        public static long UnixNowMicroSeconds()	
        {
            return (DateTime.Now.ToUniversalTime().Ticks - baseline) / (TimeSpan.TicksPerMillisecond / 1000); // it's millisecond precise
        }

        public static long UnixNowMilliSeconds()
        {
            long unixTimestamp = (DateTime.Now.ToUniversalTime().Ticks - baseline) / TimeSpan.TicksPerMillisecond;
            return unixTimestamp;
        }

        public static long ToUnixMilliSeconds(DateTime dateTime)
        {
            long unixTimestamp = (dateTime.ToUniversalTime().Ticks - baseline) / TimeSpan.TicksPerMillisecond;
            return unixTimestamp;
        }

        public static long CurrentTimeHoursForJava()
        {
            TimeSpan ts = new TimeSpan(DateTime.UtcNow.Ticks - baseline);
            return ((long) ts.TotalMilliseconds/3600000L);
        }
    }

    public class HighResTicksProvider
    {
        private static long _f;

        [DllImport("kernel32.dll")]
        private static extern bool QueryPerformanceCounter([In, Out] ref long lpPerformanceCount);

        [DllImport("kernel32.dll")]
        private static extern bool QueryPerformanceFrequency([In, Out] ref long lpFrequency);

        /// <summary>
        ///   获得当前时间戳，十分之一微秒（100纳秒，和 DateTime.Now.Ticks 刻度一样）
        /// </summary>
        /// <returns> </returns>
        public static long GetTickCount()
        {
            long f = _f;

            if (f == 0)
            {
                if (QueryPerformanceFrequency(ref f))
                {
                    _f = f;
                }
                else
                {
                    _f = -1;
                }
            }

            if (_f == -1)
            {
                // fallback
                return DateTime.Now.Ticks;
            }

            long c = 0;
            QueryPerformanceCounter(ref c);

            return (long) (((double) c)*1000*10000/(f));
        }
    }
}