package com.dianping.cat.broker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.broker.api.app.AppCommandData;
import com.dianping.cat.broker.api.app.AppDataQueue;
import com.dianping.cat.broker.api.app.BaseData;
import com.dianping.cat.broker.api.app.bucket.BucketHandler;
import com.dianping.cat.broker.api.app.bucket.CommandBucketExecutor;
import com.dianping.cat.service.app.BaseAppDataService;

public class BucketHandlerTest {

	private File file;

	@Before
	public void setUp() throws IOException {
		file = File.createTempFile("test", "test");
	}

	@After
	public void end() {
		file.delete();
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void test() throws Exception {
		Map<String, BaseAppDataService> services = new HashMap<String, BaseAppDataService>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		long startTime = sdf.parse("2014-08-19 11:20").getTime();
		BucketHandler handler = new BucketHandler(startTime, services);

		HashMap<Integer, HashMap<String, AppCommandData>> datas = ((CommandBucketExecutor) handler.getBucketExecutors()
		      .get(AppCommandData.class.getName())).getDatas();
		HashMap<String, AppCommandData> data = new HashMap<String, AppCommandData>();

		datas.put(1, data);

		for (int i = 0; i < 10; i++) {
			AppCommandData createAppData = createAppData(i);
			data.put(createAppData.toString(), createAppData);
		}

		handler.save(file);

		datas.clear();
		handler.load(file);
		AppDataQueue queue = handler.getAppDataQueue();

		while (true) {
			BaseData appdata = queue.poll();
			if (appdata != null) {

				handler.processEntity(appdata);
			} else {
				break;
			}
		}

		HashMap<String, AppCommandData> temp = ((CommandBucketExecutor) handler.getBucketExecutors().get(
		      AppCommandData.class.getName())).getDatas().get(1);
		Assert.assertEquals(10, temp.size());
	}

	public AppCommandData createAppData(int i) {
		AppCommandData appdata = new AppCommandData();

		appdata.setCommand(1);
		appdata.setVersion(i);
		appdata.setCity(i);
		appdata.setCode(i);
		appdata.setConnectType(1);
		appdata.setPlatform(i);
		appdata.setOperator(1);
		appdata.setCount(1);
		appdata.setPlatform(1);
		appdata.setRequestByte(10);
		appdata.setResponseByte(10);

		return appdata;
	}

}
