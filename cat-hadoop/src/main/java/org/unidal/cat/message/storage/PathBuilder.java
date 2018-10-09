package org.unidal.cat.message.storage;

import java.util.Date;

public interface PathBuilder {
	public String getPath(String domain, Date startTime, String consumerId, FileType type);

}
