package com.dianping.cat.util;

/**
 * Tuple to hold two elements: key and value.
 * 
 * @param <K>
 *           key
 * @param <V>
 *           value
 */
public class Pair<K, V> implements Tuple {
   private volatile K m_key;

   private volatile V m_value;

   public Pair() {
   }

   public Pair(K key, V value) {
      m_key = key;
      m_value = value;
   }

   public static <K, V> Pair<K, V> from(K key, V value) {
      return new Pair<K, V>(key, value);
   }

   @Override
   @SuppressWarnings("unchecked")
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (obj instanceof Pair) {
         Pair<Object, Object> o = (Pair<Object, Object>) obj;

         if (m_key == null) {
            if (o.m_key != null) {
               return false;
            }
         } else if (!m_key.equals(o.m_key)) {
            return false;
         }

         if (m_value == null) {
            if (o.m_value != null) {
               return false;
            }
         } else if (!m_value.equals(o.m_value)) {
            return false;
         }

         return true;
      }

      return false;
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> T get(int index) {
      switch (index) {
      case 0:
         return (T) m_key;
      case 1:
         return (T) m_value;
      default:
         throw new IndexOutOfBoundsException(String.format("Index from 0 to %s, but was %s!", size(), index));
      }
   }

   public K getKey() {
      return m_key;
   }

   public V getValue() {
      return m_value;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_key == null ? 0 : m_key.hashCode());
      hash = hash * 31 + (m_value == null ? 0 : m_value.hashCode());

      return hash;
   }

   public void setKey(K key) {
      m_key = key;
   }

   public void setValue(V value) {
      m_value = value;
   }

   @Override
   public int size() {
      return 2;
   }

   @Override
   public String toString() {
      return String.format("Pair[key=%s, value=%s]", m_key, m_value);
   }
}
