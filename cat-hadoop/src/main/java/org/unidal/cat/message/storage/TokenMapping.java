package org.unidal.cat.message.storage;

import java.io.IOException;

public interface TokenMapping {
	public void close();

	public String find(int index) throws IOException;

	public long getLastAccessTime();

	public int map(String token) throws IOException;

	public void open(int hour, String ip) throws IOException;
}
