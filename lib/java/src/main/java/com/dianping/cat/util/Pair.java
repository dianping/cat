/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
