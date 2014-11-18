package com.dianping.cat.broker.api.app.bucket;

import java.io.File;

import com.dianping.cat.broker.api.app.proto.ProtoData;

public interface BucketExecutor {

	public void processEntity(ProtoData appData);

	public void flush();

	public void save(File file);

	public ProtoData loadRecord(String[] items);
}
