package com.dianping.cat.job.hdfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileSystem;

import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.hdfs.HdfsHelper;
import com.dianping.cat.storage.hdfs.HdfsImpl;

/**
 * @author sean.wang
 * @since Mar 9, 2012
 */
public class HdfsBucket implements Bucket<byte[]> {
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
	 *            e.g /data/appdata/cat/
	 * @param logicalPath
	 *            e.g /a/b/c
	 */
	@Override
	public void initialize(Class<?> type, File baseDir, String logicalPath) throws IOException {
		this.baseDir = baseDir;
		this.logicalPath = logicalPath;
		File logicalFile = new File(logicalPath);
		String[] segs = StringUtils.split(logicalFile.getName(), File.pathSeparatorChar);
		String filename = segs[segs.length - 1];
		String indexFilename = filename + ".index";
		String dataFilename = filename + ".data";
		String hdfsDir = logicalFile.getParent();
		FileSystem fs = HdfsHelper.createLocalFileSystem(hdfsDir);
		this.hdfs = new HdfsImpl(fs, baseDir, indexFilename, dataFilename, keyLength, tagLength);
	}

	/**
	 * @throws IOException
	 */
	public void startWrite() throws IOException {
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

	public void startRead() throws IOException {
		hdfs.startRead();
	}

}
