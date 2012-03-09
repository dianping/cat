package com.dianping.cat.storage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface Bucket<T> extends TagThreadSupport<T> {
	public void close() throws IOException;

	public void deleteAndCreate() throws IOException;

	public List<T> findAllByIds(List<String> ids) throws IOException;;

	public T findById(String id) throws IOException;;

	public void initialize(Class<?> type, File path) throws IOException;

	public boolean storeById(String id, T data) throws IOException;;

}
