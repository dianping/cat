using System;
using System.Collections.Generic;
using System.Text;
using System.Runtime.CompilerServices;

/*
 * There is no C# equivallent to Java LinkedHashMap.
 * Therefore, I borrowed this LRUCache implementation from http://stackoverflow.com/questions/754233/is-it-there-any-lru-implementation-of-idictionary/3719378#3719378
 */
namespace Org.Unidal.Cat.Util
{
    internal class LRUCacheItem<K, V>
    {
        public LRUCacheItem(K k, V v)
        {
            key = k;
            value = v;
        }
        public K key;
        public V value;
    }

    public class LRUCache<K, V>
    {
        int capacity;
        Dictionary<K, LinkedListNode<LRUCacheItem<K, V>>> cacheMap = new Dictionary<K, LinkedListNode<LRUCacheItem<K, V>>>();
        LinkedList<LRUCacheItem<K, V>> lruList = new LinkedList<LRUCacheItem<K, V>>();

        public LRUCache(int capacity)
        {
            this.capacity = capacity;
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public V Get(K key)
        {
            LinkedListNode<LRUCacheItem<K, V>> node;
            if (cacheMap.TryGetValue(key, out node))
            {
                //System.Console.WriteLine("Cache HIT " + key);
                V value = node.Value.value;

                lruList.Remove(node);
                lruList.AddLast(node);
                return value;
            }
            //System.Console.WriteLine("Cache MISS " + key);
            return default(V);
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public V Set(K key, V val)
        {
            V oldValue = default(V);
            if (cacheMap.Count >= capacity)
            {
                RemoveFirst();
            }

            LinkedListNode<LRUCacheItem<K, V>> oldNode;
            if (cacheMap.TryGetValue(key, out oldNode))
            {
                oldValue = oldNode.Value.value;
                lruList.Remove(oldNode);
                cacheMap.Remove(key);
            }

            LRUCacheItem<K, V> cacheItem = new LRUCacheItem<K, V>(key, val);
            LinkedListNode<LRUCacheItem<K, V>> node = new LinkedListNode<LRUCacheItem<K, V>>(cacheItem);
            lruList.AddLast(node);
            cacheMap.Add(key, node);

            return oldValue;
        }


        protected void RemoveFirst()
        {
            // Remove from LRUPriority
            LinkedListNode<LRUCacheItem<K, V>> node = lruList.First;
            lruList.RemoveFirst();
            // Remove from cache
            cacheMap.Remove(node.Value.key);
        }
    }
}
