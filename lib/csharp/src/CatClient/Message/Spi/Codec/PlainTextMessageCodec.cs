    using Org.Unidal.Cat.Message.Internals;
using Org.Unidal.Cat.Util;
using System;
using System.Text;
using System.Globalization;
using System.Collections.Generic;

namespace Org.Unidal.Cat.Message.Spi.Codec
{
    public class PlainTextMessageCodec : IMessageCodec
    {
        #region Policy enum

        public enum Policy
        {
            DEFAULT,

            WITHOUT_STATUS,

            WITH_DURATION
        }

        #endregion

        private const String ID = "PT1"; // plain text version 1

        private const byte TAB = (byte) '\t'; // tab character

        private const byte LF = (byte) '\n'; // line feed character

        private readonly BufferHelper _mBufferHelper;

        private readonly DateHelper _mDateHelper;

        public PlainTextMessageCodec()
        {
            _mBufferHelper = new BufferHelper();
            _mDateHelper = new DateHelper();
        }

        #region IMessageCodec Members

        public virtual IMessageTree Decode(ChannelBuffer buf)
        {
            DefaultMessageTree tree = new DefaultMessageTree();

            Decode(buf, tree);
            return tree;
        }

        public virtual void Decode(ChannelBuffer buf, IMessageTree tree)
        {

            // buf.ReadInt();// read body length

            DecodeHeader(buf, tree);

            if (buf.ReadableBytes() > 0)
            {
                DecodeMessage(buf, tree);
            }
        }

        public virtual void Encode(IMessageTree tree, ChannelBuffer buf)
        {
            int count = 0;

            buf.WriteInt(0); // place-holder
            count += EncodeHeader(tree, buf);

            if (tree.Message != null)
            {
                count += EncodeMessage(tree.Message, buf);
            }

            buf.SetInt(0, count);
        }

        #endregion

        protected internal void DecodeHeader(ChannelBuffer buf, IMessageTree tree)
        {
            BufferHelper helper = _mBufferHelper;
            String id = helper.Read(buf, TAB);
            String domain = helper.Read(buf, TAB);
            String hostName = helper.Read(buf, TAB);
            String ipAddress = helper.Read(buf, TAB);
            String threadGroupName = helper.Read(buf, TAB);
            String threadId = helper.Read(buf, TAB);
            String threadName = helper.Read(buf, TAB);
            String messageId = helper.Read(buf, TAB);
            String parentMessageId = helper.Read(buf, TAB);
            String rootMessageId = helper.Read(buf, TAB);
            String sessionToken = helper.Read(buf, LF);

            if (ID.Equals(id))
            {
                tree.Domain = domain;
                tree.HostName = hostName;
                tree.IpAddress = ipAddress;
                tree.ThreadGroupName = threadGroupName;
                tree.ThreadId = threadId;
                tree.ThreadName = threadName;
                tree.MessageId = messageId;
                tree.ParentMessageId = parentMessageId;
                tree.RootMessageId = rootMessageId;
                tree.SessionToken = sessionToken;
            }
            else
            {
                throw new Exception("Unrecognized id(" + id + ") for plain text message codec!");
            }
        }

