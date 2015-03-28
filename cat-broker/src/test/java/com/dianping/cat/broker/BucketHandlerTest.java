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

import com.dianping.cat.broker.api.app.AppQueue;
import com.dianping.cat.broker.api.app.bucket.BucketHandler;
import com.dianping.cat.broker.api.app.bucket.impl.DataBucketExecutor;
import com.dianping.cat.broker.api.app.proto.AppDataProto;
import com.dianping.cat.broker.api.app.proto.ProtoData;
import com.dianping.cat.broker.api.app.service.AppService;
import com.dianping.cat.config.app.AppConfigManager;

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
		Map<String, AppService> services = new HashMap<String, AppService>();
		AppConfigManager appConfigManager = new AppConfigManager();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		long startTime = sdf.parse("2014-08-19 11:20").getTime();
		BucketHandler handler = new BucketHandler(startTime, services, appConfigManager);

		HashMap<Integer, HashMap<String, AppDataProto>> datas = ((DataBucketExecutor) handler.getBucketExecutors().get(
		      AppDataProto.class.getName())).getDatas();
		HashMap<String, AppDataProto> data = new HashMap<String, AppDataProto>();

		datas.put(1, data);

		for (int i = 0; i < 10; i++) {
			AppDataProto createAppData = createAppData(i);
			data.put(createAppData.toString(), createAppData);
		}

		handler.save(file);

		datas.clear();
		handler.load(file);
		AppQueue queue = handler.getAppDataQueue();

		while (true) {
			ProtoData appdata = queue.poll();
			if (appdata != null) {

				handler.processEntity(appdata);
			} else {
				break;
			}
		}

		HashMap<String, AppDataProto> temp = ((DataBucketExecutor) handler.getBucketExecutors().get(
		      AppDataProto.class.getName())).getDatas().get(1);
		Assert.assertEquals(10, temp.size());
	}

	public AppDataProto createAppData(int i) {
		AppDataProto appdata = new AppDataProto();

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
