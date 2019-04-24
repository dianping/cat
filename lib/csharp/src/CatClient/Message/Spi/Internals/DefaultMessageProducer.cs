using Org.Unidal.Cat.Message.Internals;
using System;
using System.IO;
using System.Collections.Generic;
using System.Text;
using Org.Unidal.Cat.Util;

namespace Org.Unidal.Cat.Message.Spi.Internals
{
    public class DefaultMessageProducer : IMessageProducer
    {
        private const string TAG_MAPS_SEPARATOR = "@|";
        private readonly IMessageManager _mManager;

        public DefaultMessageProducer(IMessageManager manager)
        {
            _mManager = manager;
        }

        #region IMessageProducer Members

        public virtual void LogError(Exception cause)
        {
            LogError(null, cause);
        }

        public virtual void LogError(String message, Exception cause)
        {
            if (!ShouldLog(cause))
            {
                 //Console.WriteLine("Ignore error log of cause: " + cause);
                return;
            }

            StringWriter writer = new StringWriter();
            if (!String.IsNullOrWhiteSpace(message))
            {
                writer.WriteLine(message);
            }
            writer.WriteLine(cause.ToString());
            var exceptionType = (cause.Source !=null && cause.Source.Length > 0 && cause.Source[0] == '~') ?
                cause.Source.Substring(1) : cause.GetType().FullName;
            LogEvent("Error", exceptionType, "ERROR",
                    writer.ToString());
        }

        public virtual void LogEvent(String type, String name, String status, String nameValuePairs)
        {
            IEvent evt0 = NewEvent(type, name, nameValuePairs);

            evt0.Status = status;
            evt0.Complete();
        }

        public virtual void LogHeartbeat(String type, String name, String status, String nameValuePairs)
        {
            IHeartbeat heartbeat = NewHeartbeat(type, name, nameValuePairs);
            heartbeat.Status = status;
            heartbeat.Complete();
        }

        public virtual void LogMetric(String name, String status, String nameValuePairs)
        {
            string type = "";
            IMetric metric = NewMetric(type, name, nameValuePairs);
            metric.Status = status;
            metric.Complete();
        }

        public virtual IEvent NewEvent(String type, String name, String nameValuePairs)
        {
            if (!_mManager.HasContext())
            {
                _mManager.Setup();
            }

            if (_mManager.CatEnabled)
            {
                IEvent evt0 = new DefaultEvent(type, name);

                // Need add data before event is added into context, so that estimated bytes is correct.
                if (!string.IsNullOrEmpty(nameValuePairs))
                {
                    evt0.AddData(nameValuePairs);
                }

                _mManager.Add(evt0);
                return evt0;
            }
            return new NullEvent();
        }

        public virtual IMetric NewMetric(String type, String name, String nameValuePairs)
        {
            if (!_mManager.HasContext())
            {
                _mManager.Setup();
            }

            if (_mManager.CatEnabled)
            {
                IMetric metric = new DefaultMetric(type, name);
                if (!string.IsNullOrEmpty(nameValuePairs))
                {
                    metric.AddData(nameValuePairs);
                }
                _mManager.Add(metric);
                return metric;
            }
            return new NullMetric();
        }

        public virtual IHeartbeat NewHeartbeat(String type, String name, String nameValuePairs)
        {
            if (!_mManager.HasContext())
            {
                _mManager.Setup();
            }

            if (_mManager.CatEnabled)
            {
                IHeartbeat heartbeat = new DefaultHeartbeat(type, name);
                if (!string.IsNullOrEmpty(nameValuePairs))
                {
                    heartbeat.AddData(nameValuePairs);
                }
                _mManager.Add(heartbeat);
                return heartbeat;
            }
            return new NullHeartbeat();
        }

        public virtual ITransaction NewTransaction(String type, String name)
        {
            // this enable CAT client logging cat message without explicit setup
            if (!_mManager.HasContext())
            {
                _mManager.Setup();
            }

            if (_mManager.CatEnabled)
            {
                ITransaction transaction = new DefaultTransaction(type, name, _mManager);
                _mManager.Start(transaction, false);
                return transaction;
            }
            return new NullTransaction();
        }

        public virtual IForkedTransaction NewForkedTransaction(String type, String name)
        {
            if (!_mManager.HasContext())
            {
                _mManager.Setup();
            }

            if (_mManager.CatEnabled)
            {
                DefaultForkedTransaction transaction = new DefaultForkedTransaction(type, name, _mManager);
                if (_mManager is DefaultMessageManager)
                {
                    ((DefaultMessageManager)_mManager).LinkAsRunAway(transaction);
                }
                _mManager.Start(transaction, true);
                return transaction;
            }
            else
            {
                return new NullTransaction();
            }

        }

        public virtual ITaggedTransaction NewTaggedTransaction(string type, string name, string tag)
        {
            if (!_mManager.HasContext())
            {
                _mManager.Setup();
            }

            if (_mManager.CatEnabled)
            {
                DefaultTaggedTransaction transaction = new DefaultTaggedTransaction(type, name, tag, _mManager);
                _mManager.Start(transaction, true);
                return transaction;
            }
            else
            {
                return new NullTransaction();
            }
        }

        private bool ShouldLog(Exception e)
        {
            if (_mManager is DefaultMessageManager)
            {
                return ((DefaultMessageManager)_mManager).ShouldLog(e);
            }
            else
            {
                return true;
            }
        }

        #endregion
    }
}