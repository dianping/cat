package com.dianping.cat.message.spi;

import java.io.File;
import java.net.URL;
import java.util.Date;

public interface MessagePathBuilder {
	public String getHdfsPath(String messageId);

	public File getLogViewBaseDir();

	public URL getLogViewBaseUrl();

	public String getLogViewPath(String messageId);

	public String getMessagePath(String domain, Date timestamp);
	
	public String getReportPath(Date timestamp);
}
