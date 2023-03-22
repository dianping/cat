using System;
using System.Collections.Generic;
using System.Text;

namespace Org.Unidal.Cat.Message
{
    public interface ITaggedTransaction : ITransaction
    {
        void Bind(String tag, String childMessageId, String title);

        string ParentMessageId {get;}

        string RootMessageId {get;}

        string Tag {get;}

        void Start();
    }
}
