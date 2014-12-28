package com.dianping.cat.storage.report;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Scanners;
import org.unidal.helper.Scanners.FileMatcher;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;

public class DefaultReportBucketManager extends ContainerHolder implements ReportBucketManager, Initializable {

	@Inject
	private ServerConfigManager m_configManager;

	private String m_reportBaseDir;

	@Override
	public void clearOldReports() {
		Transaction t = Cat.newTransaction("System", "DeleteReport");
		try {
			File reportDir = new File(m_reportBaseDir);
			final List<String> toRemovePaths = new ArrayList<String>();
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			final String today = sdf.format(date);
			final String week = sdf.format(new Date(date.getTime() - 7 * 24 * 60 * 60 * 1000L));

			Scanners.forDir().scan(reportDir, new FileMatcher() {
				@Override
				public Direction matches(File base, String path) {
					File file = new File(base, path);
					if (file.isFile() && shouldDeleteReport(path)) {
						toRemovePaths.add(path);
					}
					return Direction.DOWN;
				}

				private boolean shouldDeleteReport(String path) {
					if (path.indexOf(today) > -1 || path.indexOf(week) > -1) {
						return false;
					} else {
						return true;
					}
				}
			});
			for (String path : toRemovePaths) {
				File file = new File(m_reportBaseDir, path);

				file.delete();
				Cat.logEvent("System", "DeleteReport", Event.SUCCESS, file.getAbsolutePath());
			}
			removeEmptyDir(reportDir);
			t.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			Cat.logError(e);
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}

	@Override
	public void closeBucket(ReportBucket<?> bucket) {
		try {
			bucket.close();
		} catch (Exception e) {
			// ignore it
		} finally {
			release(bucket);
		}
	}

	private ReportBucket<?> createBucket(Class<?> type, Date timestamp, String name, String namespace)
	      throws IOException {
		ReportBucket<?> bucket = lookup(ReportBucket.class, type.getName() + "-" + namespace);

		bucket.initialize(type, name, timestamp);
		return bucket;
	}

	@SuppressWarnings("unchecked")
	private <T> ReportBucket<T> getBucket(Class<T> type, long timestamp, String name, String namespace)
	      throws IOException {
		Date date = new Date(timestamp);
		ReportBucket<?> bucket = createBucket(type, date, name, namespace);

		return (ReportBucket<T>) bucket;
	}

	@Override
	public ReportBucket<String> getReportBucket(long timestamp, String name) throws IOException {
		return getBucket(String.class, timestamp, name, "report");
	}

	@Override
	public void initialize() throws InitializationException {
		m_reportBaseDir = m_configManager.getHdfsLocalBaseDir("report");
	}

	private void removeEmptyDir(File baseFile) {
		// the path has two depth
		for (int i = 0; i < 2; i++) {
			final List<String> directionPaths = new ArrayList<String>();

			Scanners.forDir().scan(baseFile, new FileMatcher() {
				@Override
				public Direction matches(File base, String path) {
					if (new File(base, path).isDirectory()) {
						directionPaths.add(path);
					}

					return Direction.DOWN;
				}
			});
			for (String path : directionPaths) {
				try {
					File file = new File(baseFile, path);

					file.delete();
				} catch (Exception e) {
				}
			}
		}
	}

}
