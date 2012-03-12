/**
 * 
 */
package com.dianping.tkv.hdfs;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

/**
 * @author sean.wang
 * @since Mar 9, 2012
 */
public class HdfsHelper {

	public static FileSystem createFileSystem(String dir) throws IOException {
		Configuration config = new Configuration();
		config.setInt("io.file.buffer.size", 8192);
		FileSystem fs;
		if (dir == null) {
			fs = FileSystem.getLocal(config);
		} else {
			URI uri = URI.create(dir);
			fs = FileSystem.get(uri, config);
		}
		return fs;
	}

}
