package com.dianping.cat.message.spi;

import java.io.File;
import java.net.URL;

public interface MessagePathBuilder {
	public String getHdfsPath(String messageId);

	public File getLogViewBaseDir();

	public URL getLogViewBaseUrl();

	public String getLogViewPath(String messageId);
}
