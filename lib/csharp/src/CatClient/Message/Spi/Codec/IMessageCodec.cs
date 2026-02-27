namespace Org.Unidal.Cat.Message.Spi.Codec
{
    public interface IMessageCodec
    {
        IMessageTree Decode(ChannelBuffer buf);

        void Decode(ChannelBuffer buf, IMessageTree tree);

        void Encode(IMessageTree tree, ChannelBuffer buf);
    }
}