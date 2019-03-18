using Org.Unidal.Cat.Util;
using System;
using System.Collections.Generic;
using Org.Unidal.Cat.Message.Spi;
using Org.Unidal.Cat.Message.Spi.Internals;

namespace Org.Unidal.Cat.Message.Internals
{
    public class DefaultTransaction : AbstractMessage, ITransaction
    {
        // private readonly Action<ITransaction> _endCallBack;
        private readonly IMessageManager _mManager;
        private IList<IMessage> _mChildren = new List<IMessage>();
        private long _mDurationInMicro; // must be less than 0

        public DefaultTransaction(String type, String name, IMessageManager manager /*Action<ITransaction> endCallBack*/)
            : base(type, name)
        {
            _mDurationInMicro = -1;
            //_endCallBack = endCallBack;
            _mManager = manager;
            Standalone = true;
        }

        #region ITransaction Members

        public IList<IMessage> Children
        {
            get { return _mChildren; }
        }

        public long DurationInMicros
        {
            get
            {
                try
                {
                    if (_mDurationInMicro >= 0)
                    {
                        return _mDurationInMicro;
                    }
                    else
                    {   // if it's not completed explicitly
                        long duration = 0;
                        int len = (_mChildren == null) ? 0 : _mChildren.Count;

                        if (len > 0)
                        {
                            if (_mChildren != null)
                            {
                                IMessage lastChild = _mChildren[len - 1];

                                if (lastChild is ITransaction)
                                {
                                    ITransaction trx = lastChild as ITransaction;

                                    duration = trx.Timestamp * 1000L + trx.DurationInMicros - TimestampInMicros;
                                }
                                else
                                {
                                    duration = lastChild.Timestamp * 1000L - TimestampInMicros;
                                }
                            }
                        }
                        return duration;
                    }
                }
                catch (Exception ex)
                {
                    Cat.lastException = ex;
                    return _mDurationInMicro;
                }
            }
            set { _mDurationInMicro = value; }
        }

        public IMessageManager Manager
        {
            get { return _mManager; }
        }

        public long DurationInMillis
        {
            get { return DurationInMicros/1000L; }
            set { _mDurationInMicro = value*1000L; }
        }

        public bool Standalone { get; set; }

        public ITransaction AddChild(IMessage message)
        {
            lock (_mChildren)
            {
                try
                {
                    _mChildren.Add(message);
                    return this;
                }
                catch (Exception ex)
                {
                    Cat.lastException = ex;
                    return this;
                }
            }
        }

        public override void Complete()
        {
            try
            {
                if (IsCompleted())
                {
                    // complete() was called more than once
                    IMessage evt0 = new DefaultEvent("cat", "BadInstrument") { Status = "TransactionAlreadyCompleted" };

                    evt0.Complete();
                    AddChild(evt0);
                }
                else
                {
                    _mDurationInMicro = MilliSecondTimer.UnixNowMicroSeconds() - TimestampInMicros;

                    SetCompleted(true);

                    if (_mManager != null)
                    {
                        _mManager.End(this);
                    }
                }
            }
            catch (Exception ex)
            {
                Cat.lastException = ex;
            }
        }

        public override void AddData(String keyValuePairs)
        {
            base.AddData(keyValuePairs);

            // For tranasctions, we need update estimated byte size when adding data.
            // For events, we do not need. Because we can update esitmated byte size when compeleting an event, which calls ctx.add(event).
            // TODO For forked transactions, we should not include their data length into the current main thread's context.
            if (_mManager is DefaultMessageManager)
            {
                var tree = Cat.GetThreadLocalMessageTree();
                if (null != tree)
                {
                    tree.EstimatedByteSize += (keyValuePairs == null ? 0 : keyValuePairs.Length);
                }
            }
        }

        public override void AddData(String key, Object value)
        {
            base.AddData(key, value);
            if (_mManager is DefaultMessageManager)
            {
                var tree = Cat.GetThreadLocalMessageTree();
                if (null != tree)
                {
                    // "+2" is for the "&" and "=" characters.
                    tree.EstimatedByteSize += (key == null ? 0 : key.Length) + (value == null ? 0 : value.ToString().Length) + 2;
                }
            }
        }

        public bool HasChildren()
        {
            return _mChildren.Count > 0;
        }
        #endregion
    }
}