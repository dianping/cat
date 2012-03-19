package com.dianping.cat.job.storage;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;

public class RemoteMessageBucket implements Bucket<MessageTree> {

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAndCreate() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public MessageTree findById(String id) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageTree findNextById(String id, String tag) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageTree findPreviousById(String id, String tag) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<String> getIdsByPrefix(String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize(Class<?> type, File baseDir, String logicalPath) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean storeById(String id, MessageTree data) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

}
