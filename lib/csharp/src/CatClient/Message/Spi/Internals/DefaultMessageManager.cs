using System.Globalization;
using Org.Unidal.Cat.Message.Internals;
using Org.Unidal.Cat.Message.Spi.IO;
using Org.Unidal.Cat.Util;
using Org.Unidal.Cat.Configuration;
using System;
using System.Collections.Generic;
using System.Threading;
using System.Linq;

namespace Org.Unidal.Cat.Message.Spi.Internals
{
    public class DefaultMessageManager : IMessageManager
    {
        // we don't use static modifier since MessageManager is a singleton in
        // production actually
        private readonly CatThreadLocal<Context> _mContext = new CatThreadLocal<Context>();

        private AbstractClientConfig _mClientConfig;

        private MessageIdFactory _mFactory;

        private bool _mFirstMessage = true;

        private TransactionHelper _mValidator;

        private IMessageSender _mSender;

        private IMessageStatistics _mStatistics;

        private StatusUpdateTask _mStatusUpdateTask;

        private LRUCache<String, ITaggedTransaction> _mTaggedTransactions;

        // TODO implement enableLogging(Logger logger)

        #region IMessageManager Members

        public virtual AbstractClientConfig ClientConfig
        {
            get { return _mClientConfig; }
        }

        public virtual IMessageTree ThreadLocalMessageTree
        {
            get
            {
                Context ctx = _mContext.Value;

                return ctx != null ? ctx.Tree : null;
            }
        }

        public IMessageStatistics Statistics { get {return _mStatistics;}  }

        public virtual void Reset()
        {
            // destroy current thread local data
            Context ctx = _mContext.Value;

            if (ctx != null)
            {
                // For accuracy of log view, we should not remove context with zero duration.
                if (ctx._mTotalDurationInMicros == 0)
                {
                    ctx.ClearStack();
                    _mContext.Remove();
                }
                ctx._mKnownExceptions.Clear();
            }
        }

        public DefaultMessageManager()
        {
            this._mValidator = new TransactionHelper(this);
        }

        public virtual void Initialize(AbstractClientConfig clientConfig)
        {
            _mClientConfig = clientConfig;
            _mStatistics = new DefaultMessageStatistics();

            _mFactory = new MessageIdFactory();
            // initialize domain and ip address
            _mFactory.Initialize(_mClientConfig.Domain.Id);

            _mSender = new TcpMessageSender(_mClientConfig, _mStatistics, _mFactory);
            _mSender.Initialize();

            _mStatusUpdateTask = new StatusUpdateTask(_mStatistics, _mClientConfig);

            _mTaggedTransactions = new LRUCache<string, ITaggedTransaction>(CatConstants.TAGGED_TRANSACTION_CACHE_SIZE);

            // start status update task
            Thread statusUpdateTask = new Thread(_mStatusUpdateTask.Run);
            statusUpdateTask.IsBackground = true;
            statusUpdateTask.Start();
            Logger.Info("Thread(StatusUpdateTask) started.");
        }

        public virtual bool HasContext()
        {
            return _mContext.Value != null;
        }

        public virtual bool CatEnabled
        {
            get { return _mClientConfig.Domain != null && _mClientConfig.Domain.Enabled && _mContext.Value != null; }
        }

        public virtual void Add(IMessage message)
        {
            Context ctx = GetContext();

            if (ctx != null)
            {
                ctx.Add(message);
            }
            else
                Logger.Warn("Context没取到");
        }

        public void Bind(string tag, string title)
        {
            ITaggedTransaction t = _mTaggedTransactions.Get(tag);
            if (t != null)
            {
                IMessageTree tree = ThreadLocalMessageTree;
                if (null != tree)
                {
                    t.Start();
                    t.Bind(tag, tree.MessageId, title);
                }
            }
        }

        // TODO tagged transaction binding
        // public override void bind(string tag, string title);

        public virtual void Setup()
        {
            Context ctx = new Context(this, _mClientConfig.Domain.Id);

            _mContext.Value = ctx;
        }

        public bool ShouldLog(Exception e)
        {
            Context ctx = _mContext.Value;
            if (ctx != null)
            {
                return ctx.ShouldLog(e);
            }
            else
            {
                return true;
            }
        }

        public virtual void Start(ITransaction transaction, bool forked)
        {
            // Catch all exceptions because Start could be used as public API
            try
            {
                Context ctx = GetContext();

                if (ctx != null)
                {
                    ctx.Start(transaction, forked);

                    if (transaction is ITaggedTransaction)
                    {
                        ITaggedTransaction tt = (ITaggedTransaction)transaction;
                        _mTaggedTransactions.Set(tt.Tag, tt);
                    }
                }
                else if (_mFirstMessage)
                {
                    _mFirstMessage = false;
                    Logger.Info("CAT client is not enabled because it's not initialized yet");
                }
                else
                    Logger.Warn("Failed to get current context to start a transaction with. transaction: " + transaction);
            }
            catch (Exception ex)
            {
                Cat.lastException = ex;
            }
        }

