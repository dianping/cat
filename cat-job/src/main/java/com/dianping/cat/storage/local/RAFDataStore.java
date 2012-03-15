/**
 * 
 */
package com.dianping.cat.storage.local;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.dianping.cat.storage.DataStore;


/**
 * @author sean.wang
 * @since Nov 16, 2011
 */
public class RAFDataStore implements DataStore {

	private RandomAccessFile writeRAF;

	private RandomAccessFile readRAF;

	private File storeFile;

	public RAFDataStore(File dbFile) throws IOException {
		this.storeFile = dbFile;
		this.writeRAF = new RandomAccessFile(dbFile, "rw");
		this.readRAF = new RandomAccessFile(dbFile, "r");
	}

	@Override
	public void append(byte b) throws IOException {
		writeRAF.seek(writeRAF.length());
		writeRAF.write(b);
	}

	@Override
	public void append(byte[] bytes) throws IOException {
		this.append(writeRAF.length(), bytes);
	}

	@Override
	public void append(long offset, byte[] bytes) throws IOException {
		writeRAF.seek(offset);
		writeRAF.write(bytes);
	}

	@Override
	public void close() throws IOException {
		writeRAF.close();
		readRAF.close();
	}

	@Override
	public byte[] get(long offset, int length) throws IOException {
		byte[] bytes = new byte[length];
		readRAF.seek(offset);
		int actual = readRAF.read(bytes);
		if(actual != length) {
			throw new IOException(String.format("readed bytes expect %s actual %s", length, actual));
		}
		return bytes;
	}

	@Override
	public long length() throws IOException {
		return readRAF.length();
	}

	@Override
	public boolean delete() throws IOException {
		return this.storeFile.delete();
	}

}
