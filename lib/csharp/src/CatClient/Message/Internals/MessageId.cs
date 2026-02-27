using System.Globalization;
using System;
using System.Collections.Generic;
using System.Text;

namespace Org.Unidal.Cat.Message.Internals
{
    [Obsolete]
    public class MessageId
    {
        private readonly String _mDomain;

        private readonly int _mIndex;

        private readonly String _mIpAddressInHex;

        private readonly long _mTimestamp;

        internal MessageId(String domain, String ipAddressInHex, long timestamp, int index)
        {
            _mDomain = domain;
            _mIpAddressInHex = ipAddressInHex;
            _mTimestamp = timestamp;
            _mIndex = index;
        }

        public String Domain
        {
            get { return _mDomain; }
        }

        public int Index
        {
            get { return _mIndex; }
        }

        public String IpAddressInHex
        {
            get { return _mIpAddressInHex; }
        }

        public long Timestamp
        {
            get { return _mTimestamp; }
        }

        private static MessageId Parse(String messageId)
        {
            IList<String> list = messageId.Split('-');
            int len = list.Count;

            if (len >= 4)
            {
                String ipAddressInHex = list[len - 3];
                long timestamp = (Int64.Parse(list[len - 2], NumberStyles.Integer));
                int index = Int32.Parse(list[len - 1]);
                String domain;

                if (len > 4)
                {
                    // allow domain contains '-'
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < len - 3; i++)
                    {
                        if (i > 0)
                        {
                            sb.Append('-');
                        }

                        sb.Append(list[i]);
                    }

                    domain = sb.ToString();
                }
                else
                {
                    domain = list[0];
                }

                return new MessageId(domain, ipAddressInHex, timestamp, index);
            }

            throw new Exception("Invalid message id format: " + messageId);
        }

        public override String ToString()
        {
            try
            {
                StringBuilder sb = new StringBuilder(_mDomain.Length + 32);

                sb.Append(_mDomain);
                sb.Append('-');
                sb.Append(_mIpAddressInHex);
                sb.Append('-');
                sb.Append(_mTimestamp);
                sb.Append('-');
                sb.Append(_mIndex);

                return sb.ToString();
            }
            catch (Exception ex)
            {
                Cat.lastException = ex;
                return "";
            }
        }
    }
}