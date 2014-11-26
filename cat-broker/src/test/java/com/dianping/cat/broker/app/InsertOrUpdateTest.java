package com.dianping.cat.broker.app;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.app.AppSpeedData;
import com.dianping.cat.app.AppSpeedDataDao;

public class InsertOrUpdateTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		int size = 5;
		AppSpeedDataDao dao = lookup(AppSpeedDataDao.class);
		List<AppSpeedData> datas = new LinkedList<AppSpeedData>();
		Date period = new SimpleDateFormat("yyyy-MM-dd").parse("2014-11-20");

		for (int i = 0; i < size; i++) {
			AppSpeedData data = new AppSpeedData();
			
			data.setMinuteOrder(5 * i).setCity(68).setOperator(1).setNetwork(1).setAppVersion(680).setAccessNumber(100)
			      .setSlowAccessNumber(200).setResponseSumTime(300).setSlowResponseSumTimeSum(400);
			data.setSpeedId(2).setPeriod(period).setCreationDate(new Date());
			datas.add(data);
		}

		AppSpeedData[] arrays = new AppSpeedData[size];
		for (int index = 0; index < size; index++) {
			arrays[index] = datas.get(index);
		}
		dao.insertOrUpdate(arrays);

	}
}
