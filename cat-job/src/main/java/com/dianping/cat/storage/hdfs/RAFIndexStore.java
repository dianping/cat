/**
 * 
 */
package com.dianping.cat.storage.hdfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import com.dianping.cat.storage.hdfs.util.ArrayKit;
import com.dianping.cat.storage.hdfs.util.NumberKit;

/**
 * @author sean.wang
 * @since Mar 7, 2012
 */
public class RAFIndexStore implements IndexStore {
	private static final byte TAG_SPLITER = (byte) '\t';

	private final RandomAccessFile writeRAF;

	private final RandomAccessFile readRAF;

	private final int keyLength;

	private final int tagLength;

	private final int indexLength;

	private File storeFile;

	private final static int OFFSET_LEN = 4;

	private final static int LENGTH_LEN = 4;

	private byte[] toFixedKey(String key) {
		byte[] keyBytes = key.getBytes();
		int keyLength = this.keyLength;
		if (key.length() == keyLength) {
			return keyBytes;
		} else if (key.length() > keyLength) {
			throw new IllegalArgumentException("key length overflow" + key);
		}

		byte[] fixed = new byte[keyLength];
		System.arraycopy(keyBytes, 0, fixed, 0, keyBytes.length);
		return fixed;
	}

	public RAFIndexStore(File storeFile, int keyLength, int tagLength) throws FileNotFoundException {
		this.keyLength = keyLength;
		this.tagLength = tagLength;
		this.indexLength = this.keyLength + OFFSET_LEN + LENGTH_LEN + tagLength;
		this.storeFile = storeFile;
		writeRAF = new RandomAccessFile(storeFile, "rw");
		readRAF = new RandomAccessFile(storeFile, "r");
	}

	@Override
	public void append(Meta meta) throws IOException {
		Map<String, Tag> tags = meta.getTags();
		long offset = meta.getOffset();
		int length = meta.getLength();
		byte[] buf = new byte[this.indexLength];
		byte[] keyBytes = toFixedKey(meta.getKey());
		System.arraycopy(keyBytes, 0, buf, 0, keyBytes.length);
		int i = keyBytes.length;
		buf[i++] = (byte) (offset >>> 24);
		buf[i++] = (byte) (offset >>> 16);
		buf[i++] = (byte) (offset >>> 8);
		buf[i++] = (byte) offset;
		buf[i++] = (byte) (length >>> 24);
		buf[i++] = (byte) (length >>> 16);
		buf[i++] = (byte) (length >>> 8);
		buf[i++] = (byte) length;
		if (tags != null) {
			ByteBuffer buff = ByteBuffer.allocate(tagLength);
			for (Tag t : tags.values()) {
				buff.put(t.getName().getBytes());
				buff.put(NumberKit.int2Bytes(t.getPrevious()));
				buff.put(NumberKit.int2Bytes(t.getNext()));
				buff.put(TAG_SPLITER);
			}
			byte[] tagArray = buff.array();
			System.arraycopy(tagArray, 0, buf, i, tagLength);
		}
		synchronized (this.writeRAF) {
			this.writeRAF.seek(writeRAF.length());
			this.writeRAF.write(buf);
		}
	}

