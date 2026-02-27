using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Org.Unidal.Cat.Util
{
    class Pair<K, V>
    {
        public K key;
        public V value;

        public Pair(K k, V v)
        {
            key = k;
            value = v;
        }
    }
}
