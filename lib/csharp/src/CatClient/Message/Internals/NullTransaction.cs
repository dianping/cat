using System.Collections.Generic;

namespace Org.Unidal.Cat.Message.Internals
{
    public class NullTransaction : AbstractMessage, ITransaction, IForkedTransaction, ITaggedTransaction
    {
        private IList<IMessage> _mChildren;

        public NullTransaction() : base(null, null)
        {
        }

        #region ITransaction Members
        public IList<IMessage> Children
        {
            get { return _mChildren ?? (_mChildren = new List<IMessage>()); }
        }

        public long DurationInMicros
        {
            get { return 0; }
            set
            {
                //do nothing here
            }
        }

        public long DurationInMillis
        {
            get { return 0; }
            set
            {
                //do nothing here
            }
        }

        public bool Standalone
        {
            get { return true; }
            set
            {
                //do nothing here
            }
        }

        public ITransaction AddChild(IMessage message)
        {
            // do nothing here
            return this;
        }

        public override void Complete()
        {
            // do nothing here
        }

        public bool HasChildren()
        {
            return false;
        }
        #endregion

        #region IForkedTransaction Members
        public void Fork()
        {

        }
        #endregion

        public string ForkedMessageId { get {return null;} }

        #region ITaggedTransaction Members
        public void Bind(string tag, string childMessageId, string title) { }        

        public string ParentMessageId { get { return null; } }

        public string RootMessageId { get { return null; } }

        public string Tag { get { return null; } }

        public void Start() { }
        #endregion
    }
}