using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Org.Unidal.Cat.Message.Spi;

namespace Org.Unidal.Cat.Message.Internals
{
    public class DefaultMetric : AbstractMessage, IMetric
    {
        private IMessageManager _mManager;

        public DefaultMetric(String type, String name) : base(type, name)
        {
        }

        public DefaultMetric(string type, string name, IMessageManager manager)
            : base(type, name)
        {
            _mManager = manager;
        }

        public override void Complete()
        {
            try
            {
                base.Complete();

                if (_mManager != null && _mManager.ThreadLocalMessageTree.Message == null)
                {
                    _mManager.Add(this);
                }
            }
            catch (Exception ex)
            {
                Cat.lastException = ex;
            }
        }
    }
}
