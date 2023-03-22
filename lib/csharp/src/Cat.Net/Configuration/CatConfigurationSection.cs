using Microsoft.Extensions.Configuration;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Org.Unidal.Cat.Configuration
{
    public class CatConfigurationSection
    {
        private static  CatConfigurationSection _catConfig = null;

        public static void Load(IConfiguration config)
        {
            _catConfig = new CatConfigurationSection();

            _catConfig.Domain = config.GetSection("catConfiguration:domain").Get<DomainElement>();
            if (_catConfig.Domain == null)
            {
                throw new Exception("Wrong Cat Configuration.");
            }

            _catConfig.LogEnabled = config.GetSection("catconfiguration:logEnabled").Get<LogElement>();
            _catConfig.Servers = config.GetSection("catconfiguration:servers").Get<ServerElement[]>();

            if (_catConfig.Domain.Enabled && (_catConfig.Servers == null || !_catConfig.Servers.Any(s => s.Enabled)))
            {
                throw new Exception("Wrong Cat Configuration, no avaiable server.");
            }
        }

        public DomainElement Domain { get; set; }

        public LogElement LogEnabled { get; set; }

        public ServerElement[] Servers { get; set; }

        public static CatConfigurationSection CatConfig { get { return _catConfig; } }
    }

    public class DomainElement
    {
        public string Id { get; set; }

        public string Ip { get; set; }

        public bool Enabled { get; set; }

        public int MaxMessageSize { get; set; }

        public bool UseClientLoadBalance { get; set; }
    }

    public class LogElement
    {
        public bool Enabled { get; set; } = false;
    }

    public class ServerElement
    {
        public string Ip { get; set; }

        public int Port { get; set; }

        public int HttpPort { get; set; }

        public bool Enabled { get; set; } = true;
    }
}
