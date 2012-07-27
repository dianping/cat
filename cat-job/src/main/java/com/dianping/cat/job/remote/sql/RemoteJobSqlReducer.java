package com.dianping.cat.job.remote.sql;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.dianping.cat.job.sql.dal.Logview;
import com.dianping.cat.job.sql.dal.LogviewDao;

public class RemoteJobSqlReducer extends Reducer<Text, Text, Text, Text> {
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
//		Text currentHour = values.iterator().next();
//		RemoteJobLogviewRecord sql = new RemoteJobLogviewRecord(currentHour.toString(), key.toString());

		try {
			LogviewDao dao = ContainerBootstrap.INSTANCE.lookup(LogviewDao.class);
			Logview row = dao.createLocal();
			dao.insert(row);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
