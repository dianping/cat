package com.dianping.cat.job.sql.database;

import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import com.dianping.cat.job.sql.dal.Sqlreport;
import com.dianping.cat.job.sql.dal.SqlreportDao;
import com.site.dal.jdbc.DalException;

public class SqlRecordJobReducer extends Reducer<Text, Text, Text, Text>{
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
   InterruptedException {
		Text currentHour =  values.iterator().next();
		SqlReportRecord sql = new SqlReportRecord(currentHour.toString(),key.toString());
		
		try {
	      SqlreportDao dao = ContainerBootstrap.INSTANCE.lookup(SqlreportDao.class);
	      Sqlreport row = dao.createLocal();
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
	      dao.insert(row);
      } catch (ComponentLookupException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      } catch (DalException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
		
		System.out.println(sql);
	}
}	
