package org.unidal.cat.message.storage;

import java.io.IOException;

public interface IndexManager {
	public void close(int hour);

	public Index getIndex(String domain, String ip, int hour, boolean createIfNotExists) throws IOException;
}
