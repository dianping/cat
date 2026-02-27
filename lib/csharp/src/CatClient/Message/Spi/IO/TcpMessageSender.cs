using System;
using Org.Unidal.Cat.Configuration;
using Org.Unidal.Cat.Message.Spi.Codec;
using Org.Unidal.Cat.Util;
using System.Collections.Generic;
using System.Net.Sockets;
using System.Threading;
using System.Collections.Concurrent;
using Org.Unidal.Cat.Message.Spi.Internals;
using Org.Unidal.Cat.Message.Internals;

namespace Org.Unidal.Cat.Message.Spi.IO
{
    public class TcpMessageSender : IMessageSender
    {
        private const int MAX_ATOMIC_MESSAGES = 200;

        private readonly AbstractClientConfig _mClientConfig;
        private IList<Server> serversFromRemoteConfig;
        private readonly IMessageCodec _mCodec;
        private readonly BlockingThreadSafeQueue<IMessageTree> _mQueue;
        private readonly BlockingThreadSafeQueue<IMessageTree> _mAtomicTrees;
        private readonly IMessageStatistics _mStatistics;
        private bool _mActive;
        private TcpClient _mActiveChannel;
        private int _mErrors;
        private MessageIdFactory _messageIdFactory;
        private ChannelBuffer buf = new ChannelBuffer(8192);

        public TcpMessageSender(AbstractClientConfig clientConfig, IMessageStatistics statistics, MessageIdFactory messageIdFactory)
        {
            _mClientConfig = clientConfig;
            _mStatistics = statistics;
            _mActive = true;
            _mQueue = new BlockingThreadSafeQueue<IMessageTree>();
            _mAtomicTrees = new BlockingThreadSafeQueue<IMessageTree>(MAX_ATOMIC_MESSAGES);
            _mCodec = new PlainTextMessageCodec();
            _messageIdFactory = messageIdFactory;
        }

