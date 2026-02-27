using System;

namespace Org.Unidal.Cat.Message.Internals
{
    public class DefaultHeartbeat : AbstractMessage, IHeartbeat
    {
        public DefaultHeartbeat(String type, String name) : base(type, name)
        {
        }
    }
}