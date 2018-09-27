package com.dianping.cat.consumer.storage.builder;

import java.util.List;

import com.dianping.cat.message.Transaction;

public interface StorageBuilder {

	public StorageItem build(Transaction t);

	public List<String> getDefaultMethods();

	public String getType();

	public boolean isEligable(Transaction t);

}
