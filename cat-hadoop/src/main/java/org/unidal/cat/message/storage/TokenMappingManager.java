package org.unidal.cat.message.storage;

import java.io.IOException;

public interface TokenMappingManager {
	public void close(int hour);

	public TokenMapping getTokenMapping(int hour, String ip) throws IOException;
}
