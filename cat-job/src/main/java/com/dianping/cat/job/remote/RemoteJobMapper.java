package com.dianping.cat.job.remote;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class RemoteJobMapper extends Mapper<Text, Text, Text, Text> {
	private Set<String> m_rootIds = new HashSet<String>();

	private Set<String> m_existIds = new HashSet<String>();

	public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
		String text = value.toString();
		String[] ids = text.split("\t");

		String messageId = ids[0];
		String rootMessageId = ids[1];

		if (rootMessageId == null || rootMessageId.equals("null")) {
			rootMessageId = messageId;
		}

		m_rootIds.add(rootMessageId);
		m_existIds.add(messageId);
	}
}
