package com.dianping.cat.broker.api.app.bucket;

import java.io.File;

import com.dianping.cat.broker.api.app.BaseData;
import com.dianping.cat.broker.api.app.AppDataType;

public interface BucketExecutor {

	public void processEntity(BaseData appData);

	public void flush();

	public void save(File file);

	public BaseData loadRecord(String[] items, AppDataType type);
}
