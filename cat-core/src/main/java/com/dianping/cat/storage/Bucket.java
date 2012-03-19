package com.dianping.cat.storage;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface Bucket<T> {
	public void close() throws IOException;

	public void deleteAndCreate() throws IOException;

	public T findById(String id) throws IOException;

	public T findNextById(String id, String tag) throws IOException;
	
	public T findPreviousById(String id, String tag) throws IOException;

	public void flush() throws IOException;

	public Collection<String> getIdsByPrefix(String prefix);

	public void initialize(Class<?> type, File baseDir, String logicalPath) throws IOException;;

	public boolean storeById(String id, T data) throws IOException;;
}
