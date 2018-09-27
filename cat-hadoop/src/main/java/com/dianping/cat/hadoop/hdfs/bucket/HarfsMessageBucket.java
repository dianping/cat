package com.dianping.cat.hadoop.hdfs.bucket;

import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.hadoop.hdfs.MessageBlockReader;

public class HarfsMessageBucket extends AbstractHdfsMessageBucket {

	public static final String ID = HdfsMessageBucketManager.HARFS_BUCKET;

	@Override
	public void initialize(String dataFile) throws IOException {
		initialize(dataFile, new Date());
	}

	@Override
	public void initialize(String dataFile, Date date) throws IOException {
		FileSystem fs = m_manager.getHarFileSystem(m_id, date);
		int index = dataFile.indexOf("/");

		if (index > 0) {
			String parent = dataFile.substring(0, index);
			dataFile = dataFile.substring(index + 1);
			Path basePath = new Path(parent);
			m_reader = new MessageBlockReader(fs, basePath, dataFile);
		} else {
			m_reader = new MessageBlockReader(fs, dataFile);
		}
	}

}
