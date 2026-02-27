using System;
using System.Collections.Generic;
using System.Text;
using Org.Unidal.Cat.Message.Spi;

namespace Org.Unidal.Cat.Message.Internals
{
    class DefaultTaggedTransaction : DefaultTransaction, ITaggedTransaction
    {
        private string _mRootMessageId;

        private string _mParentMessageId;

        private string _mTag;

        public DefaultTaggedTransaction(string type, string name, string tag, IMessageManager manager)
            : base(type, name, manager)
        {
            _mTag = tag;
            Standalone = false;
            IMessageTree tree = manager.ThreadLocalMessageTree;
            if (tree != null)
            {
                _mRootMessageId = tree.RootMessageId;
                _mParentMessageId = tree.MessageId;
            }
        }

        public void Bind(string tag, string childMessageId, string title)
        {
            try
            {
                DefaultEvent evt = new DefaultEvent("RemoteCall", "Tagged");
                if (String.IsNullOrEmpty(title))
                {
                    title = Type + ":" + Name;
                }
                evt.AddData(childMessageId, title);
                evt.Timestamp = Timestamp;
                evt.Status = CatConstants.SUCCESS;
                evt.SetCompleted(true);
                AddChild(evt);
            }
            catch (Exception ex)
            {
                Cat.lastException = ex;
            }
        }

        public string ParentMessageId {get {return _mParentMessageId; } }

        public string RootMessageId { get { return _mRootMessageId; } }

        public string Tag { get { return _mTag; } }

        public void Start()
        {
            try
            {
                IMessageTree tree = Manager.ThreadLocalMessageTree;
                if (null != tree && tree.RootMessageId == null)
                {
                    tree.ParentMessageId = _mParentMessageId;
                    tree.RootMessageId = _mRootMessageId;
                }
            }
            catch (Exception ex)
            {
                Cat.lastException = ex;
            }
        }
    }
}
