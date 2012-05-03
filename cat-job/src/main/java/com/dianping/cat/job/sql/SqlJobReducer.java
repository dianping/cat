package com.dianping.cat.job.sql;

import java.io.IOException;
import org.apache.hadoop.mapreduce.Reducer;

public class SqlJobReducer extends Reducer<UrlStatementKey, UrlStatementValue, UrlStatementKey, SqlJobResult> {

	public void reduce(UrlStatementKey key, Iterable<UrlStatementValue> values, Context context) throws IOException,
	      InterruptedException {
		SqlJobResult result = new SqlJobResult();
		for (UrlStatementValue val : values) {
			result.add(val.getValue(), val.getFlag(), val.getSampleUrl().toString() ,val.getMinute());
		}
		context.write(key, result);
	}
}
