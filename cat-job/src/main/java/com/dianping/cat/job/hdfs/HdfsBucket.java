package com.dianping.cat.job.hdfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.dianping.cat.storage.Bucket;
import com.dianping.tkv.Meta;
import com.dianping.tkv.hdfs.HdfsImpl;

/**
 * @author sean.wang
 * @since Mar 9, 2012
 */
public class HdfsBucket implements Bucket<byte[]> {

	private HdfsImpl hdfs;

	private int keyLength = 32;

	private int tagLength = 125;

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
	public boolean storeById(String id, byte[] data, String... tags) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> findAllIdsByTag(String tag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] findNextById(String id, com.dianping.cat.storage.TagThreadSupport.Direction direction, String tag) throws IOException {
		Meta meta = hdfs.getIndex(id, tag);
		if (meta == null) {
			return null;
		}
		int nextPos = meta.getTags().get(tag).getNext();
		return hdfs.get(nextPos);
	}

	@Override
	public void close() throws IOException {
		this.hdfs.close();
	}

	@Override
	public void deleteAndCreate() {
		throw new UnsupportedOperationException();
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
	public byte[] findById(String id) throws IOException {
		return hdfs.get(id);
	}

	/**
	 * @param baseDir
	 *            e.g /data/appdata/cat/
	 * @param logicalPath
	 *            e.g /a/b/c
	 */
	@Override
	public void initialize(Class<?> type, File baseDir, String logicalPath) throws IOException {
		String name = new File(logicalPath).getName();
		String[] segs = StringUtils.split(name, File.pathSeparatorChar);
		String filename = segs[segs.length - 1];
		String indexFilename = filename + ".idx";
		String dataFilename = filename + ".data";
		String hdfsDir = null;
		hdfs = new HdfsImpl(hdfsDir, baseDir, indexFilename, dataFilename, keyLength, tagLength);
	}

	@Override
	public boolean storeById(String id, byte[] data) {
		throw new UnsupportedOperationException();
	}

}
