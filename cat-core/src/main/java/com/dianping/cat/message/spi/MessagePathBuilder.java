package com.dianping.cat.message.spi;

import java.io.File;
import java.net.URL;

public interface MessagePathBuilder {
	public URL getLogViewUrl(MessageTree tree);

	public File getLogViewFile(MessageTree tree);
}
