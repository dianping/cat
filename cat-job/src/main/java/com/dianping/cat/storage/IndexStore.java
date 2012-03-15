package com.dianping.cat.storage;

import java.io.IOException;
import java.util.Comparator;

public interface IndexStore {

	void append(Meta meta) throws IOException;

	void close() throws IOException;

	Meta getIndex(long indexPos) throws IOException;

	Meta getIndex(String key) throws IOException;

	Meta getIndex(String key, Comparator<byte[]> keyComp) throws IOException;

	Meta getIndex(String key, String tag) throws IOException;

	Meta getIndex(String key, String tag, Comparator<byte[]> keyComp) throws IOException;

	int getIndexLength();

	long size() throws IOException;

	long length() throws IOException;

	boolean delete() throws IOException;

	void flush() throws IOException;

}
