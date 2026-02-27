﻿using System;
using System.IO;
using Org.Unidal.Cat;
using System.Threading;
using System.Collections.Generic;
using Org.Unidal.Cat.Message;
using System.Threading.Tasks;
using System.Linq;

namespace CatClientTest
{
    public class Context
    {
        public int Value { get; set; } = -1000;
    }

    public class Program
    {
        static AsyncLocal<Context> asyncLocal = new AsyncLocal<Context>();

        static void Main()
        {
            System.Threading.ThreadPool.SetMinThreads(5, 100);
            try
            {
                SimpleTest().GetAwaiter().GetResult();
            }
            finally
            {
                if (null != Cat.lastException)
                {
                    Console.WriteLine("Cat.lastException:\n" + Cat.lastException);
                }
                Console.WriteLine("Test ends successfully. Press any key to continue");
                Console.Read();
            }
        }

        private static async Task SimpleTest()
        {
            var startTime = DateTime.Now;
            Console.WriteLine("Start: " + startTime);
            Console.WriteLine($"Top ThreadId: {System.Threading.Thread.CurrentThread.ManagedThreadId}");
            ITransaction newOrderTransaction = null;

            try
            {
                newOrderTransaction = Cat.NewTransaction("SimpleTestAsync-" + DateTime.Now.Ticks, "NewTrainOrder");
                asyncLocal.Value = new Context { Value = -8 };

                newOrderTransaction.AddData("I am a detailed message");
                newOrderTransaction.AddData("another message");

                var tasks = Enumerable.Range(1, 5).Select(async (i) =>
                {
                    await InvokePaymentWrap(i).ConfigureAwait(false);
                });

                await Task.WhenAll(tasks);

                Console.WriteLine("//////////////////////////////");

                for (int i = 100; i < 103; i++)
                    await InvokePayment(i);

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

        private static async Task InvokePaymentWrap(int i)
        {
            var forkedTran = Cat.NewForkedTransaction("remote", "InvokePaymentWrap");
            asyncLocal.Value = new Context() { Value = i };
            try
            {
                await InvokePayment(i).ConfigureAwait(false);
                forkedTran.Status = CatConstants.SUCCESS;
            }
            catch (Exception ex)
            {
                forkedTran?.SetStatus(ex);
            }
            finally
            {
                forkedTran?.Complete();
            }
        }

        private static async Task InvokePayment(int i)
        {
            ITransaction paymentTransaction = null;
            try
            {
                Console.WriteLine($"{i} - InvokePayment 1 ThreadId: {System.Threading.Thread.CurrentThread.ManagedThreadId}. AsyncLocal: {asyncLocal.Value.Value}");
                paymentTransaction = Cat.NewTransaction("NewPayment" + i, "PaymentDetail");
                paymentTransaction.Status = CatConstants.SUCCESS;
                await Task.Delay(100).ConfigureAwait(false);
                Console.WriteLine($"{i} - InvokePayment 2 ThreadId: {System.Threading.Thread.CurrentThread.ManagedThreadId}. AsyncLocal: {asyncLocal.Value.Value}");

                await InvokeInnerPayment(i).ConfigureAwait(false);
            }
            catch (Exception ex)
            {
                paymentTransaction.SetStatus(ex);
            }
            finally
            {
                paymentTransaction.Complete();
            }
        }

        private static async Task InvokeInnerPayment(int i)
        {
            ITransaction paymentTransaction = null;
            try
            {
                Console.WriteLine($"{i} - InnerPayment 1 ThreadId: {System.Threading.Thread.CurrentThread.ManagedThreadId}. AsyncLocal: {asyncLocal.Value.Value}");
                paymentTransaction = Cat.NewTransaction("NewInnerPayment", "PaymentDetail-" + i);
                paymentTransaction.Status = CatConstants.SUCCESS;
                await Task.Delay(1000);
                Console.WriteLine($"{i} - InnerPayment 2 ThreadId: {System.Threading.Thread.CurrentThread.ManagedThreadId}. AsyncLocal: {asyncLocal.Value.Value}");

            }
            catch (Exception ex)
            {
                paymentTransaction.SetStatus(ex);
            }
            finally
            {
                paymentTransaction.Complete();
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
                    System.Threading.Thread.Sleep((i-950)*10);
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