package com.dianping.cat.consumer.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.dianping.cat.Cat;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Files;

/**
 * @author sean.wang
 * @since Jun 26, 2012
 */
public class RemoteIdChannel {

	private long m_startTime;

	private File m_file;

	private String m_path;

	private OutputStream m_output;

	public RemoteIdChannel(File baseDir, String path, long startTime) throws FileNotFoundException {
		m_startTime = startTime;
		m_file = new File(baseDir, path);
		m_path = path;

		m_file.getParentFile().mkdirs();
		m_output = new FileOutputStream(m_file);
	}

	public long getStartTime() {
		return m_startTime;
	}

	public File getFile() {
		return m_file;
	}

	public void close() {
		try {
			m_output.close();
		} catch (IOException e) {
			Cat.logError(e);
		}
	}

	public void moveTo(File anotherBase) throws IOException {
		File target = new File(anotherBase, m_path);

		target.getParentFile().mkdirs();

		boolean success = m_file.renameTo(target);

		if (!success) {
			Files.forIO().copy(new FileInputStream(m_file), new FileOutputStream(target));
			m_file.delete();
		}
	}

	public void write(MessageTree tree) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(tree.getMessageId());
		sb.append('\t');
		sb.append(tree.getRootMessageId());
		sb.append('\n');
		synchronized (m_output) {
			m_output.write(sb.toString().getBytes());
		}
	}
}
