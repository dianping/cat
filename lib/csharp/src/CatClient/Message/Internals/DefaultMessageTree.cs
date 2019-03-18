using Org.Unidal.Cat.Message.Spi.Codec;
using System;
using Org.Unidal.Cat.Util;

namespace Org.Unidal.Cat.Message.Internals
{
    public class DefaultMessageTree : IMessageTree
    {
        private String _mMessageId;

        private String _mParentMessageId;

        private String _mRootMessageId;

        #region IMessageTree Members

        public IMessageTree Copy()
        {
            DefaultMessageTree tree = new DefaultMessageTree();

            tree.Domain = Domain;
            tree.HostName = HostName;
            tree.IpAddress = IpAddress;
            tree.MessageId = _mMessageId;
            tree.ParentMessageId = _mParentMessageId;
            tree.RootMessageId = _mRootMessageId;
            tree.SessionToken = SessionToken;
            tree.ThreadGroupName = ThreadGroupName;
            tree.ThreadId = ThreadId;
            tree.ThreadName = ThreadName;
            tree.Message = Message;
            tree.EstimatedByteSize = EstimatedByteSize;

            return tree;
        }

        public string Domain { get; set; }

        public string HostName { get { return NetworkInterfaceManager.HostName; } set { } } // set does nothing

        public string IpAddress { get { return NetworkInterfaceManager.HostIP; } set { } } // set does nothing

        public IMessage Message { get; set; }

        public String MessageId
        {
            get { return _mMessageId; }
            set
            {
                if (!string.IsNullOrEmpty(value))
                {
                    _mMessageId = value;
                }
            }
        }

        public String ParentMessageId
        {
            get { return _mParentMessageId; }
            set
            {
                if (!string.IsNullOrEmpty(value))
                {
                    _mParentMessageId = value;
                }
            }
        }

        public String RootMessageId
        {
            get { return _mRootMessageId; }
            set
            {
                if (!string.IsNullOrEmpty(value))
                {
                    _mRootMessageId = value;
                }
            }
        }

        public string SessionToken { get; set; }

        public string ThreadGroupName { get; set; }

        public string ThreadId { get; set; }

        public string ThreadName { get; set; }

        // Calculate byte size. If it is too big, take actions to prevent OOM.
        public int EstimatedByteSize { get; set;  }

        #endregion

        public override String ToString()
        {
            try
            {
                PlainTextMessageCodec codec = new PlainTextMessageCodec();
                ChannelBuffer buf = new ChannelBuffer(8192);

                codec.Encode(this, buf);

                buf.Reset();
                buf.Skip(4); // get rid of length

                return buf.ToString();
            }
            catch (Exception ex)
            {
                Cat.lastException = ex;
                return "";
            }
        }
    }
}