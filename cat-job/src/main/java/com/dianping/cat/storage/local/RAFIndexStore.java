/**
 * 
 */
package com.dianping.cat.storage.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import com.dianping.cat.storage.IndexStore;
import com.dianping.cat.storage.Meta;
import com.dianping.cat.storage.Tag;
import com.dianping.cat.storage.util.ArrayKit;
import com.dianping.cat.storage.util.NumberKit;

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

	private long length;

	private final static int OFFSET_LEN = 4;

	private final static int LENGTH_LEN = 4;

	private byte[] toFixedKey(String key) {
		byte[] keyBytes = key.getBytes();
		int keyLength = this.keyLength;
		if (key.length() >= keyLength) {
			throw new IllegalArgumentException("key length overflow" + key);
		}

		byte[] fixed = new byte[keyLength];
		int len = keyBytes.length;
		System.arraycopy(keyBytes, 0, fixed, 0, len);
		fixed[len] = TAG_SPLITER;
		return fixed;
	}

	public RAFIndexStore(File storeFile, int keyLength, int tagLength) throws IOException {
		this.keyLength = keyLength;
		this.tagLength = tagLength;
		this.indexLength = this.keyLength + OFFSET_LEN + LENGTH_LEN + tagLength;
		this.storeFile = storeFile;
		writeRAF = new RandomAccessFile(storeFile, "rw");
		readRAF = new RandomAccessFile(storeFile, "r");
		this.length = this.readRAF.length();
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
				buff.put(TAG_SPLITER);
				buff.put(Integer.toString(t.getPrevious()).getBytes());
				buff.put(TAG_SPLITER);
				buff.put(Integer.toString(t.getNext()).getBytes());
				buff.put(TAG_SPLITER);
			}
			byte[] tagArray = buff.array();
			System.arraycopy(tagArray, 0, buf, i, tagLength);
		} else {
			buf[i] = TAG_SPLITER;
		}
		synchronized (this.writeRAF) {
			this.writeRAF.seek(this.length);
			this.writeRAF.write(buf);
			this.length += buf.length;
		}
	}

	private long binarySearchPos(String key, Comparator<byte[]> keyComp) throws IOException {
		long low = 0;
		int indexLength = this.indexLength;
		int keyLength = this.keyLength;
		long high = (int) this.length / indexLength - 1;

		while (low <= high) {
			long mid = (low + high) >>> 1;
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

	private byte[] getBytes(long offset, int length) throws IOException {
		byte[] bytes = new byte[length];
		readRAF.seek(offset);
		int actual = readRAF.read(bytes);
		if (actual != length) {
			throw new IOException(String.format("readed bytes expect %s actual %s", length, actual));
		}
		return bytes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.hdfs.IndexStore#getIndex(int)
	 */
	@Override
	public Meta getIndex(long indexPos) throws IOException {
		byte[] bytes = this.getBytes(1L * indexPos * this.indexLength, this.indexLength);
		return deserialMeta(bytes);
	}

	public Meta deserialMeta(byte[] bytes) {
		Meta m = new Meta();
		int keyLength = this.keyLength;
		int keyLast = ArrayKit.lastIndexOf(bytes, TAG_SPLITER, keyLength - 1);
		byte[] keyBytes = new byte[keyLast];
		System.arraycopy(bytes, 0, keyBytes, 0, keyBytes.length);
		m.setKey(new String(keyBytes));
		int i = keyLength;
		m.setOffset(NumberKit.bytes2Int(bytes, i));
		i += 4;
		m.setLength(NumberKit.bytes2Int(bytes, i));
		i += 4;
		int tagEnderIndex = ArrayUtils.lastIndexOf(bytes, TAG_SPLITER);
		if (tagEnderIndex > 0 && tagEnderIndex > i) {
			byte[] tagBytes = new byte[tagEnderIndex - i];
			System.arraycopy(bytes, i, tagBytes, 0, tagBytes.length);
			byte[][] tagSegs = ArrayKit.split(tagBytes, TAG_SPLITER);
			Tag t = null;
			for (int j = 0; j < tagSegs.length; j++) {
				String tagSeg = new String(tagSegs[j]);
				if (j % 3 == 0) {
					t = new Tag();
					t.setName(tagSeg);
					m.addTag(t);
				} else if (j % 3 == 1) {
					t.setPrevious(Integer.parseInt(tagSeg));
				} else {
					t.setNext(Integer.parseInt(tagSeg));
				}
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
		if (this.size() == 0) {
			return null;
		}
		synchronized (this.readRAF) {
			long pos = this.binarySearchPos(key, keyComp);
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
		if (tagName != null && (tags == null || !tags.containsKey(tagName))) {
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
		return this.length / this.indexLength;
	}

	@Override
	public int getIndexLength() {
		return this.indexLength;
	}

	@Override
	public long length() throws IOException {
		return this.length;
	}

	@Override
	public boolean delete() throws IOException {
		return this.storeFile.delete();
	}

	@Override
	public void flush() throws IOException {
		this.writeRAF.getChannel().force(false);
	}

	public InputStream getInputStream() throws FileNotFoundException {
		return new FileInputStream(storeFile);
	}

	public OutputStream getOutputStream() throws FileNotFoundException {
		return new FileOutputStream(storeFile);
	}

}
