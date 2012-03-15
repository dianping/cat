package com.dianping.cat.job.hdfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;

import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.hdfs.HdfsHelper;
import com.dianping.cat.storage.hdfs.HdfsImpl;

/**
 * @author sean.wang
 * @since Mar 9, 2012
 */
public class HdfsBucket implements Bucket<byte[]> {
	private static final Log log = LogFactory.getLog(HdfsBucket.class);

	private HdfsImpl hdfs;

	private int keyLength = 32;

	private int tagLength = 125;

	private File baseDir;

	private String logicalPath;

	@Override
	public void close() throws IOException {
		this.hdfs.close();
	}

	@Override
	public void deleteAndCreate() throws IOException {
		this.hdfs.deleteLocal();
		this.initialize(null, this.baseDir, this.logicalPath);
	}

	public void delete() throws IOException {
		this.hdfs.delete();
	}

	@Override
	public List<byte[]> findAllByIds(List<String> ids) throws IOException {
		List<byte[]> values = new ArrayList<byte[]>(ids.size());
		for (String id : ids) {
			byte[] value = this.findById(id);
			values.add(value);
		}
		return values;
	}

	@Override
	public List<String> findAllIdsByTag(String tag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] findById(String id) throws IOException {
		return hdfs.get(id);
	}

	@Override
	public void flush() throws IOException {
		hdfs.buildIndex();
		hdfs.endWrite();
		this.startRead();
	}

	@Override
	public byte[] findPreviousById(String id, String tagName) throws IOException {
		return hdfs.getPrevious(id, tagName);
	}

	@Override
	public byte[] findNextById(String id, String tagName) throws IOException {
		return hdfs.getNext(id, tagName);
	}

	/**
	 * @param baseDir
	 *           e.g /data/appdata/cat/
	 * @param logicalPath
	 *           e.g /a/b/c
	 */
	@Override
	public void initialize(Class<?> type, File baseDir, String logicalPath) throws IOException {
		this.baseDir = baseDir;
		this.logicalPath = logicalPath;
		
		File file = new File(baseDir, logicalPath);
		File parent = file.getParentFile();
		parent.mkdirs();

		File tmpDir = new File(parent, "hdfstmp");
		FileSystem fs = HdfsHelper.createLocalFileSystem(parent.getAbsolutePath());
		String filename = new File(logicalPath).getName() + "-" + InetAddress.getLocalHost().getHostAddress();

		this.hdfs = new HdfsImpl(fs, tmpDir, filename + ".idx", filename + ".dat", keyLength, tagLength);
		this.startWrite();
	}

	/**
	 * @throws IOException
	 */
	private void startWrite() throws IOException {
		hdfs.startWrite();
	}

	public void setHdfs(HdfsImpl hdfs) {
		this.hdfs = hdfs;
	}

	public void setKeyLength(int keyLength) {
		this.keyLength = keyLength;
	}

	public void setTagLength(int tagLength) {
		this.tagLength = tagLength;
	}

	@Override
	public boolean storeById(String id, byte[] data, String... tags) throws IOException {
		return this.hdfs.put(id, data, tags);
	}

	private void startRead() throws IOException {
		try {
			hdfs.startRead();
		} catch (FileNotFoundException e) {
			log.warn("hdfs remote data file not exists");
		}
	}
}
