namespace Org.Unidal.Cat.Message.Internals
{
    public class NullHeartbeat : AbstractMessage, IHeartbeat
    {
        public NullHeartbeat() : base(null, null)
        {
        }
    }
}