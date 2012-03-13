package com.dianping.cat.storage.hdfs;

import java.io.IOException;
import java.util.Comparator;

public interface IndexStore {

	void append(Meta meta) throws IOException;

	void close() throws IOException;

	Meta getIndex(int indexPos) throws IOException;

	Meta getIndex(String key) throws IOException;

	Meta getIndex(String key, Comparator<String> c) throws IOException;

	Meta getIndex(String key, String tag) throws IOException;

	Meta getIndex(String key, String tag, Comparator<String> c) throws IOException;

	int getIndexLength();

	long size() throws IOException;

	long length() throws IOException;

	boolean delete() throws IOException;

	void flush() throws IOException;

}
