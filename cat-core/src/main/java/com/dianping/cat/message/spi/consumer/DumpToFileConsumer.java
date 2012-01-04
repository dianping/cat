package com.dianping.cat.message.spi.consumer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class DumpToFileConsumer implements MessageConsumer, Initializable {
	@Inject
	private File m_baseDir;

	@Override
	public void consume(MessageTree tree) {
		File file = new File(m_baseDir, tree.getDomain() + "-" + tree.getThreadId() + ".log");
		FileOutputStream fos = null;

		try {
			String str = tree.toString();

			fos = new FileOutputStream(file, true);
			fos.write(str.getBytes("utf-8"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// ignore it
				}
			}
		}

	}

	@Override
	public String getConsumerId() {
		return "dump-to-file";
	}

	@Override
	public String getDomain() {
		// no limitation
		return null;
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_baseDir == null) {
			try {
				m_baseDir = new File(".").getCanonicalFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		m_baseDir.mkdirs();
	}

	public void setBaseDir(File baseDir) {
		m_baseDir = baseDir;
	}
}