        protected internal IMessage DecodeLine(ChannelBuffer buf, ITransaction parent,
                                               Stack<ITransaction> stack, IMessageTree tree)
        {
            BufferHelper helper = _mBufferHelper;
            byte identifier = buf.ReadByte();
            String timestamp = helper.Read(buf, TAB);
            String type = helper.Read(buf, TAB);
            String name = helper.Read(buf, TAB);

            if (identifier == 'E')
            {
                IMessage evt = new DefaultEvent(type, name);
                String status = helper.Read(buf, TAB);
                String data = helper.ReadRaw(buf, TAB);

                helper.Read(buf, LF); // get rid of line feed
                evt.Timestamp = _mDateHelper.Parse(timestamp);
                evt.Status = status;
                evt.AddData(data);

                if (parent != null)
                {
                    parent.AddChild(evt);
                    tree.EstimatedByteSize += evt.EstimateByteSize();
                    return parent;
                }
                return evt;
            }
            if (identifier == 'M')
            {
                DefaultMetric metric = new DefaultMetric(type, name);
                String status = helper.Read(buf, TAB);
                String data = helper.ReadRaw(buf, TAB);

                helper.Read(buf, LF); // get rid of line feed
                metric.Timestamp = _mDateHelper.Parse(timestamp);
                metric.Status = status;
                metric.AddData(data);

                if (parent != null)
                {
                    parent.AddChild(metric);
                    tree.EstimatedByteSize += metric.EstimateByteSize();
                    return parent;
                }
                return metric;
            }
            if (identifier == 'H')
            {
                IMessage heartbeat = new DefaultHeartbeat(type, name);
                String status0 = helper.Read(buf, TAB);
                String data1 = helper.ReadRaw(buf, TAB);

                helper.Read(buf, LF); // get rid of line feed
                heartbeat.Timestamp = _mDateHelper.Parse(timestamp);
                heartbeat.Status = status0;
                heartbeat.AddData(data1);

                if (parent != null)
                {
                    parent.AddChild(heartbeat);
                    tree.EstimatedByteSize += heartbeat.EstimateByteSize();
                    return parent;
                }
                return heartbeat;
            }
            if (identifier == 't')
            {
                IMessage transaction = new DefaultTransaction(type, name,
                                                              null);

                helper.Read(buf, LF); // get rid of line feed
                transaction.Timestamp = _mDateHelper.Parse(timestamp);

                if (parent != null)
                {
                    parent.AddChild(transaction);
                }

                stack.Push(parent);
                return transaction;
            }
            if (identifier == 'A')
            {
                ITransaction transaction2 = new DefaultTransaction(type, name, null);
                String status3 = helper.Read(buf, TAB);
                String duration = helper.Read(buf, TAB);
                String data4 = helper.ReadRaw(buf, TAB);

                helper.Read(buf, LF); // get rid of line feed
                transaction2.Timestamp = _mDateHelper.Parse(timestamp);
                transaction2.Status = status3;
                transaction2.AddData(data4);

                long d = Int64.Parse(duration.Substring(0, duration.Length - 2), NumberStyles.Integer);
                transaction2.DurationInMicros = d;

                if (parent != null)
                {
                    parent.AddChild(transaction2);
                    tree.EstimatedByteSize += transaction2.EstimateByteSize();
                    return parent;
                }
                return transaction2;
            }
            if (identifier == 'T')
            {
                String status5 = helper.Read(buf, TAB);
                String duration6 = helper.Read(buf, TAB);
                String data7 = helper.ReadRaw(buf, TAB);

                helper.Read(buf, LF); // get rid of line feed
                parent.Status = status5;
                parent.AddData(data7);

                long d8 = Int64.Parse(
                    duration6.Substring(0, duration6.Length - 2),
                    NumberStyles.Integer);
                parent.DurationInMicros = d8;
                tree.EstimatedByteSize += parent.EstimateByteSize();
                return stack.Pop();
            }
            Logger.Error("Unknown identifier(" + identifier + ") of message: " + buf);

            // unknown message, ignore it
            return parent;
        }

        protected internal void DecodeMessage(ChannelBuffer buf, IMessageTree tree)
        {
            Stack<ITransaction> stack = new Stack<ITransaction>();
            IMessage parent = DecodeLine(buf, null, stack, tree);

            tree.Message = parent;
            tree.EstimatedByteSize = parent.EstimateByteSize();

            while (buf.ReadableBytes() > 0)
            {
                IMessage message = DecodeLine(buf, (ITransaction) parent, stack, tree);

                if (message is ITransaction)
                {
                    parent = message;
                }
                else
                {
                    break;
                }
            }
        }

        protected internal int EncodeHeader(IMessageTree tree, ChannelBuffer buf)
        {
            BufferHelper helper = _mBufferHelper;
            int count = 0;

            count += helper.Write(buf, ID);
            count += helper.Write(buf, TAB);
            count += helper.Write(buf, tree.Domain);
            count += helper.Write(buf, TAB);
            count += helper.Write(buf, tree.HostName);
            count += helper.Write(buf, TAB);
            count += helper.Write(buf, tree.IpAddress);
            count += helper.Write(buf, TAB);
            count += helper.Write(buf, tree.ThreadGroupName);
            count += helper.Write(buf, TAB);
            count += helper.Write(buf, tree.ThreadId);
            count += helper.Write(buf, TAB);
            count += helper.Write(buf, tree.ThreadName);
            count += helper.Write(buf, TAB);
            count += helper.Write(buf, tree.MessageId);
            count += helper.Write(buf, TAB);
            count += helper.Write(buf, tree.ParentMessageId);
            count += helper.Write(buf, TAB);
            count += helper.Write(buf, tree.RootMessageId);
            count += helper.Write(buf, TAB);
            count += helper.Write(buf, tree.SessionToken);
            count += helper.Write(buf, LF);

            return count;
        }

