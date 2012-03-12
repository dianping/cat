/**
 * 
 */
package com.dianping.tkv.local;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.dianping.tkv.DataStore;

/**
 * @author sean.wang
 * @since Nov 16, 2011
 */
public class RAFDataStore implements DataStore {

	private RandomAccessFile writeRAF;

	private RandomAccessFile readRAF;

	public RAFDataStore(File dbFile) throws IOException {
		writeRAF = new RandomAccessFile(dbFile, "rw");
		readRAF = new RandomAccessFile(dbFile, "r");
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
	public byte[] get(long pos, int size) throws IOException {
		byte[] bytes = new byte[size];
		readRAF.seek(pos);
		readRAF.read(bytes);
		return bytes;
	}

	@Override
	public long length() throws IOException {
		return readRAF.length();
	}

}
