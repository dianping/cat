package com.dianping.cat.message;


public interface ForkableTransaction extends Transaction {
    ForkedTransaction doFork();
}