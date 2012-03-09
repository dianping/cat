/**
 * 
 */
package com.dianping.cat.job.hdfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.storage.Bucket;
import com.dianping.tkv.Meta;
import com.dianping.tkv.hdfs.HdfsImpl;

/**
 * @author sean.wang
 * @since Mar 9, 2012
 */
public class HdfsBucket implements Bucket<byte[]> {

	private HdfsImpl hdfs;

	private String hdfsDir;

	private String localDir;

	private String indexFilename;

	private String dataFilename;

	private int keyLength = 32;

	private int tagLength = 125;

	public void setHdfs(HdfsImpl hdfs) {
		this.hdfs = hdfs;
	}

	public void setHdfsDir(String hdfsDir) {
		this.hdfsDir = hdfsDir;
	}

	public void setLocalDir(String localDir) {
		this.localDir = localDir;
	}

	public void setIndexFilename(String indexFilename) {
		this.indexFilename = indexFilename;
	}

	public void setDataFilename(String dataFilename) {
		this.dataFilename = dataFilename;
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

	@Override
	public void initialize(Class<?> type, File path) throws IOException {
		hdfs = new HdfsImpl(hdfsDir, localDir, indexFilename, dataFilename, keyLength, tagLength);
	}

	@Override
	public boolean storeById(String id, byte[] data) {
		throw new UnsupportedOperationException();
	}

}
