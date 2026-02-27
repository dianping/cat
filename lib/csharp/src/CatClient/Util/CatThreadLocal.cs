using System;
using System.Collections;
using System.Collections.Concurrent;
using System.Threading;
using System.Web;

namespace Org.Unidal.Cat.Util
{
    public class CatThreadLocal<T>  
    {
        private AsyncLocal<T> _contextLocal = new AsyncLocal<T>();

        public T Value
        {
            get
            {
                return _contextLocal.Value;
            }
            set
            {
                _contextLocal.Value = value;
            }
        }

        public void Remove()
        {
            _contextLocal.Value = default(T);
        }
    }
}