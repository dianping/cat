using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using Org.Unidal.Cat;

namespace CatClientTest.PerformanceTest
{
    class MetricTest
    {
        public static void Test()
        {
            //TestLogMetricForCount();
            //TestLogMetricForSum();

            // TestLogMetricForDuration();
            TestLogMetricForSum();
        }

        private static void TestLogMetricForCount()
        {
            for (int i = 0; i < 10; i++)
            {
                Cat.LogMetricForCount("my-count1-from-dotnet");
                Console.WriteLine("my-count1-from-dotnet: time: " + DateTime.Now + " value: " + 1);
                Thread.Sleep(1000);
            }
        }

        private static void TestLogMetricForCount2()
        {
            for (int i = 0; i < 10; i++)
            {
                int value = 100;
                Cat.LogMetricForCount("my-count2-from-dotnet", value);
                Console.WriteLine("my-count2-from-dotnet: time: " + DateTime.Now + " value: "  + value);
                Thread.Sleep(1000);
            }
        }

        private static void TestLogMetricForDuration()
        {
            for (int i = 0; i < 10; i++)
            {
                int value = 100;
                Cat.LogMetricForDuration("my-duration-from-dotnet", value);
                Console.WriteLine("my-duration-from-dotnet: time: " + DateTime.Now + " value: " + value);
                Thread.Sleep(1000);
            }
        }

        private static void TestLogMetricForSum()
        {
            double value = 10;
            // Cat.LogMetricForSum("redis.bb.read", value, 1);
            Cat.LogMetricForSum("redis.bb.read", 0);
            Console.WriteLine("redis.bb.read: time: " + DateTime.Now + " value: " + value);
            Thread.Sleep(1000);
        }

        private static void TestLogMetricForSum2()
        {
            for (int i = 0; i < 10; i++)
            {
                double sum = 27.389234;
                int quantity = 10;
                Cat.LogMetricForSum("my-quantity-sum2-from-dotnet", sum, quantity);
                Console.WriteLine("my-quantity-sum2-from-dotnet: time: " + DateTime.Now + " quantity: " + quantity + " sum: " + sum);
                Thread.Sleep(1000);
            }
        }
    }
}
