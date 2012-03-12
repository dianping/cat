/**
 * 
 */
package com.dianping.cat.storage.hdfs.hdfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.hadoop.fs.FileSystem;

import com.dianping.cat.storage.hdfs.DataStore;
import com.dianping.cat.storage.hdfs.IndexStore;
import com.dianping.cat.storage.hdfs.Meta;
import com.dianping.cat.storage.hdfs.BatchHolder;
import com.dianping.cat.storage.hdfs.Record;
import com.dianping.cat.storage.hdfs.Tag;
import com.dianping.cat.storage.hdfs.Tkv;

/**
 * @author sean.wang
 * @since Mar 7, 2012
 */
public class HdfsImpl implements Tkv {
	private HdfsIndexStore indexStore;

	private HdfsDataStore dataStore;

	private Lock writeLock = new ReentrantLock();

	public HdfsImpl() {

	}

	public HdfsImpl(FileSystem fs, File localDir, String indexFilename, String dataFilename, int keyLength, int tagLength) throws IOException {
		File localIndexFile = new File(localDir, indexFilename);
		if (!localDir.exists()) {
			localDir.mkdirs();
		}
		if (!localIndexFile.exists()) {
			localIndexFile.createNewFile();
		}
		this.setIndexStore(new HdfsIndexStore(fs, indexFilename, localIndexFile, keyLength, tagLength));
		this.setDataStore(new HdfsDataStore(fs, dataFilename));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.Tkv#close()
	 */
	@Override
	public void close() throws IOException {
		try {
			writeLock.lock();
			this.getIndexStore().close();
			this.getDataStore().close();
		} finally {
			writeLock.unlock();
		}
	}

	/*
	 * 
	 * 
	 * @see com.dianping.cat.storage.hdfs.Tkv#get(int)
	 */
	@Override
	public byte[] get(int indexPos) throws IOException {
		Meta meta = this.getIndex(indexPos);
		if (meta == null) {
			return null;
		}
		return getRecordFromIndex(meta);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.Tkv#get(java.lang.String)
	 */
	@Override
	public byte[] get(String key) throws IOException {
		Meta meta = getIndex(key);
		if (meta == null) {
			return null;
		}
		return getRecordFromIndex(meta);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.Tkv#get(java.lang.String, java.lang.String)
	 */
	@Override
	public byte[] get(String key, String tag) throws IOException {
		Meta meta = getIndex(key, tag);
		if (meta == null) {
			return null;
		}
		return getRecordFromIndex(meta);
	}

	public DataStore getDataStore() {
		return dataStore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.Tkv#getIndex(int)
	 */
	@Override
	public Meta getIndex(int indexPos) throws IOException {
		return this.getIndexStore().getIndex(indexPos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.Tkv#getIndex(java.lang.String)
	 */
	@Override
	public Meta getIndex(String key) throws IOException {
		return this.getIndexStore().getIndex(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.Tkv#getIndex(java.lang.String, java.lang.String)
	 */
	@Override
	public Meta getIndex(String key, String tag) throws IOException {
		return this.getIndexStore().getIndex(key, tag);
	}

	public IndexStore getIndexStore() {
		return indexStore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.Tkv#getRecord(java.lang.String, java.lang.String)
	 */
	@Override
	public Record getRecord(String key, String tag) throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param index
	 * @return
	 * @throws IOException
	 */
	private byte[] getRecordFromIndex(Meta meta) throws IOException {
		return getDataStore().get(meta.getOffset(), meta.getLength());
	}

	private int pos = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.Tkv#put(java.lang.String, byte[])
	 */
	@Override
	public boolean put(String key, byte[] value) throws IOException {
		try {
			this.writeLock.lock();
			if (this.getIndexStore().getIndex(key) != null) {
				return false; // this key already exists
			}
			long offset = this.getDataStore().length();
			this.getDataStore().append(value);
			Meta meta = new Meta();
			meta.setKey(key);
			meta.setOffset(offset);
			meta.setLength(value.length);
			this.getIndexStore().append(meta);
			pos++;
			return true;
		} finally {
			this.writeLock.unlock();
		}
	}

	@Override
	public boolean put(String key, byte[] value, String... tags) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void batchPut(BatchHolder holder) throws IOException {
		Collection<String> keys = holder.getKeys();
		if (keys == null || keys.size() == 0) {
			return;
		}
		Object[] keyArray = keys.toArray();
		Arrays.sort(keyArray);
		List<Meta> metas = new ArrayList<Meta>(keyArray.length);
		// build tag position
		Map<String, Tag> lastTagHolder = new HashMap<String, Tag>();
		for (Object o : keyArray) {
			String key = (String) o;
			Meta meta = new Meta();
			meta.setKey(key);
			holder.getMeta(key, meta);
			Map<String, Tag> tags = meta.getTags();
			if (tags != null) {
				for (Tag t : tags.values()) {
					t.setPos(pos);
					Tag holdTag = lastTagHolder.get(t.getName());
					if (holdTag != null) {
						t.setPrevious(holdTag.getPos());
						holdTag.setNext(pos);
					}
					lastTagHolder.put(t.getName(), t);
				}
			}
			pos++;
			metas.add(meta);
		}
		// store index
		try {
			writeLock.lock();
			for (Meta meta : metas) {
				this.getDataStore().append(holder.getValue(meta.getKey()));
				this.getIndexStore().append(meta);
			}
		} finally {
			writeLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.Tkv#size()
	 */
	@Override
	public long size() throws IOException {
		return this.getIndexStore().size();
	}

	public void startWrite() throws IOException {
		this.dataStore.startWrite();
	}

	public void endWrite() throws IOException {
		this.dataStore.endWrite();
	}

	public void startRead() throws IOException {
		this.dataStore.startRead();
	}

	public void endRead() throws IOException {
		this.dataStore.endRead();
	}

	public void setDataStore(HdfsDataStore dataStore) {
		this.dataStore = dataStore;
	}

	public void setIndexStore(HdfsIndexStore indexStore) {
		this.indexStore = indexStore;
	}

	@Override
	public boolean delete() throws IOException {
		boolean dataDeleted = this.dataStore.delete();
		boolean indexDeleted = this.indexStore.delete();
		return dataDeleted && indexDeleted;
	}

}
