using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Org.Unidal.Cat.Util
{
    internal class ThreadSafeStack<T> : IEnumerable<T>, ICollection, IEnumerable
    {
        private Stack<T> _stack = new Stack<T>();
        private Object _syncRoot = new object();

        public int Count
        {
            get
            {
                lock (SyncRoot)
                {
                   return _stack.Count;
                }
            }
        }

        public Object SyncRoot => _syncRoot;

        public bool IsSynchronized => true;

        public T Peek()
        {
            lock (SyncRoot)
            {
                return _stack.Peek();
            }
        }

        public T Pop()
        {
            lock (SyncRoot)
            {
                return _stack.Pop();
            }
        }

        public void Push(T item)
        {
            lock (SyncRoot)
            {
                _stack.Push(item);
            }
        }

        public void Clear()
        {
            lock (SyncRoot)
            {
                _stack.Clear();
            }
        }

        public void CopyTo(Array array, int index)
        {
            lock (this.SyncRoot)
            {
                (_stack as ICollection).CopyTo(array, index);
            }
        }

        public IEnumerator<T> GetEnumerator()
        {
            return _stack.GetEnumerator();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return GetEnumerator();
        }
    }
}
