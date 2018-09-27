package org.unidal.cat.message.storage;

import java.io.File;

public interface StorageConfiguration {
	public String getBaseDataDir();

	public boolean isLocalMode();

	public void setBaseDataDir(File baseDataDir);
	
	public void setBaseDataDir(String baseDataDir);
}