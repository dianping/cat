package com.dianping.cat.job.sql;

import java.io.IOException;
import org.apache.hadoop.mapreduce.Reducer;

public class SqlJobReducer extends Reducer<SqlStatementKey, SqlStatementValue, SqlStatementKey, SqlJobResult> {

	public void reduce(SqlStatementKey key, Iterable<SqlStatementValue> values, Context context) throws IOException,
	      InterruptedException {
		SqlJobResult result = new SqlJobResult();
		
		for (SqlStatementValue val : values) {
			result.add(val.getValue(), val.getFlag(), val.getSampleUrl());
		}
		context.write(key, result);
	}
}
