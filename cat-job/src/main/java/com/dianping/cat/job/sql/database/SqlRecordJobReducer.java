package com.dianping.cat.job.sql.database;

import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import com.dianping.cat.job.sql.dal.SqlReportRecord;
import com.dianping.cat.job.sql.dal.SqlReportRecordDao;
import com.site.dal.jdbc.DalException;

public class SqlRecordJobReducer extends Reducer<Text, Text, Text, Text> {
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
		Text currentHour = values.iterator().next();
		SqlReportJobRecord sql = new SqlReportJobRecord(currentHour.toString(), key.toString());

		try {
			SqlReportRecordDao dao = ContainerBootstrap.INSTANCE.lookup(SqlReportRecordDao.class);
			SqlReportRecord row = dao.createLocal();
			row.setDomain(sql.getDomain());
			row.setTotalCount(sql.getTotalCount());
			row.setFailureCount(sql.getFailureCount());
			row.setLongSqls(sql.getLongCount());
			row.setAvg2Value(sql.getAvg2());
			row.setSumValue(sql.getSum());
			row.setSum2Value(sql.getSum2());
			row.setMaxValue(sql.getMax());
			row.setMinValue(sql.getMin());
			row.setStatement(sql.getStatement());
			row.setName(sql.getName());
			row.setSampleLink(sql.getSampleLink());
			row.setTransactionDate(sql.getDate());
			row.setCreationDate(new Date());
			row.setDurationDistribution(sql.getDurationDistribution());
			row.setHitsOverTime(sql.getHitsOverTime());
			row.setDurationOverTime(sql.getDurationOverTime());
			row.setFailureOverTime(sql.getFailureOverTime());
			dao.insert(row);
		} catch (ComponentLookupException e) {
			e.printStackTrace();
		} catch (DalException e) {
			e.printStackTrace();
		}
	}
}