        protected internal int EncodeLine(IMessage message, ChannelBuffer buf, char type, Policy policy)
        {
            BufferHelper helper = _mBufferHelper;
            int count = 0;

            count += helper.Write(buf, (byte) type);

            if (type == 'T' && message is ITransaction)
            {
                long duration = ((ITransaction) message).DurationInMillis;

                count += helper.Write(buf, _mDateHelper.Format2(message.Timestamp + duration));
            }
            else
            {
                count += helper.Write(buf, _mDateHelper.Format2(message.Timestamp));
            }

            count += helper.Write(buf, TAB);
            count += helper.Write(buf, message.Type);
            count += helper.Write(buf, TAB);
            count += helper.Write(buf, message.Name);
            count += helper.Write(buf, TAB);

            if (policy != Policy.WITHOUT_STATUS)
            {
                count += helper.Write(buf, message.Status);
                count += helper.Write(buf, TAB);

                Object data = message.Data;

                if (policy == Policy.WITH_DURATION && message is ITransaction)
                {
                    long duration0 = ((ITransaction) message).DurationInMicros;

                    count += helper.Write(buf, duration0.ToString(CultureInfo.InvariantCulture));
                    //以微秒为单位
                    count += helper.Write(buf, "us");
                    count += helper.Write(buf, TAB);
                }

                count += helper.WriteRaw(buf, data.ToString());
                count += helper.Write(buf, TAB);
            }

            count += helper.Write(buf, LF);

            return count;
        }

        public int EncodeMessage(IMessage message, ChannelBuffer buf)
        {
            if (message == null)
                return 0;

            if (message is IEvent)
            {
                return EncodeLine(message, buf, 'E', Policy.DEFAULT);
            }
            var transaction = message as ITransaction;
            if (transaction != null)
            {
                IList<IMessage> children = new List<IMessage>(transaction.Children);

                if ((children.Count == 0))
                {
                    return EncodeLine(transaction, buf, 'A', Policy.WITH_DURATION);
                }
                int count = 0;
                int len = children.Count;

                count += EncodeLine(transaction, buf, 't', Policy.WITHOUT_STATUS);

                for (int i = 0; i < len; i++)
                {
                    IMessage child = children[i];

                    count += EncodeMessage(child, buf);
                }

                count += EncodeLine(transaction, buf, 'T', Policy.WITH_DURATION);

                return count;
            }
            if (message is IHeartbeat)
            {
                return EncodeLine(message, buf, 'H', Policy.DEFAULT);
            }
            if (message is IMetric)
            {
                return EncodeLine(message, buf, 'M', Policy.DEFAULT);
            }
            throw new Exception("Unsupported message type: " + message.Type + ".");
        }

        #region Nested type: BufferHelper

        protected internal class BufferHelper
        {
            private readonly UTF8Encoding _mEncoding = new UTF8Encoding();

            public String Read(ChannelBuffer buf, byte separator)
            {
                int count = buf.BytesBefore(separator);

                if (count < 0)
                {
                    return null;
                }
                byte[] data = new byte[count];

                buf.ReadBytes(data);
                buf.ReadByte(); // get rid of separator

                return Encoding.UTF8.GetString(data);
            }