        ~TcpMessageSender()
        {
            try
            {
                if (null != _mActiveChannel)
                {
                    _mActiveChannel.Close();
                    _mActiveChannel = null;
                }
            }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        #region IMessageSender Members

        public virtual bool HasSendingMessage
        {
            get { return _mQueue.Count > 0; }
        }

        public void Initialize()
        {
            // Load server list from remote config
            serversFromRemoteConfig = _mClientConfig.Servers;
            Logger.Info("Max Queue Size: " + _mClientConfig.MaxQueueSize);
            Logger.Info("Max Queue Byte Size: " + _mClientConfig.MaxQueueByteSize);

            // Connect to the server
            _mActiveChannel = GetActiveConnection(_mClientConfig.Servers, serversFromRemoteConfig);

            // Do not occupty threads in the global ThreadPool, in which the number of threads is limited.
            // ThreadPool.QueueUserWorkItem(ChannelManagementTask);
            Thread channelManagementThread = new Thread(ChannelManagementTask);
            channelManagementThread.IsBackground = true;
            channelManagementThread.Start();
            Logger.Info("Thread(TcpMessageSender-ChannelManagementTask) started.");

            Thread asynchronousSendThread = new Thread(AsynchronousSendTask);
            asynchronousSendThread.IsBackground = true;
            asynchronousSendThread.Start();
            Logger.Info("Thread(TcpMessageSender-AsynchronousSendTask) started.");

            Thread mergeAtomicThread = new Thread(MergeAtomicTask);
            mergeAtomicThread.IsBackground = true;
            mergeAtomicThread.Start();
            Logger.Info("Thread(TcpMessageSender-MergeAtomicTask) started.");
        }

        // Create one TCP connection either to a server obtained from remote config (preferred), or to a server obtained from local config.
        private TcpClient GetActiveConnection(IList<Server> staticServers, IList<Server> serversFromRemoteConfig)
        {
            if (serversFromRemoteConfig != null)
            {
                foreach (Server server in serversFromRemoteConfig)
                {
                    if (IsConnectedToSameServer(_mActiveChannel, server))
                    {
                        return _mActiveChannel;
                    }
                    TcpClient channel = CreateChannel(server);
                    if (channel != null && channel.Connected)
                    {
                        Logger.Info("Connected to " + server + " successfully, as one server from remote config");
                        return channel;
                    }
                }
            }

            if (staticServers != null)
            {
                foreach (Server server in staticServers)
                {
                    if (IsConnectedToSameServer(_mActiveChannel, server))
                    {
                        return _mActiveChannel;
                    }
                    TcpClient channel = CreateChannel(server);
                    if (channel != null && channel.Connected)
                    {
                        Logger.Info("Connected to " + server + " successfully, as one server from local config");
                        return channel;
                    }
                }
            }

            Logger.Error("Cannot get any active TCP connection from servers: " + serversFromRemoteConfig + "; " + staticServers);
            return null;
        }

        private static bool IsConnectedToSameServer(TcpClient currentClient, Server newServer)
        {
            try
            {
                if (currentClient != null && currentClient.Connected)
                {
                    var newServerEndPoint = newServer.Ip + ":" + newServer.Port;
                    if (newServerEndPoint  == currentClient.Client.RemoteEndPoint.ToString())
                    {
                        return true;
                    }
                }
                return false;
            }
            catch (Exception ex) { Cat.lastException = ex; return false; }
        }

        private bool isAtomicMessage(IMessageTree tree) {
            IMessage message = tree.Message;

		    if (message is ITransaction) {
                String type = message.Type;

                return (type != null &&
                    (type.StartsWith("Cache.") || type.StartsWith("Redis") || type.StartsWith("Memcached")
                    || "SQL" == type));
		    } else {
			    return true;
		    }
        }

        public void Send(IMessageTree tree)
        {
            if (isAtomicMessage(tree))
            {
                if (_mAtomicTrees.Count >= _mClientConfig.MaxQueueSize)
                {
                    LogQueueOverflow(tree);
                }
                else if (_mAtomicTrees.EstimatedByteSize >= _mClientConfig.MaxQueueByteSize)
                {
                    LogQueueBytesOverflow(tree);
                }
                else
                {
                    // if (_mAtomicTrees.Count < _mClientConfig.MaxQueueSize && _mAtomicTrees.EstimatedByteSize < _mClientConfig.MaxQueueByteSize)
                    _mAtomicTrees.Enqueue(tree);
                }
            }
            else
            {
                if (_mQueue.Count >= _mClientConfig.MaxQueueSize)
                {
                    LogQueueOverflow(tree);
                }
                else if (_mQueue.EstimatedByteSize >= _mClientConfig.MaxQueueByteSize)
                {
                    LogQueueBytesOverflow(tree);
                }
                else
                {
                    // if (_mQueue.Count < _mClientConfig.MaxQueueSize && _mQueue.EstimatedByteSize < _mClientConfig.MaxQueueByteSize)
                    _mQueue.Enqueue(tree);
                }
            }
        }

        private void LogQueueOverflow(IMessageTree tree)
        {
            if (_mStatistics != null)
            {
                _mStatistics.OnOverflowed(tree);
            }

            // throw it away since the queue is full
            Interlocked.Add(ref _mErrors, 1);

            if (_mErrors % 10000 == 0)
            {
                Logger.Warn("Can't send message to cat-server because max queue size is reached ! Count: " + _mErrors);
            }
        }

        private void LogQueueBytesOverflow(IMessageTree tree)
        {
            if (_mStatistics != null)
            {
                _mStatistics.OnBytesOverflowed(tree);
            }

            // throw it away since the queue is full
            Interlocked.Add(ref _mErrors, 1);

            if (_mErrors % 10000 == 0)
            {
                Logger.Warn("Can't send message to cat-server because max queue byte size is reached ! Count: " + _mErrors);
            }
        }

        public void Shutdown()
        {
            _mActive = false;

            try
            {
                if (_mActiveChannel != null && _mActiveChannel.Connected)
                {
                    _mActiveChannel.Close();
                }
            }
            catch (Exception ex) { Cat.lastException = ex; }
        }

        #endregion

        public void ChannelManagementTask(object o)
        {
            int count = 0;
            while (true)
            {
                // For every 5 minutes
                try
                {
                    _messageIdFactory.SaveMark(true);
                    if (_mActive)
                    {
                        // 1. If TCP connection is down, try to reconnect.
                        if (_mActiveChannel == null || !_mActiveChannel.Connected)
                        {
                            _mClientConfig.Refresh();
                            serversFromRemoteConfig = _mClientConfig.Servers;
                            if (null != _mActiveChannel)
                            {
                                Logger.Warn("The current TCP connection to " + _mActiveChannel + " is no longer connected. Will try to re-connect to server.");
                            }

                            var channel = GetActiveConnection(_mClientConfig.Servers, serversFromRemoteConfig);
                            if (channel != null && channel != _mActiveChannel)
                            {
                                var lastChannel = _mActiveChannel;
                                _mActiveChannel = channel;
                                if (lastChannel != null)
                                {
                                    lastChannel.Close();
                                }
                                NetworkInterfaceManager.Refresh();
                            }
                        }
                        // 2.If TCP connection is up, rebalance by connecting to a better CAT server, if necessary. 
                        else
                        {
                            // Rebalance step (1): for every one hour, refresh router config from CAT server.
                            if (count % (CatConstants.REFRESH_ROUTER_CONFIG_INTERVAL / CatConstants.TCP_RECONNECT_INTERVAL) == 0)
                            {
                                Logger.Info("Refreshing router config from CAT server");
                                _mClientConfig.Refresh();
                            }
                            // Rebalance step (2): for every 10 min, forcefully re-establish TCP connection, according to latest router config
                            if (count % (CatConstants.TCP_REBALANCE_INTERVAL / CatConstants.TCP_RECONNECT_INTERVAL) == 0)
                            {
                                serversFromRemoteConfig = _mClientConfig.Servers;
                                var channel = GetActiveConnection(_mClientConfig.Servers, serversFromRemoteConfig);
                                if (channel != null && channel != _mActiveChannel)
                                {
                                    var lastChannel = _mActiveChannel;
                                    _mActiveChannel = channel;
                                    if (lastChannel != null)
                                    {
                                        if (lastChannel.Client != null && channel.Client != null)
                                        {
                                            Logger.Info("Rebalancing by re-establising TCP connection. Old connection to: "
                                                + lastChannel.Client.RemoteEndPoint + " New connection to: " + channel.Client.RemoteEndPoint);
                                        }
                                        lastChannel.Close();
                                    }
                                    NetworkInterfaceManager.Refresh();
                                }
                            }
                        }
                    }
                }
                catch (Exception ex) { Cat.lastException = ex; }
                finally { count++; }
                Thread.Sleep(CatConstants.TCP_RECONNECT_INTERVAL);
            }
        }

        public void AsynchronousSendTask(object o)
        {
            try
            {
                while (true)
                {
                    if (_mActive)
                    {
                        if (_mActiveChannel == null || !_mActiveChannel.Connected)
                        {
                            Logger.Warn("TCP connection is not available." + _mActiveChannel);
                            Thread.Sleep(CatConstants.TCP_CHECK_INTERVAL);
                            continue;
                        }

                        try
                        {
                            IMessageTree tree;
                            _mQueue.TryDequeue(out tree, true);
                            if (tree != null)
                            {
                                SendInternal(tree);
                                tree = null;
                            };
                        }
                        catch (Exception ex) { 
                            Cat.lastException = ex; 
                        }
                    } else {
                        Thread.Sleep(CatConstants.TCP_CHECK_INTERVAL);
                    }
                }
            }
            catch (Exception ex) { Cat.lastException = ex; }
        }


        private void SendInternal(IMessageTree tree)
        {
            if (_mActiveChannel != null && _mActiveChannel.Connected)
            {
                // Re-use the existing buffer.
                buf.Reset();

                _mCodec.Encode(tree, buf);

                // Nullify reference as early as possible
                tree.Message = null;

                byte[] data = buf.ToArray();

                var sendCount = _mActiveChannel.Client.Send(data);

                if (_mStatistics != null)
                {
                    _mStatistics.OnBytes(data.Length);
                }
            }
            else
            {
                tree.Message = null;
                Logger.Warn("SendInternal中，Socket关闭");
            }
        }

        private static TcpClient CreateChannel(Server server)
        {
            if (!server.Enabled)
            {
                return null;
            }

            TcpClient socket = new TcpClient();

            socket.NoDelay = true;
            socket.ReceiveTimeout = 2*1000; // 2 seconds

            string ip = server.Ip;
            int port = server.Port;

            // Logger.Info("Connecting to server({0}:{1}) ...", ip, port);

            try
            {
                socket.Connect(ip, port);

                if (socket.Connected)
                {
                    // Logger.Info("Connected to server({0}:{1}).", ip, port);

                    return socket;
                }
                Logger.Error("Failed to connect to server({0}:{1}).", ip, port);
            }
            catch (Exception e)
            {
                Logger.Error(
                    "Failed to connect to server({0}:{1}). Error: {2}.",
                    ip,
                    port,
                    e.Message
                    );
                Cat.lastException = e;
                if (null != socket)
                {
                    try { socket.Close(); }
                    catch (Exception ex) { Cat.lastException = ex; };
                }
            }

            return null;
        }

        private bool ShouldMerge(BlockingThreadSafeQueue<IMessageTree> trees)
        {
            IMessageTree tree;
            trees.TryPeek(out tree, true);

            if (tree != null)
            {
                long firstTime = tree.Message.Timestamp;

                // 30 sec
                const int maxDuration = 1000 * 30;

                if (MilliSecondTimer.UnixNowMilliSeconds() - firstTime > maxDuration 
                    || trees.Count >= MAX_ATOMIC_MESSAGES 
                    || trees.Count >= _mClientConfig.MaxQueueSize
                    || trees.EstimatedByteSize >= _mClientConfig.MaxQueueByteSize)
                {
                    return true;
                }
            }
            return false;
        }

        private void MergeAtomicTask(object o) {
            while (true)
            {
                try
                {
                    // ShouldMerge will wait on _mAtomicTrees.TryPeek() if the queue is not full enough.
                    if (ShouldMerge(_mAtomicTrees))
                    {
                        IMessageTree tree = MergeTree(_mAtomicTrees);
                        if (null == tree)
                        {
                            continue;
                        }
                        else if (_mQueue.Count >= _mClientConfig.MaxQueueSize)
                        {
                            LogQueueOverflow(tree);
                        }
                        else if (_mQueue.EstimatedByteSize >= _mClientConfig.MaxQueueByteSize)
                        {
                            LogQueueBytesOverflow(tree);
                        }
                        else
                        {
                            // if (_mQueue.Count < _mClientConfig.MaxQueueSize && _mQueue.EstimatedByteSize < _mClientConfig.MaxQueueByteSize)
                            _mQueue.Enqueue(tree);
                        }
                    }
                }
                catch (Exception ex)
                {
                    Cat.lastException = ex;
                    Thread.Sleep(200);
                }
            }
        }

        private IMessageTree MergeTree(BlockingThreadSafeQueue<IMessageTree> trees)
        {
		    int max = MAX_ATOMIC_MESSAGES - 1;
		    DefaultTransaction tran = new DefaultTransaction("_CatMergeTree", "_CatMergeTree", null);
		    tran.Status = CatConstants.SUCCESS;
		    tran.SetCompleted(true);
            
            IMessageTree first;
            trees.TryPeek(out first, false);

            // Usually this should not happen, because it is in the same thread with ShouldMerge()
            if (first == null)
            {
                return null;
            }

            // Set merge tree start time to the first massage's timestamp
            tran.Timestamp = first.Message.Timestamp;

            long lastTimestamp = 0;
            long lastDuration = 0;
            int estimatedByteSize = 0;
		    while (max >= 0) {
			    IMessageTree tree;
                trees.TryDequeue(out tree, false);

                if (tree != null)
                {
                    tran.AddChild(tree.Message);
                    estimatedByteSize += tree.EstimatedByteSize;

                    if (first!=tree)
                    {
                        _messageIdFactory.Reuse(tree.MessageId);
                    }

                    lastTimestamp = tree.Message.Timestamp;
                    if (tree.Message is DefaultTransaction)
                    {
                        lastDuration = ((DefaultTransaction)tree.Message).DurationInMillis;
                    }
                    else
                    {
                        lastDuration = 0;
                    }
                }

			    if (tree == null || max == 0) {
                    // Set merge tree end time to the last massage's end time
                    tran.DurationInMillis = (lastTimestamp - tran.Timestamp + lastDuration);
                    break;
			    }
			    max--;
		    }
		    ((DefaultMessageTree) first).Message = tran;
            estimatedByteSize += tran.EstimateByteSize();
            first.EstimatedByteSize = estimatedByteSize;
		    return first;
	    }
    }
}