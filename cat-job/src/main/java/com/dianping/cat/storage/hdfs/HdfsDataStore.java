/**
 * 
 */
package com.dianping.cat.storage.hdfs;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * @author sean.wang
 * @since Mar 7, 2012
 */
public class HdfsDataStore implements DataStore {
	private long length = 0;

	private Path path;

	private FileSystem fs;

	private FSDataOutputStream output;

	private FSDataInputStream input;

	public HdfsDataStore() {

	}

	public HdfsDataStore(FileSystem fs, String hdfsFilename) throws IOException {
		this.path = new Path(fs.getWorkingDirectory(), hdfsFilename);
		this.fs = fs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.com.dianping.cat.storage.hdfs.DataStore#append(byte)
	 */
	@Override
	public void append(byte b) throws IOException {
		this.output.write(b);
		this.length++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.com.dianping.cat.storage.hdfs.DataStore#append(byte[])
	 */
	@Override
	public void append(byte[] bytes) throws IOException {
		this.output.write(bytes);
		this.length += bytes.length;
	}

	@Override
	public void append(long offset, byte[] bytes) throws IOException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.com.dianping.cat.storage.hdfs.DataStore#close()
	 */
	@Override
	public void close() throws IOException {
		this.closeOutput();
		this.closeInput();
		this.closeFlieSystem();
	}

	/**
	 * @throws IOException
	 */
	private void closeFlieSystem() throws IOException {
		if (this.fs == null) {
			this.fs.close();
			this.fs = null;
		}
	}

	public void openOutput() throws IOException {
		if (this.output == null) {
			this.output = this.fs.create(this.path);
		}
	}

	public void flushAndCloseOutput() throws IOException {
		if (this.output != null) {
			this.output.flush();
			this.closeOutput();
		}
	}

	public void closeOutput() throws IOException {
		if (this.output != null) {
			this.output.close();
			this.output = null;
		}
	}

	public void openInput() throws IOException {
		if (this.input == null) {
			this.input = this.fs.open(this.path, 1024);
		}
	}

	public void closeInput() throws IOException {
		if (this.input != null) {
			this.input.close();
			this.input = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.com.dianping.cat.storage.hdfs.DataStore#get(long, int)
	 */
	@Override
	public byte[] get(long offset, int length) throws IOException {
		FSDataInputStream in = this.input;
		if (in == null) {
			throw new IllegalStateException("input can't null");
		}
		byte[] bytes = new byte[length];
		in.seek(offset);
		in.read(bytes);
		return bytes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.com.dianping.cat.storage.hdfs.DataStore#length()
	 */
	@Override
	public long length() throws IOException {
		return this.length;
	}

	@Override
	public boolean delete() throws IOException {
		boolean localDeleted = this.deleteLocal();
		boolean remoteDeleted = this.deleteRemote();
		return localDeleted && remoteDeleted;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public boolean deleteRemote() throws IOException {
		boolean remoteDeleted = false;
		if (this.fs != null) {
			remoteDeleted = this.fs.delete(path, false);
		}
		return remoteDeleted;
	}

	public boolean deleteLocal() {
		return true;
	}

}
