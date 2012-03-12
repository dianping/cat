package com.dianping.tkv;

import java.io.IOException;

/**
 * @author sean.wang
 * @since Nov 16, 2011
 */
public interface DataStore {
	byte[] get(long startIndex, int size) throws IOException;

	void append(byte[] bytes) throws IOException;
	
	void append(long offset, byte[] bytes) throws IOException;
	
	void append(byte b) throws IOException;

	void close() throws IOException;

	long length() throws IOException;

}
