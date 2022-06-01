package com.dianping.cat.util;

public interface Tuple {
   public <T> T get(int index);

   public int size();
}
