package com.dianping.cat.message.spi;

import java.io.File;
import java.net.URL;

public interface MessageStorage {
	public File getBaseDir();

	public URL getBaseUrl();

	/**
	 * Store a message tree to the storage.
	 * 
	 * @param tree
	 *           message tree to store
	 * @return relative path to base directory or base URL
	 */
	public String store(MessageTree tree);
}
