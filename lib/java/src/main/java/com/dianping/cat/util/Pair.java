package com.dianping.cat.util;

public class Pair<K, V> implements Tuple {
    private volatile K key;
    private volatile V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
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

            if (key == null) {
                if (o.key != null) {
                    return false;
                }
            } else if (!key.equals(o.key)) {
                return false;
            }

            if (value == null) {
                if (o.value != null) {
                    return false;
                }
            } else if (!value.equals(o.value)) {
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
                return (T) key;
            case 1:
                return (T) value;
            default:
                throw new IndexOutOfBoundsException(String.format("Index from 0 to %s, but was %s!", size(), index));
        }
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (key == null ? 0 : key.hashCode());
        hash = hash * 31 + (value == null ? 0 : value.hashCode());

        return hash;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public String toString() {
        return String.format("Pair[key=%s, value=%s]", key, value);
    }
}
