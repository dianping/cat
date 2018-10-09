package org.unidal.cat.message.storage.hdfs;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeHelper;

@Named(type = MessageConsumerFinder.class, value = "hdfs")
public class HdfsMessageConsumerFinder implements MessageConsumerFinder {

	@Inject
	private HdfsSystemManager m_fileSystemManager;

	private Map<String, Set<String>> m_caches = new HashMap<String, Set<String>>();

	@Override
	public Set<String> findConsumerIps(final String domain, int hour) {
		String key = domain + '-' + hour;
		Set<String> ips = m_caches.get(key);

		if (ips == null) {
			synchronized (m_caches) {
				ips = m_caches.get(key);

				if (ips == null) {
					ips = findfromHdfs(domain, hour);
					m_caches.put(key, ips);
				}
			}
		}

		return ips;
	}

	private Set<String> findfromHdfs(final String domain, int hour) {
		Date start = new Date(hour * TimeHelper.ONE_HOUR);
		MessageFormat format = new MessageFormat("/{0,date,yyyyMMdd}/{0,date,HH}");
		String parent = m_fileSystemManager.getBaseDir() + format.format(new Object[] { start });

		FileSystem fs;

		try {
			fs = m_fileSystemManager.getFileSystem();
		} catch (IOException e) {
			Cat.logError(e);
			return null;
		}

		final Set<String> result = new HashSet<String>();

		try {
			final Path basePath = new Path(parent);

			if (fs != null) {
				fs.listStatus(basePath, new PathFilter() {
					@Override
					public boolean accept(Path p) {
						String name = p.getName();

						if (name.contains(domain) && name.endsWith(".dat")) {
							int start = name.lastIndexOf('-');
							int end = name.length() - 4;

							result.add(name.substring(start + 1, end));
						}
						return false;
					}
				});
			}
		} catch (IOException e) {
			Cat.logError(e);
		}
		return result;
	}

}
