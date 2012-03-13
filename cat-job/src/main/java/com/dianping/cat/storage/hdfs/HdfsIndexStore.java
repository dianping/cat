/**
 * 
 */
package com.dianping.cat.storage.hdfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.dianping.cat.storage.hdfs.util.IoKit;

/**
 * @author sean.wang
 * @since Mar 7, 2012
 */
public class HdfsIndexStore implements IndexStore {
	private RAFIndexStore localIndexStore;

	private FileSystem fs;

	private Path path;

	private File localFile;

	public HdfsIndexStore() {

	}

	public HdfsIndexStore(FileSystem fs, String hdfsFilename, File localFile, int keyLength, int tagLength) throws IOException {
		this.fs = fs;
		this.localFile = localFile;
		this.localIndexStore = new RAFIndexStore(localFile, keyLength, tagLength);
		this.path = new Path(fs.getWorkingDirectory(), hdfsFilename);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.IndexStore#append(com.dianping.cat.storage.hdfs.Meta)
	 */
	@Override
	public void append(Meta meta) throws IOException {
		this.localIndexStore.append(meta);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.hdfs.IndexStore#close()
	 */
	@Override
	public void close() throws IOException {
		this.localIndexStore.close();
		this.fs.close();
	}

	@Override
	public boolean delete() throws IOException {
		boolean localDeleted = this.localIndexStore.delete();
		boolean remoteDeleted = this.fs.delete(path, false);
		return localDeleted && remoteDeleted;
	}

	public void download() throws IOException {
		InputStream input = fs.open(path);
		OutputStream output = new FileOutputStream(this.localFile);
		IoKit.copyAndClose(input, output);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.hdfs.IndexStore#getIndex(int)
	 */
	@Override
	public Meta getIndex(int indexPos) throws IOException {
		return this.localIndexStore.getIndex(indexPos);
	}

	@Override
	public Meta getIndex(String key) throws IOException {
		return this.localIndexStore.getIndex(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.hdfs.IndexStore#getIndex(java.lang.String)
	 */
	@Override
	public Meta getIndex(String key, Comparator<byte[]> keyComp) throws IOException {
		return this.localIndexStore.getIndex(key, keyComp);
	}

	@Override
	public Meta getIndex(String key, String tagName) throws IOException {
		return this.localIndexStore.getIndex(key, tagName);
	}

	@Override
	public Meta getIndex(String key, String tagName, Comparator<byte[]> keyComp) throws IOException {
		return this.localIndexStore.getIndex(key, tagName, keyComp);
	}

	@Override
	public int getIndexLength() {
		return this.localIndexStore.getIndexLength();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.hdfs.IndexStore#size()
	 */
	@Override
	public long size() throws IOException {
		return length() / getIndexLength();
	}

	@Override
	public void flush() throws IOException {
		InputStream input = new FileInputStream(this.localFile);
		OutputStream output = fs.create(path);
		IoKit.copyAndClose(input, output);
	}

	@Override
	public long length() throws IOException {
		return this.fs.getFileStatus(path).getLen();
	}

}
