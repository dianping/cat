using System.Text;
using System.IO;

namespace Org.Unidal.Cat.Message.Spi.Codec
{
    public class ChannelBuffer
    {
        private readonly MemoryStream _mBuf;

        private BinaryWriter _mWriter;

        public MemoryStream Buf {
            get {
                return _mBuf;
            }
        }

        public ChannelBuffer(int capacity)
        {
            _mBuf = new MemoryStream(capacity);
            _mWriter = new BinaryWriter(_mBuf, Encoding.UTF8);
        }

        /// <summary>
        ///   从当前位置到目标字符第一次出现的位置有多少字节?
        /// </summary>
        /// <param name="separator"> </param>
        /// <returns> </returns>
        public int BytesBefore(byte separator)
        {
            int count = 0;
            long oldPosition = _mBuf.Position;

            while (_mBuf.Position < _mBuf.Length)
            {
                int b = _mBuf.ReadByte();

                if (b == -1)
                {
                    return -1;
                }
                if ((byte) b == separator)
                {
                    _mBuf.Position = oldPosition;
                    return count;
                }

                count++;
            }

            _mBuf.Position = oldPosition;
            return 0;
        }

        public void Skip(int bytes)
        {
            _mBuf.Position += bytes;
        }

        public int ReadableBytes()
        {
            return (int) (_mBuf.Length - _mBuf.Position);
        }

        public int ReadBytes(byte[] data)
        {
            return _mBuf.Read(data, 0, data.Length);
        }

        public byte ReadByte()
        {
            return (byte) (_mBuf.ReadByte() & 0xFF);
        }

        public void WriteByte(byte b)
        {
            _mWriter.Write(b);
        }

        public void WriteByte(char c)
        {
            _mWriter.Write((byte) (c & 0xFF));
        }

        public int ReadInt()
        {
            byte[] bytes = new byte[4];
            _mBuf.Read(bytes, 0, 4);
            return FromBytes(bytes);
        }

        public void WriteInt(int i)
        {
            _mWriter.Write(ToBytes(i));
        }

        public void WriteBytes(byte[] data)
        {
            _mWriter.Write(data);
        }

        public void WriteBytes(byte[] data, int offset, int len)
        {
            _mWriter.Write(data, offset, len);
        }

        // for test purpose
        public void Reset()
        {
            _mBuf.Seek(0, SeekOrigin.Begin);
            _mBuf.SetLength(0);
            _mWriter = new BinaryWriter(_mBuf, Encoding.UTF8);
        }

        /// <summary>
        ///   在流的相应位置插入一个整数的字节(覆盖？)
        /// </summary>
        /// <param name="index"> </param>
        /// <param name="i"> </param>
        public void SetInt(int index, int i)
        {
            _mWriter.Seek(index, SeekOrigin.Begin);
            _mWriter.Write(ToBytes(i));
        }

        private static byte[] ToBytes(int value)
        {
            byte[] bytes = new byte[4];

            bytes[3] = (byte) value;
            bytes[2] = (byte) (value >> 8);
            bytes[1] = (byte) (value >> 16);
            bytes[0] = (byte) (value >> 24);
            return bytes;
        }

        private static int FromBytes(byte[] bytes)
        {
            int value;
            value = bytes[3];
            value = (bytes[2] << 8) | value;
            value = (bytes[1] << 16) | value;
            value = (bytes[0] << 24) | value;
            return value;
        }

        public byte[] ToArray()
        {
            return _mBuf.ToArray();
        }

        /// <summary>
        ///   从当前位置到结尾的字节数组的字符串表示
        /// </summary>
        /// <returns> </returns>
        public override string ToString()
        {
            byte[] data = _mBuf.ToArray();
            int offset = (int) _mBuf.Position;
            string str = Encoding.UTF8.GetString(data, offset, data.Length - offset);

            //ToArray本身就不为该Position，所以下一行代码多余
            //_mBuf.Seek(offset, SeekOrigin.Begin);
            return str;
        }
    }
}