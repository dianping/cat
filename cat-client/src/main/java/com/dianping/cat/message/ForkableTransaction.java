package com.dianping.cat.message;

public interface ForkableTransaction extends Transaction {
   public ForkedTransaction doFork();
}