	private int binarySearchPos(String key, Comparator<byte[]> keyComp) throws IOException {
		int low = 0;
		int indexLength = this.indexLength;
		int keyLength = this.keyLength;
		int high = (int) this.readRAF.length() / indexLength;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			byte[] midVal = this.getBytes(mid * indexLength, keyLength);
			int cmp = keyComp.compare(midVal, toFixedKey(key));

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.hdfs.IndexStore#close()
	 */
	@Override
	public void close() throws IOException {
		synchronized (writeRAF) {
			this.writeRAF.close();
		}
		synchronized (readRAF) {
			this.readRAF.close();
		}
	}

	private byte[] getBytes(long pos, int size) throws IOException {
		byte[] bytes = new byte[size];
		readRAF.seek(pos);
		readRAF.read(bytes);
		return bytes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.hdfs.IndexStore#getIndex(int)
	 */
	@Override
	public Meta getIndex(int indexPos) throws IOException {
		this.readRAF.seek(indexPos * this.indexLength);
		byte[] bytes = new byte[this.indexLength];
		this.readRAF.read(bytes);
		return deserialMeta(bytes);
	}

	private Meta deserialMeta(byte[] bytes) {
		Meta m = new Meta();
		int keyLength = this.keyLength;
		byte[] keyBytes = new byte[keyLength];
		System.arraycopy(bytes, 0, keyBytes, 0, keyLength);
		m.setKey(new String(keyBytes));
		int i = keyLength;
		m.setOffset(NumberKit.bytes2Int(bytes, i));
		i += 4;
		m.setLength(NumberKit.bytes2Int(bytes, i));
		i += 4;
		int tagEnderIndex = ArrayUtils.lastIndexOf(bytes, TAG_SPLITER);
		if (tagEnderIndex > 0) {
			byte[] tagBytes = new byte[tagEnderIndex - i];
			System.arraycopy(bytes, i, tagBytes, 0, tagBytes.length);
			byte[][] tagSegs = ArrayKit.split(tagBytes, TAG_SPLITER);
			for (byte[] tagSeg : tagSegs) {
				byte[] nameSeg = new byte[tagSeg.length - 8];
				System.arraycopy(tagSeg, 0, nameSeg, 0, nameSeg.length);
				Tag t = new Tag();
				t.setName(new String(nameSeg));
				int j = nameSeg.length;
				int previous = NumberKit.bytes2Int(tagSeg, j);
				t.setPrevious(previous);
				j += 4;
				int next = NumberKit.bytes2Int(tagSeg, j);
				t.setNext(next);
				m.addTag(t);
			}
		}
		return m;
	}

	private Comparator<byte[]> defaultKeyComparator = new Comparator<byte[]>() {

		@Override
		public int compare(byte[] o1, byte[] o2) {
			if (o1.length != o2.length) {
				throw new IllegalArgumentException("byte[] length must equals:" + o1.length + ":" + o2.length);
			}
			for (int i = 0; i < o1.length; i++) {
				byte b1 = o1[i];
				byte b2 = o2[i];
				if (b1 != b2) {
					return b1 - b2;
				}
			}
			return 0;
		}
	};

	@Override
	public Meta getIndex(String key) throws IOException {
		return this.getIndex(key, null, defaultKeyComparator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.hdfs.IndexStore#getIndex(java.lang.String)
	 */
	@Override
	public Meta getIndex(String key, Comparator<byte[]> keyComp) throws IOException {
		synchronized (this.readRAF) {
			int pos = this.binarySearchPos(key, keyComp);
			if (pos < 0) {
				return null;
			}
			return this.getIndex(pos);
		}
	}

	@Override
	public Meta getIndex(String key, String tagName) throws IOException {
		return this.getIndex(key, tagName, defaultKeyComparator);
	}

	@Override
	public Meta getIndex(String key, String tagName, Comparator<byte[]> keyComp) throws IOException {
		Meta meta = this.getIndex(key, keyComp);
		if (meta == null) {
			return null;
		}
		Map<String, Tag> tags = meta.getTags();
		if (tagName != null && (tags != null && tags.get(tagName) == null)) {
			return null;
		}
		return meta;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.hdfs.IndexStore#size()
	 */
	@Override
	public long size() throws IOException {
		return this.length() / this.indexLength;
	}

	@Override
	public int getIndexLength() {
		return this.indexLength;
	}

	@Override
	public long length() throws IOException {
		return this.readRAF.length();
	}

	@Override
	public boolean delete() throws IOException {
		return this.storeFile.delete();
	}

	@Override
	public void flush() throws IOException {
	}

}
