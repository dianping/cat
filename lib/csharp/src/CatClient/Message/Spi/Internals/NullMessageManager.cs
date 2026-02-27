using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Org.Unidal.Cat.Message.Spi;
using Org.Unidal.Cat.Configuration;
using System.Threading;

namespace Org.Unidal.Cat.Message.Internals
{
    class NullMessageManager : IMessageManager
    {
        private AbstractClientConfig _mClientConfig = new NullClientConfig();
        private IMessageTree messageTree = new NullMessageTree();
        private int seq;

        Configuration.AbstractClientConfig IMessageManager.ClientConfig
        {
            get { return _mClientConfig; }
        }

        IMessageTree IMessageManager.ThreadLocalMessageTree
        {
            get { return messageTree; }
        }

        bool IMessageManager.CatEnabled
        {
            get { return false; }
        }

        void IMessageManager.Add(IMessage message)
        {
        }

        void IMessageManager.Initialize(Configuration.AbstractClientConfig config)
        {
        }

        void IMessageManager.Reset()
        {
        }

        bool IMessageManager.HasContext()
        {
            return false;
        }

        void IMessageManager.Setup()
        {
        }

        void IMessageManager.Start(ITransaction transaction, bool forked)
        {
        }

        void IMessageManager.End(ITransaction transaction)
        {
        }

        void IMessageManager.Bind(string tag, string title)
        {
        }

        string IMessageManager.CreateMessageId()
        {
            return NullMessageTree.UNKNOWN + "-00000000-000000-" + Interlocked.Increment(ref seq);
        }

        public ITransaction PeekTransaction()
        {
            return new NullTransaction();
        }
    }
}
