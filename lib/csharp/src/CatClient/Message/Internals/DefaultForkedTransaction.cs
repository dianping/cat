using System;
using System.Collections.Generic;
using System.Text;
using Org.Unidal.Cat.Configuration;
using Org.Unidal.Cat.Message.Spi;

namespace Org.Unidal.Cat.Message.Internals
{
    public class DefaultForkedTransaction : DefaultTransaction, IForkedTransaction
    {
        private string _mRootMessageId;
        private string _mParentMessageId;
        private string _mForkedMessageId;

        public DefaultForkedTransaction(string type, string name, IMessageManager manager) : base(type, name, manager)
        {
            Standalone = false;
            
            IMessageTree tree = manager.ThreadLocalMessageTree;

            if (tree != null)
            {
                _mRootMessageId = tree.RootMessageId;
                _mParentMessageId = tree.MessageId;

                // Detach parent transaction and this forked transaction, by calling linkAsRunAway(), at this earliest moment,
                // so that thread synchronization is not needed at all between them in the future.
                _mForkedMessageId = Cat.CreateMessageId();
            }
        }

        public void Fork()
        {
            try
            {
                IMessageManager manager = base.Manager;

                manager.Setup();
                manager.Start(this, false);

                IMessageTree tree = manager.ThreadLocalMessageTree;

                if (tree != null)
                {
                    // Override tree.messageId to be forkedMessageId of current forked transaction, which is created in the parent thread.
                    tree.MessageId = _mForkedMessageId;
                    tree.RootMessageId = (_mRootMessageId == null ? _mParentMessageId : _mRootMessageId);
                    tree.ParentMessageId = _mParentMessageId;
                }
            }
            catch (Exception ex)
            {
                Cat.lastException = ex;
            }
        }

        public string ForkedMessageId {get {return _mForkedMessageId;}}
    }
}
