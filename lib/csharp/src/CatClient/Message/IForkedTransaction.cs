using System;
using System.Collections.Generic;
using System.Text;

namespace Org.Unidal.Cat.Message
{
    public interface IForkedTransaction : ITransaction
    {
        void Fork();

        String ForkedMessageId {get;}
    }
}
