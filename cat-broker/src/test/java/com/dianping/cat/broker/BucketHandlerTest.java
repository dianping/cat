package com.dianping.cat.broker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.broker.api.app.AppData;
import com.dianping.cat.broker.api.app.AppDataQueue;
import com.dianping.cat.broker.api.app.BucketHandler;
import com.dianping.cat.config.app.AppDataService;

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
	public void test() throws Exception {
		AppDataService appDataService = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		long startTime = sdf.parse("2014-08-19 11:20").getTime();
		BucketHandler handler = new BucketHandler(startTime, appDataService);

		HashMap<Integer, HashMap<String, AppData>> datas = handler.getDatas();
		HashMap<String, AppData> data = new HashMap<String, AppData>();

		datas.put(1, data);

		for (int i = 0; i < 10; i++) {
			AppData createAppData = createAppData(i);
			data.put(createAppData.toString(), createAppData);
		}

		handler.save(file);

		datas.clear();

		handler.load(file);
		AppDataQueue queue = handler.getAppDataQueue();

		while (true) {
			AppData appdata = queue.poll();
			if (appdata != null) {

				handler.processEntity(appdata);
			} else {
				break;
			}
		}

		HashMap<String, AppData> temp = handler.getDatas().get(1);
		Assert.assertEquals(10, temp.size());
	}

	public AppData createAppData(int i) {
		AppData appdata = new AppData();

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
