/**
 * 
 */
package com.dianping.tkv.hdfs;

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

import com.dianping.tkv.DataStore;
import com.dianping.tkv.IndexStore;
import com.dianping.tkv.Meta;
import com.dianping.tkv.MetaHolder;
import com.dianping.tkv.Record;
import com.dianping.tkv.Tag;
import com.dianping.tkv.Tkv;

/**
 * @author sean.wang
 * @since Mar 7, 2012
 */
public class HdfsImpl implements Tkv {
	private IndexStore indexStore;

	private DataStore dataStore;

	private Lock indexWriteLock = new ReentrantLock();

	private Lock dataWriteLock = new ReentrantLock();

	public HdfsImpl() {

	}

	public HdfsImpl(String hdfsDir, File localDir, String indexFilename, String dataFilename, int keyLength, int tagLength) throws IOException {
		this.indexStore = new HdfsIndexStore(hdfsDir, indexFilename, new File(localDir, indexFilename), keyLength, tagLength);
		this.dataStore = new HdfsDataStore(hdfsDir, dataFilename);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.tkv.Tkv#close()
	 */
	@Override
	public void close() throws IOException {
		try {
			indexWriteLock.lock();
			this.getIndexStore().close();
		} finally {
			indexWriteLock.unlock();
		}
		try {
			dataWriteLock.lock();
			this.getDataStore().close();
		} finally {
			dataWriteLock.unlock();
		}
	}

	/*
	 * 
	 * 
	 * @see com.dianping.tkv.Tkv#get(int)
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
	 * @see com.dianping.tkv.Tkv#get(java.lang.String)
	 */
	@Override
	public byte[] get(String key) throws IOException {
		Meta meta = getIndex(key);
		if (meta == null) {
			return null;
		}
		return getRecordFromIndex(meta);
	}

	/**
	 * @param index
	 * @return
	 * @throws IOException
	 */
	private byte[] getRecordFromIndex(Meta meta) throws IOException {
		return getDataStore().get(meta.getOffset(), meta.getLength());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.tkv.Tkv#get(java.lang.String, java.lang.String)
	 */
	@Override
	public byte[] get(String key, String tag) throws IOException {
		Meta meta = getIndex(key, tag);
		if (meta == null) {
			return null;
		}
		return getRecordFromIndex(meta);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.tkv.Tkv#getIndex(int)
	 */
	@Override
	public Meta getIndex(int indexPos) throws IOException {
		return this.getIndexStore().getIndex(indexPos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.tkv.Tkv#getIndex(java.lang.String)
	 */
	@Override
	public Meta getIndex(String key) throws IOException {
		return this.getIndexStore().getIndex(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.tkv.Tkv#getIndex(java.lang.String, java.lang.String)
	 */
	@Override
	public Meta getIndex(String key, String tag) throws IOException {
		return this.getIndexStore().getIndex(key, tag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.tkv.Tkv#getRecord(java.lang.String, java.lang.String)
	 */
	@Override
	public Record getRecord(String key, String tag) throws IOException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.tkv.Tkv#put(java.lang.String, byte[])
	 */
	@Override
	public boolean put(String key, byte[] value) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean put(String key, byte[] value, String... tags) throws IOException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.tkv.Tkv#size()
	 */
	@Override
	public long size() throws IOException {
		return this.getIndexStore().size();
	}

	public void putIndex(MetaHolder holder) throws IOException {
		Collection<String> keys = holder.getKeys();
		if (keys == null || keys.size() == 0) {
			return;
		}
		Object[] keyArray = keys.toArray();
		Arrays.sort(keyArray);
		List<Meta> metas = new ArrayList<Meta>(keyArray.length);
		int i = 0;
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
					t.setPos(i);
					Tag holdTag = lastTagHolder.get(t.getName());
					if (holdTag != null) {
						t.setPrevious(holdTag.getPos());
						holdTag.setNext(i);
					}
					lastTagHolder.put(t.getName(), t);
				}
			}
			i++;
			metas.add(meta);
		}
		// store index
		try {
			indexWriteLock.lock();
			for (Meta meta : metas) {
				this.getIndexStore().append(meta);
			}
		} finally {
			indexWriteLock.unlock();
		}
	}

	public void putData(File dataFile) {
		try {
			dataWriteLock.lock();
			// TODO
		} finally {
			dataWriteLock.unlock();
		}

	}

	public IndexStore getIndexStore() {
		return indexStore;
	}

	public void setIndexStore(IndexStore indexStore) {
		this.indexStore = indexStore;
	}

	public DataStore getDataStore() {
		return dataStore;
	}

	public void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
	}

}
