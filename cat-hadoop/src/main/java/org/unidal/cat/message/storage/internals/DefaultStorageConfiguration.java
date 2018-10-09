package org.unidal.cat.message.storage.internals;

import java.io.File;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.message.storage.StorageConfiguration;
import org.unidal.lookup.annotation.Named;

@Named(type = StorageConfiguration.class)
public class DefaultStorageConfiguration implements Initializable, StorageConfiguration {
	private String m_baseDataDir;

	@Override
	public String getBaseDataDir() {
		return m_baseDataDir;
	}

	@Override
	public void initialize() throws InitializationException {
		m_baseDataDir = "/data/appdatas/cat/bucket/";
	}

	@Override
	public boolean isLocalMode() {
		return true;
	}

	@Override
	public void setBaseDataDir(File baseDataDir) {
		m_baseDataDir = baseDataDir.getAbsolutePath() + '/';
	}

	@Override
	public void setBaseDataDir(String baseDataDir) {
		m_baseDataDir = baseDataDir;
	}
}
