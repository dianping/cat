using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Org.Unidal.Cat.Message.Internals
{
    class NullMessageTree : IMessageTree
    {
        public const string UNKNOWN = "Unknown";
        public const string UNKNOWN_MESSAGE_ID = UNKNOWN + "-00000000-000000-0";

        private ITransaction transaction = new NullTransaction();

        string IMessageTree.Domain
        {
            get
            {
                return UNKNOWN;
            }
            set
            {
            }
        }

        string IMessageTree.HostName
        {
            get
            {
                return UNKNOWN;
            }
            set
            {
            }
        }

        string IMessageTree.IpAddress
        {
            get
            {
                return "0.0.0.0";
            }
            set
            {
            }
        }

        IMessage IMessageTree.Message
        {
            get
            {
                return transaction;
            }
            set
            {
            }
        }

        string IMessageTree.MessageId
        {
            get
            {
                return UNKNOWN_MESSAGE_ID;
            }
            set
            {
            }
        }

        string IMessageTree.ParentMessageId
        {
            get
            {
                return UNKNOWN_MESSAGE_ID;
            }
            set
            {
            }
        }

        string IMessageTree.RootMessageId
        {
            get
            {
                return UNKNOWN_MESSAGE_ID;
            }
            set
            {
            }
        }

        string IMessageTree.SessionToken
        {
            get
            {
                return UNKNOWN;
            }
            set
            {
            }
        }

        string IMessageTree.ThreadGroupName
        {
            get
            {
                return UNKNOWN;
            }
            set
            {
            }
        }

        string IMessageTree.ThreadId
        {
            get
            {
                return "0";
            }
            set
            {
            }
        }

        string IMessageTree.ThreadName
        {
            get
            {
                return UNKNOWN;
            }
            set
            {
            }
        }

        public int EstimatedByteSize { get; set; }

        IMessageTree IMessageTree.Copy()
        {
            return this;
        }
    }
}
