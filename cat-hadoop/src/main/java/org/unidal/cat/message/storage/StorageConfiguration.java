package org.unidal.cat.message.storage;

import java.io.File;

public interface StorageConfiguration {
	public String getBaseDataDir();

	public void setBaseDataDir(String baseDataDir);

	public boolean isLocalMode();

	public void setBaseDataDir(File baseDataDir);
}