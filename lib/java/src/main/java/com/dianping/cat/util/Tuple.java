package com.dianping.cat.util;

public interface Tuple {
    <T> T get(int index);

    int size();
}
