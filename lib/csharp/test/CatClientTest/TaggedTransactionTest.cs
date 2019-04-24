using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Org.Unidal.Cat.Message;
using Org.Unidal.Cat;
using System.Threading;

namespace CatClientTest
{
    internal class TaggedTransactionTest
    {
        public static void Test()
        {
            ITransaction t = Cat.NewTransaction("TaggedRoot C", "Root");
            Cat.NewTaggedTransaction("TaggedChild C", "Child1", "Tag1");
            Cat.NewTaggedTransaction("TaggedChild C", "Child2", "Tag2");

            Thread thread1 = new Thread(TaggedWork.DoWork);
            Thread thread2 = new Thread(TaggedWork.DoWork);

            thread1.Start(new TaggedParams(500, "Tag1"));
            thread2.Start(new TaggedParams(100, "Tag2"));

            Thread.Sleep(200);

            t.Status = CatConstants.SUCCESS;
            t.Complete();

            //thread1.Join();
            //thread2.Join();
        }
    }

    class TaggedWork
    {
        public static void DoWork(Object obj)
        {
            var parameters = (TaggedParams)obj;
            var tag = parameters._mTag;
            ITransaction t = Cat.NewTransaction("TaggedThread", tag);

            try
            {
                Thread.Sleep(parameters._mTimeout);
                // Cat.LogEvent("TaggedRunnable", "Timeout." + parameters._mTimeout);
                t.Status = CatConstants.SUCCESS;
                Cat.Bind(tag, "Child Tagged Thread");
            }
            catch (Exception e)
            {
                Cat.LogError(e);
                t.SetStatus(e);
            }
            finally
            {
                t.Complete();
            }
        }
    }

    class TaggedParams
    {
        public string _mTag;
        public int _mTimeout;

        public TaggedParams(int timeout, string tag)
        {
            this._mTag = tag;
            this._mTimeout = timeout;
        }
    }
}
