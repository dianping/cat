using Org.Unidal.Cat.Configuration;
using Org.Unidal.Cat.Message.Spi;
using System;
using System.Linq;
using System.Collections.Generic;
using System.IO;
using System.Xml;
using Org.Unidal.Cat.Message.Spi.Internals;
using Org.Unidal.Cat.Util;

namespace Org.Unidal.Cat.Configuration
{
    // CAT clienet config, which is loaded from a local XML file.
    class LocalClientConfig : AbstractClientConfig
    {
        public LocalClientConfig(string configFile)
        {
            Init(configFile);
            var servers = base.GetCatTcpServers(true);
            if (servers.Count > 0)
            {
                Servers = servers;
            }
        }

        protected override string GetCatRouterServiceURL(bool sync)
        {
            // TODO need to try multiple servers here.
            if (Servers.Count > 0)
            {
                Server server = Servers[0];
                // http://192.168.183.100:8080/cat/s/router
                return "http://" + server.Ip + ":" + server.HttpPort + "/cat/s/router";
            }
            else
                return null;
        }

        private string GetDomainId()
        {
            return this.Domain.Id;
        }

        private void Init(string configFile)
        {
            if (!String.IsNullOrWhiteSpace(configFile) && File.Exists(configFile))
            {
                XmlDocument doc = new XmlDocument();

                doc.Load(configFile);

                XmlElement root = doc.DocumentElement;

                if (root != null)
                {
                    this.MaxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
                    this.MaxQueueByteSize = GetMaxQueueByteSize(root);
                    this.Domain = BuildDomain(root.GetElementsByTagName("domain"));
                    bool logEnable = BuildLogEnabled(root.GetElementsByTagName("logEnabled"));
                    Logger.Initialize(this.Domain.Id, logEnable);
                    Logger.Info("Use config file({0}).", configFile);

                    IEnumerable<Server> servers = BuildServers(root.GetElementsByTagName("servers"));

                    //NOTE: 只添加Enabled的
                    Servers = new List<Server>();
                    foreach (Server server in servers.Where(server => server.Enabled))
                    {
                        Servers.Add(server);
                        Logger.Info("CAT server configured: {0}:{1}", server.Ip, server.Port);
                    }
                }
            }
            else
            {
                var catConfig = Configuration.CatConfigurationSection.CatConfig;
                if (catConfig == null)
                {
                    throw new Exception("Please initialize CAT Configuration via CatConfigurationSection::(IConfiguration config)");
                }

                this.Domain = new Domain() { Id = catConfig.Domain.Id.Trim(), Enabled = catConfig.Domain.Enabled, MaxMessageSize = catConfig.Domain.MaxMessageSize };
                this.UseClientLoadBalace = catConfig.Domain.UseClientLoadBalance;
                bool logEnable = catConfig.LogEnabled.Enabled;
                Logger.Initialize(this.Domain.Id, logEnable);

                IEnumerable<Server> servers = catConfig.Servers.OfType<Configuration.ServerElement>().Where(s => s.Enabled)
                                                       .Select(s => new Server(s.Ip, s.Port, s.HttpPort) { Enabled = s.Enabled });
                //NOTE: 只添加Enabled的
                Servers = new List<Server>();
                foreach (Server server in servers)
                {
                    Servers.Add(server);
                    Logger.Info("CAT server configured: {0}:{1}", server.Ip, server.Port);
                }
            }

            Cat.Enabled = this.Domain != null && !string.IsNullOrEmpty(this.Domain.Id) && this.Domain.Enabled;
            Logger.Info("CAT server turned on: " + Cat.Enabled);
        }

        private int GetMaxQueueSize(XmlElement element)
        {
            try
            {
                var maxQueueSizeStr = element.GetAttribute("max-queue-size");
                if (!String.IsNullOrWhiteSpace(maxQueueSizeStr))
                {
                    var maxQueueSize = int.Parse(maxQueueSizeStr);
                    if (maxQueueSize > 0)
                    {
                        return maxQueueSize;
                    }
                }
            }
            catch (Exception ex)
            { Cat.lastException = ex; }
            return DEFAULT_MAX_QUEUE_SIZE;
        }

        private int GetMaxQueueByteSize(XmlElement element)
        {
            try
            {
                var maxQueueByteSizeStr = element.GetAttribute("max-queue-byte-size");
                if (!String.IsNullOrWhiteSpace(maxQueueByteSizeStr))
                {
                    var maxQueueByteSize = int.Parse(maxQueueByteSizeStr);
                    if (maxQueueByteSize > 0)
                    {
                        return maxQueueByteSize;
                    }
                }
            }
            catch (Exception ex)
            { Cat.lastException = ex; }
            return DEFAULT_MAX_QUEUE_BYTE_SIZE;
        }

        private static Domain BuildDomain(XmlNodeList nodes)
        {
            if (nodes == null || nodes.Count == 0)
            {
                return new Domain();
            }

            XmlElement node = (XmlElement)nodes[0];
            return new Domain
            {
                Id = GetStringProperty(node, "id", CatConstants.UNKNOWN_DOMAIN).Trim(),
                //Ip = GetStringProperty(node, "ip", null),
                Enabled = GetBooleanProperty(node, "enabled", true)
            };
        }

        private static bool BuildLogEnabled(XmlNodeList nodes)
        {
            if (nodes == null || nodes.Count == 0)
            {
                return false;
            }
            XmlElement node = (XmlElement)nodes[0];
            return GetBooleanProperty(node, "enabled", false);
        }

        private static IEnumerable<Server> BuildServers(XmlNodeList nodes)
        {
            List<Server> servers = new List<Server>();

            if (nodes != null && nodes.Count > 0)
            {
                XmlElement first = (XmlElement)nodes[0];
                XmlNodeList serverNodes = first.GetElementsByTagName("server");

                foreach (XmlNode node in serverNodes)
                {
                    XmlElement serverNode = (XmlElement)node;
                    string ip = GetStringProperty(serverNode, "ip", "localhost");
                    int port = GetIntProperty(serverNode, "port", 2280);
                    int httpPort = GetIntProperty(serverNode, "http-port", 8080);
                    Server server = new Server(ip, port, httpPort) { Enabled = GetBooleanProperty(serverNode, "enabled", true) };

                    servers.Add(server);
                }
            }

            if (servers.Count == 0)
            {
                Logger.Warn("No server configured, use localhost:2280 instead.");
                servers.Add(new Server("localhost", 2280));
            }

            return servers;
        }

        private static string GetStringProperty(XmlElement element, string name, string defaultValue)
        {
            if (element != null)
            {
                string value = element.GetAttribute(name);

                if (value.Length > 0)
                {
                    return value;
                }
            }

            return defaultValue;
        }

        private static bool GetBooleanProperty(XmlElement element, string name, bool defaultValue)
        {
            if (element != null)
            {
                string value = element.GetAttribute(name);

                if (value.Length > 0)
                {
                    return "true".Equals(value);
                }
            }

            return defaultValue;
        }

        private static int GetIntProperty(XmlElement element, string name, int defaultValue)
        {
            if (element != null)
            {
                string value = element.GetAttribute(name);

                if (value.Length > 0)
                {
                    int tmpRet;
                    if (int.TryParse(value, out tmpRet))
                        return tmpRet;
                }
            }

            return defaultValue;
        }

        public override string GetConfigHeartbeatMessage()
        {
            return null;
        }
    }
}
