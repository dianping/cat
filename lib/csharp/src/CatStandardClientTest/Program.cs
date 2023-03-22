using Microsoft.Extensions.Configuration;
using Org.Unidal.Cat;
using Org.Unidal.Cat.Configuration;
using Org.Unidal.Cat.Message;
using System;

namespace CatStandardClientTest
{
    class Program
    {
        static void Main(string[] args)
        {
            IConfiguration config = new ConfigurationBuilder().AddJsonFile("appsettings.json", optional: false, reloadOnChange: true).Build();
            CatConfigurationSection.Load(config);

            SimpleTest();
            Test99Line();

            Console.ReadLine();
        }

        private static void SimpleTest()
        {
            var startTime = DateTime.Now;
            Console.WriteLine("Start: " + startTime);
            Console.WriteLine($"Top ThreadId: {System.Threading.Thread.CurrentThread.ManagedThreadId}");
            ITransaction newOrderTransaction = null;

            try
            {
                newOrderTransaction = Cat.NewTransaction("SimpleTestAsync-3-" + DateTime.Now.Ticks, "NewTrainOrder");

                newOrderTransaction.AddData("I am a detailed message");
                newOrderTransaction.AddData("another message");

                newOrderTransaction.Status = CatConstants.SUCCESS;
            }
            catch (Exception ex)
            {
                newOrderTransaction.SetStatus(ex);
            }
            finally
            {
                Console.WriteLine(newOrderTransaction.DurationInMillis);
                newOrderTransaction.Complete();
                Console.WriteLine("End: " + DateTime.Now);
                Console.WriteLine($"Duration: {(DateTime.Now - startTime).TotalMilliseconds}");
            }
        }

        private static void Test99Line()
        {
            var name = DateTime.Now.ToLongTimeString();

            ITransaction newOrderTransaction = null;

            for (int i = 1; i <= 950; i++)
            {
                try
                {
                    newOrderTransaction = Cat.NewTransaction("Line99Test", name);
                    newOrderTransaction.Status = CatConstants.SUCCESS;
                    System.Threading.Thread.Sleep(1);
                  
                }
                catch (Exception ex)
                {
                    newOrderTransaction.SetStatus(ex);
                }
                finally
                {
                    Console.WriteLine(i);
                    newOrderTransaction.Complete();
                }
            }

            for (int i = 951; i <= 1000; i++)
            {
                try
                {
                    newOrderTransaction = Cat.NewTransaction("Line99Test", name);
                    newOrderTransaction.Status = CatConstants.SUCCESS;
                    System.Threading.Thread.Sleep((i - 950) * 10);
                }
                catch (Exception ex)
                {
                    newOrderTransaction.SetStatus(ex);
                }
                finally
                {
                    Console.WriteLine(i);
                    newOrderTransaction.Complete();
                }
            }
        }
    }
}
