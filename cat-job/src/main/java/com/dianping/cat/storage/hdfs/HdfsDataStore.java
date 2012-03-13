/**
 * 
 */
package com.dianping.cat.storage.hdfs;

import java.io.FileNotFoundException;
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
	 * @see com.dianping.cat.storage.hdfs.DataStore#append(byte)
	 */
	@Override
	public void append(byte b) throws IOException {
		this.output.write(b);
		this.length++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.DataStore#append(byte[])
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
	 * @see com.dianping.cat.storage.hdfs.DataStore#close()
	 */
	@Override
	public void close() throws IOException {
		if (this.output != null) {
			this.output.close();
		}
		if (this.input != null) {
			this.input.close();
		}
		this.fs.close();
	}

	public void startWrite() throws IOException {
		if (this.output == null) {
			this.output = this.fs.create(this.path);
		}
	}

	public void endWrite() throws IOException {
		if (this.output != null) {
			this.output.flush();
			this.output.close();
			this.output = null;
		}
	}

	public void startRead() throws IOException {
		if (this.input == null) {
			try {
				this.input = this.fs.open(this.path, 1024);
			} catch (FileNotFoundException e) {

			}
		}
	}

	public void endRead() throws IOException {
		if (this.input != null) {
			this.input.close();
			this.input = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.DataStore#get(long, int)
	 */
	@Override
	public byte[] get(long offset, int length) throws IOException {
		FSDataInputStream in = this.input;
		if (in == null) {
			return null;
		}
		byte[] bytes = new byte[length];
		in.seek(offset);
		in.read(bytes);
		return bytes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.DataStore#length()
	 */
	@Override
	public long length() throws IOException {
		return this.length;
	}

	@Override
	public boolean delete() throws IOException {
		boolean remoteDeleted = this.fs.delete(path, false);
		return remoteDeleted;
	}

}