        public virtual void End(ITransaction transaction)
        {
            Context ctx = GetContext();

            if (ctx != null && (transaction.Standalone || transaction is DefaultTransaction))
            {
                if (ctx.End(transaction))
                {
                    _mContext.Remove();
                }
            }
        }

        #endregion

        public string CreateMessageId() 
        {
            return _mFactory.GetNextId();
        }

        public ITransaction PeekTransaction()
        {
            try
            {
                Context ctx = GetContext();
                if (null != ctx)
                {
                    return ctx.PeekTransaction();
                }
                else
                {
                    return new NullTransaction();
                }
            }
            catch (Exception ex)
            {
                Cat.lastException = ex;
                return new NullTransaction();
            }
        }

        internal void Flush(IMessageTree tree)
        {
            if (_mSender != null)
            {
                _mSender.Send(tree);

                if (_mStatistics != null)
                {
                    _mStatistics.OnSending(tree);
                }
                Reset();
            }
        }

        public void LinkAsRunAway(DefaultForkedTransaction transaction) {
            try
            {
                Context ctx = GetContext();
                if (ctx != null)
                {
                    _mValidator.LinkAsRunAway(transaction);
                }
            }
            catch (Exception ex)
            {
                Cat.lastException = ex;
            }
        }

        internal Context GetContext()
        {
            if (Cat.IsInitialized())
            {
                Context ctx = _mContext.Value;

                if (ctx != null)
                {
                    return ctx;
                }
                if (_mClientConfig.DevMode)
                {
                    throw new Exception(
                        "Cat has not been initialized successfully, please call Cal.setup(...) first for each thread.");
                }
            }

            return null;
        }

        internal String NextMessageId()
        {
            return _mFactory.GetNextId();
        }

        #region Nested type: Context

        internal class Context
        {
            private readonly IMessageTree _mTree;

            private readonly ThreadSafeStack<ITransaction> _mStack;

            public int _mLength;

            public long _mTotalDurationInMicros;

            public ISet<Exception> _mKnownExceptions;
            
            private readonly DefaultMessageManager _mManager;

            public Context(DefaultMessageManager manager, String domain)
            {
                _mManager = manager;

                _mTree = new DefaultMessageTree();
                _mStack = new ThreadSafeStack<ITransaction>();

                Thread thread = Thread.CurrentThread;
                String groupName = Thread.GetDomain().FriendlyName;

                _mTree.ThreadGroupName = groupName;
                _mTree.ThreadId = thread.ManagedThreadId.ToString();
                _mTree.ThreadName = thread.Name;

                _mTree.Domain = domain;
                // _mTree.HostName = hostName;
                // _mTree.IpAddress = ipAddress;

                _mLength = 1;

                _mKnownExceptions = new HashSet<Exception>();
            }

            public IMessageTree Tree
            {
                get { return _mTree; }
            }

            public ThreadSafeStack<ITransaction> Stack
            {
                get { return _mStack; }
            }

            /// <summary>
            ///   添加Event和Heartbeat
            /// </summary>
            /// <param name="manager"> </param>
            /// <param name="message"> </param>
            public void Add(IMessage message)
            {
                if ((_mStack.Count == 0))
                {
                    IMessageTree tree = _mTree.Copy();

                    if (String.IsNullOrWhiteSpace(tree.MessageId))
                    {
                        tree.MessageId = _mManager.NextMessageId();
                    }

                    tree.Message = message;
                    tree.EstimatedByteSize = message.EstimateByteSize();
                    _mTree.EstimatedByteSize = 0;
                    _mManager.Flush(tree);
                }
                else
                {
                    ITransaction parent = _mStack.Peek();
                    AddTransactionChild(message, parent);
                }
                Tree.EstimatedByteSize += message.EstimateByteSize();
            }

            private void AddTransactionChild(IMessage message, ITransaction transaction)
            {
                long treePeriod = trimToHour(_mTree.Message.Timestamp);
                long messagePeriod = trimToHour(message.Timestamp - 10 * 1000L);

                if (treePeriod < messagePeriod || _mLength >= _mManager._mClientConfig.Domain.MaxMessageSize)
                {
                    _mManager._mValidator.TruncateAndFlush(this, message.Timestamp);
                }

                transaction.AddChild(message);
                _mLength++;
            }

