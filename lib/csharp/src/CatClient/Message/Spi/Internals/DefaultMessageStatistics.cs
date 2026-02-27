namespace Org.Unidal.Cat.Message.Spi.Internals
{
    public class DefaultMessageStatistics : IMessageStatistics
    {
        #region IMessageStatistics Members

        public long Produced { get; set; }

        public long Overflowed { get; set; }

        public long BytesOverflowed { get; set; }

        public long Bytes { get; set; }

        public void OnSending(IMessageTree tree)
        {
            Produced++;
        }

        public void OnOverflowed(IMessageTree tree)
        {
            Overflowed++;
        }

        public void OnBytesOverflowed(IMessageTree tree)
        {
            BytesOverflowed++;
        }

        public void OnBytes(int size)
        {
            Bytes += size;
        }

        #endregion

        public override string ToString()
        {
            return "Produced[" + Produced + "] Overflowed[" + Overflowed + "] Bytes[" + Bytes + "]";
        }
    }
}