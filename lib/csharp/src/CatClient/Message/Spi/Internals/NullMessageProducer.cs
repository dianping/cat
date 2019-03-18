using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Org.Unidal.Cat.Message.Internals;

namespace Org.Unidal.Cat.Message.Spi.Internals
{
    class NullMessageProducer : IMessageProducer
    {
        void IMessageProducer.LogError(Exception cause)
        {
        }

        void IMessageProducer.LogError(string message, Exception cause)
        {
        }

        void IMessageProducer.LogEvent(string type, string name, string status, string nameValuePairs)
        {
        }

        void IMessageProducer.LogHeartbeat(string type, string name, string status, string nameValuePairs)
        {
        }

        void IMessageProducer.LogMetric(string name, string status, string nameValuePairs)
        {
        }

        IMetric IMessageProducer.NewMetric(string type, string name, string data)
        {
            return new NullMetric();
        }

        IEvent IMessageProducer.NewEvent(string type, string name, string data)
        {
            return new NullEvent();
        }

        IHeartbeat IMessageProducer.NewHeartbeat(string type, string name, string data)
        {
            return new NullHeartbeat();
        }

        ITransaction IMessageProducer.NewTransaction(string type, string name)
        {
            return new NullTransaction();
        }

        IForkedTransaction IMessageProducer.NewForkedTransaction(string type, string name)
        {
            return new NullTransaction();
        }

        ITaggedTransaction IMessageProducer.NewTaggedTransaction(string type, string name, string tag)
        {
            return new NullTransaction();
        }
    }
}