            private void AdjustForTruncatedTransaction(ITransaction root)
            {
                DefaultEvent next = new DefaultEvent("TruncatedTransaction", "TotalDuration");
                long actualDurationInMicros = _mTotalDurationInMicros + root.DurationInMicros;

                next.AddData(Convert.ToString(actualDurationInMicros));
                next.Status = CatConstants.SUCCESS;
                root.AddChild(next);

                _mTotalDurationInMicros = 0;
            }

            ///<summary>
            ///  return true means the transaction has been flushed.
            ///</summary>
            ///<param name="manager"> </param>
            ///<param name="transaction"> </param>
            ///<returns> true if message is flushed, false otherwise </returns>
            public bool End(ITransaction transaction)
            {
                lock (_mStack.SyncRoot)
                {
                    if (_mStack.Count != 0)
                    {
                        ITransaction current = _mStack.Peek();

                        if (transaction == current)
                        {
                            _mStack.Pop();
                            _mManager._mValidator.Validate(_mStack.Count == 0 ? null : _mStack.Peek(), current);
                        }
                        else
                        {
                            if (_mStack.FirstOrDefault(item => item == transaction) != null)
                            {
                                current = _mStack.Pop();
                                while (transaction != current && _mStack.Count != 0)
                                {
                                    _mManager._mValidator.Validate(_mStack.Peek(), current);

                                    current = _mStack.Pop();
                                }
                            }
                        }

                        if (_mStack.Count == 0)
                        {
                            IMessageTree tree = _mTree.Copy();
                            _mTree.MessageId = null;
                            _mTree.Message = null;
                            _mTree.EstimatedByteSize = 0;

                            if (_mTotalDurationInMicros > 0)
                            {
                                AdjustForTruncatedTransaction((ITransaction)tree.Message);
                            }

                            _mManager.Flush(tree);
                            return true;
                        }
                    }
                    return false;
                }
            }

            /// <summary>
            ///   返回stack的顶部对象
            /// </summary>
            /// <returns> </returns>
            public ITransaction PeekTransaction()
            {
                return (_mStack.Count == 0) ? null : _mStack.Peek();
            }

            public bool ShouldLog(Exception e)
            {
                if (null == _mKnownExceptions)
                {
                    _mKnownExceptions = new HashSet<Exception>();
                }

                if (_mKnownExceptions.Contains(e))
                {
                    return false;
                }
                else
                {
                    _mKnownExceptions.Add(e);
                    return true;
                }
            }

            /// <summary>
            ///   添加transaction
            /// </summary>
            /// <param name="manager"> </param>
            /// <param name="transaction"> </param>
            public void Start(ITransaction transaction, bool forked)
            {
                if (_mStack.Count != 0)
                {
                    // In the corresponding Java code, standAlone is NOT set to false here
                    // transaction.Standalone = false;

                    // Do NOT make strong reference from parent transaction to forked transaction.
                    // Instead, we create a "soft" reference to forked transaction later, via linkAsRunAway()
                    // By doing so, there is no need for synchronization between parent and child threads.
                    // Both threads can complete() anytime despite the other thread.
                    if (!(transaction is IForkedTransaction)) {
                        ITransaction parent = _mStack.Peek();
                        AddTransactionChild(transaction, parent);
                    }
                }
                else
                {
                    if (_mTree.MessageId == null)
                    {
                        _mTree.MessageId = _mManager.NextMessageId();
                    }
                    
                    _mTree.Message = transaction;
                }

                if (!forked)
                {

                    Tree.EstimatedByteSize += transaction.EstimateByteSize();
                    _mStack.Push(transaction);
                }
            }

            public void ClearStack()
            {
                if (null != _mStack && _mStack.Count > 0)
                {
                    _mStack.Clear();
                }
            }

            public override string ToString()
            {
                return "Context. thread: " + _mTree.ThreadId;
            }

            private long trimToHour(long timestamp)
            {
                return timestamp - timestamp % (3600 * 1000L);
            }
        }

        #endregion

        class TransactionHelper
        {
            private readonly DefaultMessageManager _mManager;

            public TransactionHelper(DefaultMessageManager manager)
            {
                _mManager = manager;
            }

            public void LinkAsRunAway(DefaultForkedTransaction transaction)
            {
                DefaultEvent evt = new DefaultEvent("RemoteCall", "RunAway");

                evt.AddData(transaction.ForkedMessageId, transaction.Type + ":" + transaction.Name);
                evt.Timestamp = transaction.Timestamp;
                evt.Status = CatConstants.SUCCESS;
                evt.SetCompleted(true);
                transaction.Standalone = true;

                _mManager.Add(evt);
            }

