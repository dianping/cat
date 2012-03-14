/**
 * 
 */
package com.dianping.cat.storage.hdfs;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

/**
 * @author sean.wang
 * @since Mar 9, 2012
 */
public class HdfsHelper {

	public static FileSystem createRemoteFileSystem(String dir) throws IOException {
		Configuration config = new Configuration();
		config.setInt("io.file.buffer.size", 8192);
		URI uri = URI.create(dir);
		FileSystem fs = FileSystem.get(uri, config);
		return fs;
	}

	public static FileSystem createLocalFileSystem(String dir) throws IOException {
		Configuration config = new Configuration();
		config.setInt("io.file.buffer.size", 8192);
		config.setStrings("dfs.data.dir", dir);
		FileSystem fs = FileSystem.getLocal(config);
		return fs;
	}

}