            public String ReadRaw(ChannelBuffer buf, byte separator)
            {
                int count = buf.BytesBefore(separator);

                if (count < 0)
                {
                    return null;
                }
                byte[] data = new byte[count];

                buf.ReadBytes(data);
                buf.ReadByte(); // get rid of separator

                int length = data.Length;

                for (int i = 0; i < length; i++)
                {
                    if (data[i] == '\\')
                    {
                        if (i + 1 < length)
                        {
                            byte b = data[i + 1];

                            if (b == 't')
                            {
                                data[i] = (byte) '\t';
                            }
                            else if (b == 'r')
                            {
                                data[i] = (byte) '\r';
                            }
                            else if (b == 'n')
                            {
                                data[i] = (byte) '\n';
                            }
                            else
                            {
                                data[i] = b;
                            }

                            Array.Copy(data, i + 2, data, i + 1, length - i - 2);
                            length--;
                        }
                    }
                }

                return Encoding.UTF8.GetString(data, 0, length);
            }

            public int Write(ChannelBuffer buf, byte b)
            {
                buf.WriteByte(b);
                return 1;
            }

            public int Write(ChannelBuffer buf, String str)
            {
                if (str == null)
                {
                    str = "null";
                }

                byte[] data = _mEncoding.GetBytes(str);

                buf.WriteBytes(data);
                return data.Length;
            }

            public int WriteRaw(ChannelBuffer buf, String str)
            {
                if (str == null)
                {
                    str = "null";
                }

                byte[] data = _mEncoding.GetBytes(str);

                int len = data.Length;
                int count = len;
                int offset = 0;

                for (int i = 0; i < len; i++)
                {
                    byte b = data[i];

                    if (b == '\t' || b == '\r' || b == '\n' || b == '\\')
                    {
                        buf.WriteBytes(data, offset, i - offset);
                        buf.WriteByte('\\');

                        if (b == '\t')
                        {
                            buf.WriteByte('t');
                        }
                        else if (b == '\r')
                        {
                            buf.WriteByte('r');
                        }
                        else if (b == '\n')
                        {
                            buf.WriteByte('n');
                        }
                        else
                        {
                            buf.WriteByte(b);
                        }

                        count++;
                        offset = i + 1;
                    }
                }

                if (len > offset)
                {
                    buf.WriteBytes(data, offset, len - offset);
                }

                return count;
            }
        }

        #endregion

        #region Nested type: DateHelper

        ///<summary>
        ///  Thread safe date helper class. DateFormat is NOT thread safe.
        ///</summary>
        protected internal class DateHelper
        {
            // This is slow.
            //public String Format(long timestamp)
            //{
            //    return new DateTime(timestamp*10000L).ToString("yyyy-MM-dd HH:mm:ss.fff");
            //}
            private static readonly long baseline = new DateTime(1970, 1, 1, 0, 0, 0).Ticks;

            public String Format2(long timestamp)
            {
                int year = DateTime.Now.Year;
                DateTime dt = new DateTime(timestamp * 10000L + baseline).ToLocalTime();
                char[] chars = new char[23];

                chars[0] = (char)((year / 1000) + '0');
                chars[1] = (char)((year % 1000) / 100 + '0');
                chars[2] = (char)((year % 100) / 10 + '0');
                chars[3] = (char)((year % 10) + '0');

                chars[4] = '-';
                
                int month = dt.Month;
                chars[5] = (char)((month / 10) + '0');
                chars[6] = (char)((month % 10) + '0');

                chars[7] = '-';

                int day = dt.Day;
                chars[8] = (char)((day / 10) + '0');
                chars[9] = (char)((day % 10) + '0');

                chars[10] = ' ';

                int hour = dt.Hour;
                chars[11] = (char)((hour / 10) + '0');
                chars[12] = (char)((hour % 10) + '0');

                chars[13] = ':';

                int min = dt.Minute;
                chars[14] = (char)((min / 10) + '0');
                chars[15] = (char)((min % 10) + '0');

                chars[16] = ':';

                int second = dt.Second;
                chars[17] = (char)((second / 10) + '0');
                chars[18] = (char)((second % 10) + '0');

                chars[19] = '.';

                int milliSecond = (dt.Millisecond);
                chars[20] = (char)((milliSecond / 100) + '0');
                chars[21] = (char)((milliSecond % 100) / 10 + '0');
                chars[22] = (char)((milliSecond % 10) + '0');

                return new string(chars);
            }

            public long Parse(String str)
            {
                DateTime dateTime = DateTime.ParseExact(str, "yyyy-MM-dd HH:mm:ss.fff", CultureInfo.CurrentCulture);

                return dateTime.Ticks/10000L;
            }
        }

        #endregion
    }
}