using System;
using System.Collections.Generic;
using System.Text;
using System.Net;
using System.IO;
using System.Threading;
using System.Threading.Tasks;
using System.Security.Cryptography;
using System.Linq;

namespace Org.Unidal.Cat.Configuration
{
    public abstract class AbstractClientConfig
    {
        private IList<Server> _mServers = new List<Server>();
        private Domain _mDomain;
        protected const int DEFAULT_MAX_QUEUE_SIZE = 1000;
        protected const int DEFAULT_MAX_QUEUE_BYTE_SIZE = 256 * 1024 * 1024;
        private int _mMaxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
        private int _mMaxQueueByteSize = DEFAULT_MAX_QUEUE_BYTE_SIZE;
        private static Lazy<RNGCryptoServiceProvider> rnd = new Lazy<RNGCryptoServiceProvider>(() => new RNGCryptoServiceProvider());

        /// <summary>
        ///   是否是开发模式
        /// </summary>
        public bool DevMode { get; set; }

        public Domain Domain
        {
            get { return _mDomain; }
            set { _mDomain = value; }
        }

        /// <summary>
        ///   Cat日志服务器，可以有多个
        /// </summary>
        public IList<Server> Servers
        {
            get { return _mServers; }
            set { _mServers = value; }
        }

        public int MaxQueueSize
        {
            get { return _mMaxQueueSize; }
            set { _mMaxQueueSize = value; }
        }

        public int MaxQueueByteSize
        {
            get { return _mMaxQueueByteSize; }
            set { _mMaxQueueByteSize = value; }
        }

        /// <summary>
        /// 是否采用客户端随机方式实现负载均衡；开启后，会对服务端获取的服务器列表进行随机打乱
        /// </summary>
        public bool UseClientLoadBalace { get; set; } = false;

        public void Refresh()
        {
            var servers = GetCatTcpServers(true);
            if (servers.Count > 0)
                this.Servers = servers;
        }

        protected abstract string GetCatRouterServiceURL(bool sync);

        public abstract string GetConfigHeartbeatMessage();

        protected IList<Server> GetCatTcpServers(bool sync = false)
        {
            IList<Server> servers = new List<Server>();

            try
            {
                var remoteConfig = GetCatTcpServerList(sync);
                if (String.IsNullOrWhiteSpace(remoteConfig))
                {
                    return servers;
                }
                var tokens = remoteConfig.Split(new char[] { ';' });

                foreach (string token in tokens)
                {
                    if (String.IsNullOrWhiteSpace(token))
                    {
                        continue;
                    }
                    var trimmedToken = token.Trim();

                    var addressAndPort = trimmedToken.Split(new char[] { ':' });
                    if (addressAndPort.Length == 0)
                    {
                        continue;
                    }

                    int port = 2280;
                    try
                    {
                        if (addressAndPort.Length >= 2)
                        {
                            port = Convert.ToInt32(addressAndPort[1]);
                        }
                    }
                    catch (Exception ex)
                    {
                        Cat.lastException = ex;
                        continue;
                    }

                    var httpPort = addressAndPort[0] == "127.0.0.1" ? 2281 : 8080;
                    Server server = new Server(addressAndPort[0], port, httpPort);
                    servers.Add(server);
                }

                if (UseClientLoadBalace && servers.Count > 0)
                {
                    servers = servers.OrderBy(x => GetNextInt32()).ToList();
                }
            }
            catch (Exception ex)
            { Cat.lastException = ex; }

            return servers;
        }

        private String GetCatTcpServerList(bool sync)
        {
            // 1. First get the url of CAT router service (/cat/s/router)
            var catRouterUrl = GetCatRouterServiceURL(sync);

            // 2. Send http request to CAT router serivce, in order to get CAT server for this specific domain, as part of load-balancing.
            if (!String.IsNullOrWhiteSpace(catRouterUrl))
            {
                var request = (HttpWebRequest)HttpWebRequest.Create(catRouterUrl + "?domain=" + Domain.Id);
                request.Timeout = 2000;
                request.ReadWriteTimeout = 2000;
                request.KeepAlive = false;

                using (HttpWebResponse response = (HttpWebResponse)request.GetResponse())
                {
                    using (Stream stream = response.GetResponseStream())
                    using (StreamReader sr = new StreamReader(stream))
                    {
                        string resultstring = sr.ReadToEnd();
                        return resultstring;
                    }
                }
            }
            else
                return null;
        }

        private static int GetNextInt32()
        {
            byte[] randomInt = new byte[4];
            rnd.Value.GetBytes(randomInt);
            return Convert.ToInt32(randomInt[0]);
        }
    }
}
