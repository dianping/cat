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
			row.setTotalcount(sql.getTotalCount());
			row.setFailures(sql.getFailureCount());
			row.setLongsqls(sql.getLongCount());
			row.setAvg2value(sql.getAvg2());
			row.setSumvalue(sql.getSum());
			row.setSum2value(sql.getSum2());
			row.setMaxvalue(sql.getMax());
			row.setMinvalue(sql.getMin());
			row.setStatement(sql.getStatement());
			row.setName(sql.getName());
			row.setSamplelink(sql.getSampleLink());
			row.setTransactiondate(sql.getDate());
			row.setCreationdate(new Date());
			row.setDurationdistribution(sql.getDurationDistribution());
			row.setHitsovertime(sql.getHitsOverTime());
			row.setDurationovertime(sql.getDurationOverTime());
			row.setFailureovertime(sql.getFailureOverTime());
			dao.insert(row);
		} catch (ComponentLookupException e) {
			e.printStackTrace();
		} catch (DalException e) {
			e.printStackTrace();
		}
	}
}
