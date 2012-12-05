package com.dianping.cat.job.url;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class UrlJobReducer extends Reducer<Text, UrlValue, Text, Text> {

	public void reduce(Text key, Iterable<UrlValue> values, Context context) throws IOException, InterruptedException {
		StringBuilder result = new StringBuilder('\n');
		int i = 0;
		double sum = 0;
		for (UrlValue val : values) {
			i++;
			sum += val.getValue();
			result.append(val.getDate()).append("\t").append(val.getValue()).append('\n');
		}
		result.append("Avg=").append(sum / i).append("\n");
		context.write(key, new Text(result.toString()));
	}
}
