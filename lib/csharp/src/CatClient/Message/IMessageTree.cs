using System;

namespace Org.Unidal.Cat.Message
{
    public interface IMessageTree
    {
        String Domain { get; set; }


        String HostName { get; set; }


        String IpAddress { get; set; }


        IMessage Message { get; set; }


        String MessageId { get; set; }


        String ParentMessageId { get; set; }


        String RootMessageId { get; set; }


        String SessionToken { get; set; }


        String ThreadGroupName { get; set; }


        String ThreadId { get; set; }


        String ThreadName { get; set; }

        int EstimatedByteSize { get; set; }

        IMessageTree Copy();
    }
}