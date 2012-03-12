/**
 * 
 */
package com.dianping.tkv.local;

import static com.dianping.tkv.util.NumberKit.bytes2Int;
import static com.dianping.tkv.util.NumberKit.int2Bytes;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.dianping.tkv.DataStore;
import com.dianping.tkv.Meta;
import com.dianping.tkv.Record;
import com.dianping.tkv.Tkv;
import com.dianping.tkv.util.StringKit;

/**
 * Tagged key-value store implement.
 * 
 * @author sean.wang
 * @since Feb 21, 2012
 */
public class LocalImpl implements Tkv {
	private static class IndexItem {
		private int pos;

		private int bodyLength;

		private Map<String, Integer> tagPosMap;

		IndexItem(int pos, int bodyLength) {
			this.pos = pos;
			this.bodyLength = bodyLength;
		}

		void addTagPos(String tagName, int pos) {
			if (tagPosMap == null) {
				tagPosMap = new HashMap<String, Integer>();
			}
			tagPosMap.put(tagName, pos);
		}

		Integer getTagPos(String tagName) {
			if (tagPosMap == null) {
				return null;
			}
			return tagPosMap.get(tagName);
		}
	}

	private final Lock writeLock = new ReentrantLock();

	private final Lock readLock = new ReentrantLock();

	private DataStore store;

	private Map<String, IndexItem> keyValueIndex;

	private Map<String, List<String>> tagListIndex;

	public LocalImpl(File dbFile) throws IOException {
		this.store = new RAFDataStore(dbFile);
		this.keyValueIndex = new HashMap<String, IndexItem>();
		this.tagListIndex = new HashMap<String, List<String>>();
		deserial();
	}

	@Override
	public void close() throws IOException {
		this.store.close();
	}

	private Record createNewRecord(String key, byte[] value, String... tags) {
		Record newRecord = new Record();
		newRecord.setKey(key);
		newRecord.setValue(value);
		if (tags != null) {
			newRecord.setTags(tags);
			int len = 0;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < tags.length; i++) {
				String tag = tags[i];
				sb.append(tag);
				len += tag.length();
				if (i != tags.length - 1) {
					sb.append(Record.TAG_SPLITER);
					len++;
				}
			}
			newRecord.setTagsLength(len);
			newRecord.setTagsString(sb.toString());
		}
		return newRecord;
	}

	protected void deserial() throws IOException {
		DataStore store = this.store;
		int pos = 0;// record position
		try {
			writeLock.lock();
			while (pos < store.length()) {
				int keyLength = bytes2Int(store.get(pos, 4));
				pos += 4;
				int valueLength = bytes2Int(store.get(pos, 4));
				pos += 4;
				int tagsLength = bytes2Int(store.get(pos, 4));
				pos += 4;
				byte[] keyBuf = store.get(pos, keyLength);
				pos += keyLength;
				byte[] valueBuf = store.get(pos, valueLength);
				pos += valueLength;
				String key = new String(keyBuf);
				String[] tagArray = null;
				Record r = new Record();
				r.setPos(pos);
				r.setKey(key);
				r.setValue(valueBuf);
				if (tagsLength > 0) {
					byte[] tagsBuf = store.get(pos, tagsLength);
					pos += tagsLength;
					String tags = new String(tagsBuf);
					tagArray = StringKit.split(tags, Record.TAG_SPLITER);
					r.setTags(tagArray);
					r.setTagsLength(tagsLength);
					r.setTagsString(tags);
				}
				index(r);
				pos += 1; // skip ender
			}
		} finally {
			writeLock.unlock();
		}

	}

	@Override
	public byte[] get(String key) throws IOException {
		return get(key, null);
	}

	@Override
	public byte[] get(String key, String tag) throws IOException {
		Record r = getRecord(key, tag);
		return r.getValue();
	}

	@Override
	public byte[] get(int indexPos) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Meta getIndex(String key) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Meta getIndex(String key, String tag) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Meta getIndex(int indexPos) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Record getRecord(String key, String tag) throws IOException {
		Record r = new Record();
		byte[] body = null;
		IndexItem indexItem = this.keyValueIndex.get(key);
		if (indexItem == null) {
			return null;
		}
		try {
			readLock.lock();
			body = this.store.get(indexItem.pos, indexItem.bodyLength);
		} finally {
			readLock.unlock();
		}
		byte[] intBuf = new byte[4];
		System.arraycopy(body, 0, intBuf, 0, intBuf.length);
		int keyLength = bytes2Int(intBuf);
		System.arraycopy(body, 4, intBuf, 0, intBuf.length);
		byte[] keyBuf = new byte[keyLength];
		System.arraycopy(body, 12, keyBuf, 0, keyLength);
		r.setKey(new String(keyBuf));
		int valueLength = bytes2Int(intBuf);
		byte[] valueBuf = new byte[valueLength];
		System.arraycopy(body, 12 + keyLength, valueBuf, 0, valueLength);
		r.setValue(valueBuf);
		if (tag != null) {
			Integer pos = indexItem.getTagPos(tag);
			if (pos != null) {
				List<String> tagList = this.tagListIndex.get(tag);
				String nextKey = pos == tagList.size() - 1 ? null : tagList.get(pos + 1);
				String priviousKey = pos == 0 ? null : tagList.get(pos - 1);
				r.setPriviousKey(priviousKey);
				r.setNexKey(nextKey);
			}
		}
		return r;
	}

	public DataStore getStore() {
		return this.store;
	}

	/**
	 * @param pos
	 * @param keyLength
	 * @param valueLength
	 * @param tagsLength
	 * @param tagArray
	 * @param key
	 */
	private void index(Record record) {
		String key = record.getKey();
		IndexItem item = new IndexItem(record.getPos(), record.getBodyLength());
		this.keyValueIndex.put(key, item);
		String[] tagArray = record.getTags();
		if (tagArray != null) {
			for (String tag : tagArray) {
				List<String> list = this.tagListIndex.get(tag);
				if (list == null) {
					list = new LinkedList<String>();
					this.tagListIndex.put(tag, list);
				}
				list.add(key);
				item.addTagPos(tag, list.size() - 1);
			}
		}
	}

	@Override
	public boolean put(String key, byte[] value) throws IOException {
		return put(key, value, (String[]) null);
	}

	@Override
	public boolean put(String key, byte[] value, String... tags) throws IOException {
		try {
			writeLock.lock();
			if (this.keyValueIndex.containsKey(key)) {
				return false;
			}
			Record r = createNewRecord(key, value, tags);
			r.setPos((int) store.length());
			storeRecord(r);
			index(r);
		} finally {
			writeLock.unlock();
		}
		return true;
	}

	@Override
	public long size() {
		return this.keyValueIndex.size();
	}

	private void storeRecord(Record r) throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(r.getBodyLength() + 1);
		bb.put(int2Bytes(r.getKey().length()));
		bb.put(int2Bytes(r.getValue().length));
		bb.put(int2Bytes(r.getTagsLength()));
		bb.put(r.getKey().getBytes());
		bb.put(r.getValue());
		if (r.getTags() != null) {
			bb.put(r.getTagsToString().getBytes());
		}
		bb.put((byte) Record.ENDER);
		this.store.append(bb.array());
	}
}
