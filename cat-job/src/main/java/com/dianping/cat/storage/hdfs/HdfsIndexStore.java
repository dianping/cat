/**
 * 
 */
package com.dianping.cat.storage.hdfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.dianping.cat.storage.IndexStore;
import com.dianping.cat.storage.Meta;
import com.dianping.cat.storage.local.RAFIndexStore;
import com.dianping.cat.storage.util.IoKit;



/**
 * @author sean.wang
 * @since Mar 7, 2012
 */
public class HdfsIndexStore implements IndexStore {
	private RAFIndexStore localIndexStore;

	private FileSystem fs;

	private Path path;

	public HdfsIndexStore(FileSystem fs, String hdfsFilename, File localFile, int keyLength, int tagLength) throws IOException {
		this.fs = fs;
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
		boolean localDeleted = this.deleteLocal();
		boolean remoteDeleted = this.deleteRemote();
		return localDeleted && remoteDeleted;
	}

	public boolean deleteLocal() throws IOException {
		return this.localIndexStore.delete();
	}

	public boolean deleteRemote() throws IOException {
		boolean remoteDeleted = false;
		if (this.fs != null) {
			remoteDeleted = this.fs.delete(path, false);
		}
		return remoteDeleted;
	}

	public void download() throws IOException {
		InputStream input = fs.open(path);
		OutputStream output = this.localIndexStore.getOutputStream();
		IoKit.copyAndClose(input, output);
	}

	@Override
	public void flush() throws IOException {
		this.localIndexStore.flush();
		InputStream input = this.localIndexStore.getInputStream();
		OutputStream output = fs.create(path);
		IoKit.copyAndClose(input, output);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.storage.hdfs.hdfs.IndexStore#getIndex(int)
	 */
	@Override
	public Meta getIndex(long indexPos) throws IOException {
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

	@Override
	public long length() throws IOException {
		return this.localIndexStore.length();
	}

	@Override
	public long size() throws IOException {
		return length() / getIndexLength();
	}

}
