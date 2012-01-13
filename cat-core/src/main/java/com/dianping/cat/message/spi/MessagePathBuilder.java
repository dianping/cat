package com.dianping.cat.message.spi;

import java.io.File;
import java.net.URL;

public interface MessagePathBuilder {
	public File getLogViewBaseDir();

	public URL getLogViewBaseUrl();

	public String getLogViewPath(MessageTree tree);
}
