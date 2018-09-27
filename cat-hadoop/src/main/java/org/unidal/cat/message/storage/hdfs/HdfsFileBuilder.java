package org.unidal.cat.message.storage.hdfs;

import java.text.MessageFormat;
import java.util.Date;

import org.unidal.cat.message.storage.FileType;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = PathBuilder.class, value = "hdfs")
public class HdfsFileBuilder implements PathBuilder {

	@Inject
	private HdfsSystemManager m_fileSystemManager;

	@Override
	public String getPath(String domain, Date startTime, String consumerId, FileType type) {
		MessageFormat format;
		String path;

		switch (type) {
		case TOKEN:
			format = new MessageFormat("/{0,date,yyyyMMdd}/{0,date,HH}/{2}.{3}");
			path = format.format(new Object[] { startTime, null, consumerId, type.getExtension() });
			break;
		default:
			format = new MessageFormat("/{0,date,yyyyMMdd}/{0,date,HH}/{1}-{2}.{3}");
			path = format.format(new Object[] { startTime, domain, consumerId, type.getExtension() });
			break;
		}

		return m_fileSystemManager.getBaseDir() + path;
	}
}
