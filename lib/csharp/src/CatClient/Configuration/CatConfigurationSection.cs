using System;
using System.Collections.Generic;
using System.Configuration;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Org.Unidal.Cat.Configuration
{
    public class CatConfigurationSection : ConfigurationSection
    {
        private static CatConfigurationSection _catConfig = ConfigurationManager.GetSection("CatConfiguration") as CatConfigurationSection;

        public static CatConfigurationSection CatConfig { get { return _catConfig; } }

        [ConfigurationProperty("domain")]
        public DomainElement Domain
        {
            get { return (DomainElement)this["domain"]; }
            set { this["domain"] = value; }
        }

        [ConfigurationProperty("logEnabled")]
        public LogElement LogEnabled
        {
            get { return (LogElement)this["logEnabled"]; }
            set { this["logEnabled"] = value; }
        }

        [ConfigurationProperty("servers")]
        public ServerCollection Servers
        {
            get { return (ServerCollection)this["servers"]; }
            set { this["servers"] = value; }
        }
    }

    public class DomainElement : ConfigurationElement
    {
        [ConfigurationProperty("id", DefaultValue = "Unknown", IsRequired = true)]
        public string Id
        {
            get { return (string)this["id"]; }
            set { this["id"] = value; }
        }

        [ConfigurationProperty("ip", DefaultValue = "", IsRequired = false)]
        public string Ip
        {
            get { return (string)this["ip"]; }
            set { this["ip"] = value; }
        }

        [ConfigurationProperty("enabled", DefaultValue = "false", IsRequired = true)]
        public bool Enabled
        {
            get { return (bool)this["enabled"]; }
            set { this["enabled"] = value; }
        }

        [ConfigurationProperty("max_message_size", DefaultValue = 1024, IsRequired = false)]
        public int MaxMessageSize
        {
            get { return (int)this["max_message_size"]; }
            set { this["max_message_size"] = value; }
        }

        [ConfigurationProperty("client_load_balance", DefaultValue = "false", IsRequired = false)]
        public bool UseClientLoadBalance
        {
            get { return (bool)this["client_load_balance"]; }
            set { this["client_load_balance"] = value; }
        }
    }

    public class LogElement : ConfigurationElement
    {
        [ConfigurationProperty("enabled", DefaultValue = "false", IsRequired = true)]
        public bool Enabled
        {
            get { return (bool)this["enabled"]; }
            set { this["enabled"] = value; }
        }
    }

    public class ServerElement : ConfigurationElement
    {
        [ConfigurationProperty("ip", DefaultValue = "", IsRequired = true)]
        public string Ip
        {
            get { return (string)this["ip"]; }
            set { this["ip"] = value; }
        }

        [ConfigurationProperty("port", DefaultValue = 2280, IsRequired = false)]
        public int Port
        {
            get { return (int)this["port"]; }
            set { this["port"] = value; }
        }

        [ConfigurationProperty("http-port", DefaultValue = 8080, IsRequired = false)]
        public int HttpPort
        {
            get { return (int)this["http-port"]; }
            set { this["http-port"] = value; }
        }

        [ConfigurationProperty("enabled", DefaultValue = "true", IsRequired = false)]
        public bool Enabled
        {
            get { return (bool)this["enabled"]; }
            set { this["enabled"] = value; }
        }
    }

    [ConfigurationCollection(typeof(ServerElement))]
    public class ServerCollection : ConfigurationElementCollection
    {
        internal const string PropertyName = "server";

        protected override ConfigurationElement CreateNewElement()
        {
            return new ServerElement();
        }

        protected override object GetElementKey(ConfigurationElement element)
        {
            return ((ServerElement)element).Ip;
        }

        protected override string ElementName => PropertyName;

        protected override bool IsElementName(string elementName)
        {
            return elementName.Equals(PropertyName, StringComparison.InvariantCultureIgnoreCase);
        }

        public override ConfigurationElementCollectionType CollectionType
        {
            get
            {
                return ConfigurationElementCollectionType.BasicMapAlternate;
            }
        }
    }
}
