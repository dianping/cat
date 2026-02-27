using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections.Concurrent;
using System.Collections;
using System.Threading;
using Org.Unidal.Cat.Message;

namespace Org.Unidal.Cat.Util
{
    public class BlockingThreadSafeQueue<T>
    {
        private object wakeUpLink = new object();
        private volatile int isWait = 0;
        private int notifyMinSize = 1;

        private ConcurrentQueue<ReferenceWrapper<T>> queue;
        private int estimatedByteSize;

        public int EstimatedByteSize
        {
            get { return estimatedByteSize;  }
        }

        public BlockingThreadSafeQueue(int notifyMinSize = 1)
        {
            this.notifyMinSize = (notifyMinSize < 1 ? 1 : notifyMinSize);
            queue = new ConcurrentQueue<ReferenceWrapper<T>>();
        }
        public int Count { get { return queue.Count; } }
        public bool IsEmpty { get { return queue.IsEmpty; } }
        public void Enqueue(T item)
        {
            Interlocked.Add(ref estimatedByteSize,  GetEstimatedSize(item));
            queue.Enqueue(new ReferenceWrapper<T> { Item = item });
            if (isWait == 1)
            {
                lock (wakeUpLink)
                {
                    if (isWait == 1 && queue.Count >= notifyMinSize)
                    {
                        Monitor.PulseAll(wakeUpLink);
                        isWait = 0;
                    }
                }
            }
        }
        public bool TryDequeue(out T result, bool needWait = false)
        {
            result = default(T);
            ReferenceWrapper<T> item;

            if (queue.Count < notifyMinSize && needWait)
            {
                lock (wakeUpLink)
                {
                    if (queue.Count < notifyMinSize)
                    {
                        isWait = 1;
                        Monitor.Wait(wakeUpLink, 50);
                    }
                }
            }

            var hasElement = queue.TryDequeue(out item);
            if (hasElement)
            {
                result = item.Item;
                item.Item = default(T);
                Interlocked.Add(ref estimatedByteSize, 0 - GetEstimatedSize(result));
            }
            return hasElement;
        }

        public bool TryPeek(out T result, bool needWait = false)
        {
            result = default(T);
            ReferenceWrapper<T> item;

            if (queue.Count < notifyMinSize && needWait)
            {
                lock (wakeUpLink)
                {
                    if (queue.Count < notifyMinSize)
                    {
                        isWait = 1;
                        Monitor.Wait(wakeUpLink, 50);
                    }
                }
            }

            var hasElement = queue.TryPeek(out item);
            if (hasElement)
            {
                result = item.Item;
            }
            return hasElement;
        }

        public struct ReferenceWrapper<TItem>
        {
            public TItem Item { get; set; }
        }

        // This is not generic, a bit hacky.
        private int GetEstimatedSize(T item)
        {
            if (item is IMessageTree)
            {
                return ((IMessageTree)item).EstimatedByteSize;
            }
            return 0;
        }
    }
}
