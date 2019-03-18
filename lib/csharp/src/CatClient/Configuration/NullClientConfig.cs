using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Org.Unidal.Cat.Message.Internals;

namespace Org.Unidal.Cat.Configuration
{
    class NullClientConfig : AbstractClientConfig
    {
        public NullClientConfig()
        {
            this.Domain = new Domain { Id = NullMessageTree.UNKNOWN, Enabled = true };
        }

        protected override string GetCatRouterServiceURL(bool sync)
        {
            return null;
        }

        public override string GetConfigHeartbeatMessage()
        {
            return null;
        }
    }
}
