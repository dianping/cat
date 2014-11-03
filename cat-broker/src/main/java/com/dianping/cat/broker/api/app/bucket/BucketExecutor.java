package com.dianping.cat.broker.api.app.bucket;

import java.io.File;

import com.dianping.cat.broker.api.app.AppData;
import com.dianping.cat.broker.api.app.AppDataType;

public interface BucketExecutor {

	void processEntity(AppData appData);

	void flush();

	void save(File file);

	AppData loadRecord(String[] items, AppDataType type);
}