            public void MarkAsRunAway(ITransaction parent, DefaultTaggedTransaction transaction)
            {
                if (!transaction.HasChildren())
                {
                    transaction.AddData("RunAway");
                }
                transaction.Status = CatConstants.SUCCESS;
                transaction.Standalone = true;
                transaction.Complete();
            }

            public void markAsNotCompleted(DefaultTransaction transaction)
            {
                DefaultEvent evt = new DefaultEvent("cat", "BadInstrument");

                evt.Status = "TransactionNotCompleted";
                evt.SetCompleted(true);
                transaction.AddChild(evt);
                transaction.SetCompleted(true);
            }

            private void MigrateMessage(ThreadSafeStack<ITransaction> stack, ITransaction source, ITransaction target, int level)
            {
                // Note that stack.ToArray() gives an array reversed, which is the opposite of Java.
                ITransaction[] onStackTransactions = stack.ToArray();
                ITransaction current = (level < stack.Count ? onStackTransactions[stack.Count - 1 - level] : null);
                bool shouldKeep = false;

                foreach (IMessage child in source.Children)
                {
                    if (child != current)
                    {
                        target.AddChild(child);
                    }
                    else
                    {
                        DefaultTransaction cloned = new DefaultTransaction(current.Type, current.Name, _mManager);

                        cloned.Timestamp = current.Timestamp;
                        cloned.DurationInMicros = current.DurationInMicros;
                        cloned.AddData(current.Data);
                        cloned.Status = CatConstants.SUCCESS;

                        target.AddChild(cloned);
                        MigrateMessage(stack, current, cloned, level + 1);
                        shouldKeep = true;
                    }
                }

                lock (source.Children)
                {
                    source.Children.Clear();
                }

                if (shouldKeep)
                {
                    source.AddChild(current);
                }
            }

            public void TruncateAndFlush(Context ctx, long timestamp)
            {
                IMessageTree tree = ctx.Tree;
                ThreadSafeStack<ITransaction> stack = ctx.Stack;
                IMessage message = tree.Message;

                if (message is DefaultTransaction)
                {
                    string id = tree.MessageId;
                    string rootId = tree.RootMessageId;
                    string childId = _mManager.NextMessageId();
                    DefaultTransaction source = (DefaultTransaction)message;
                    DefaultTransaction target = new DefaultTransaction(source.Type, source.Name, _mManager);

                    target.Timestamp = source.Timestamp;
                    target.DurationInMicros = source.DurationInMicros;
                    target.AddData(source.Data);
                    target.Status = CatConstants.SUCCESS;

                    MigrateMessage(stack, source, target, 1);

                    int reducedByteSize = 0;
                    foreach (ITransaction transaction in stack)
                    {
                        DefaultTransaction tran = (DefaultTransaction)transaction;
                        tran.Timestamp = timestamp;
                        reducedByteSize += transaction.EstimateByteSize();
                    }

                    DefaultEvent next = new DefaultEvent("RemoteCall", "Next");
                    next.AddData(childId);
                    next.Status = CatConstants.SUCCESS;
                    target.AddChild(next);

                    IMessageTree t = tree.Copy();

                    t.Message = target;

                    ctx.Tree.MessageId = childId;
                    ctx.Tree.ParentMessageId = id;

                    ctx.Tree.RootMessageId = (rootId != null ? rootId : id);

                    ctx._mLength = stack.Count;
                    // Update estimated byte size of the truncated tree to be the total size of all on-stack transactions.
                    ctx.Tree.EstimatedByteSize = reducedByteSize;

                    ctx._mTotalDurationInMicros = ctx._mTotalDurationInMicros + target.DurationInMicros;
                    _mManager.Flush(t);
                }
            }

            //验证Transaction
            public void Validate(ITransaction parent, ITransaction transaction)
            {
                if (transaction.Standalone)
                {
                    IList<IMessage> children = transaction.Children;
                    int len = children.Count;
                    for (int i = 0; i < len; i++)
                    {
                        IMessage message = children[i];

                        var childTransaction = message as ITransaction;
                        if (childTransaction != null)
                        {
                            Validate(transaction, childTransaction);
                        }
                    }

                    if (!transaction.IsCompleted() && transaction is DefaultTransaction)
                    {
                        // missing transaction end, log a BadInstrument event so that
                        // developer can fix the code
                        this.markAsNotCompleted((DefaultTransaction)transaction);
                    }
                }
                else if (!transaction.IsCompleted())
                {
                    if (transaction is DefaultForkedTransaction)
                    {
                        this.LinkAsRunAway((DefaultForkedTransaction)transaction);
                    }
                    else if (transaction is DefaultTaggedTransaction)
                    {
                        this.MarkAsRunAway(parent, (DefaultTaggedTransaction)transaction);
                    }
                }
            }
        }
    }
}