using System;
using System.Text;
using Org.Unidal.Cat.Message.Spi.Codec;
using Org.Unidal.Cat.Util;

namespace Org.Unidal.Cat.Message.Internals
{
    public abstract class AbstractMessage : IMessage
    {
        private String _mName;
        private String _mType;
        private bool _mCompleted;
        private StringBuilder _mData;

        private String _mStatus = "unset";

        protected AbstractMessage(String type, String name)
        {
            _mType = type;
            _mName = name;
            TimestampInMicros = MilliSecondTimer.UnixNowMicroSeconds();
        }

        /// <summary>
        ///   其实是Ticks除以10
        /// </summary>
        protected long TimestampInMicros { get; private set; }

        #region IMessage Members

        public String Data
        {
            get { return _mData == null ? "" : _mData.ToString(); }
        }

        public String Name
        {
            get { return _mName; }
            set { _mName = value ?? CatConstants.NULL_STRING; }
        }

        public String Status
        {
            get { return _mStatus; }

            set
            {
                _mStatus = (value == null) ? "unset" : (value.Length > 64 ? value.Substring(0, 64) : value);
            }
        }

        /// <summary>
        ///   其实是Ticks除以10000
        /// </summary>
        public long Timestamp
        {
            get { return TimestampInMicros/1000L; }
            set { TimestampInMicros = value*1000L; }
        }

        public String Type
        {
            get { return _mType; }
            set { _mType = value ?? CatConstants.NULL_STRING; }
        }

        public virtual void AddData(String keyValuePairs)
        {
            try
            {
                // No meaning to add null as data.
                if (null == keyValuePairs)
                    return;

                if (_mData == null)
                {
                    _mData = new StringBuilder(keyValuePairs);
                }
                else
                {
                    _mData.Append(keyValuePairs);
                }
            }
            catch (Exception ex)
            {
                Cat.lastException = ex;
            }
        }

        public virtual void AddData(String key, Object value)
        {
            try
            {
                if (_mData == null)
                {
                    _mData = new StringBuilder();
                }
                else if (_mData.Length > 0)
                {
                    _mData.Append('&');
                }

                _mData.Append(key).Append('=').Append(value);
            }
            catch (Exception ex)
            {
                Cat.lastException = ex;
            }
        }

        public virtual void Complete()
        {
            SetCompleted(true);
        }

        public bool IsCompleted()
        {
            return _mCompleted;
        }

        public bool IsSuccess()
        {
            return CatConstants.SUCCESS.Equals(_mStatus);
        }

        public void SetStatus(Exception e)
        {
            if (null != e)
            {
                _mStatus = e.GetType().FullName;
            }
        }

        #endregion

        public void SetCompleted(bool completed)
        {
            _mCompleted = completed;
        }

        public override String ToString()
        {
            try
            {
                PlainTextMessageCodec codec = new PlainTextMessageCodec();
                ChannelBuffer buf = new ChannelBuffer(8192);

                codec.EncodeMessage(this, buf);
                buf.Reset();

                return buf.ToString();
            }
            catch (Exception ex)
            {
                Cat.lastException = ex;
                return "";
            }
        }

        public int EstimateByteSize()
        {
            return (Type == null ? 0 : Type.Length)
                + (Name == null ? 0 : Name.Length)
                + (Data == null ? 0 : Data.Length);
                //+ (_mStatus == null ? 0 : _mStatus.Length);
        }
    }
}