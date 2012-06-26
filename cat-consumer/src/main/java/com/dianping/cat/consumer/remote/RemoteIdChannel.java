/**
 * 
 */
package com.dianping.cat.consumer.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
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

	public RemoteIdChannel(File baseDir, String path, long startTime) {
		m_startTime = startTime;
		m_file = new File(baseDir, path);
		m_path = path;
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
			e.printStackTrace();
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
		List<String> remoteIds = new ArrayList<String>();
		Transaction t = (Transaction) tree.getMessage();
		doTransactionChilds(remoteIds, t);

		if (remoteIds.size() == 0) {
			return;
		}

		StringBuilder sb = new StringBuilder((remoteIds.size() + 1) * remoteIds.get(0).length() + 32);
		if (t.isSuccess()) {
			sb.append('0');
		} else {
			sb.append('1');
		}
		sb.append('\t');
		sb.append(tree.getMessageId());
		sb.append('\t');
		sb.append(tree.getParentMessageId());
		sb.append('\t');
		sb.append(tree.getRootMessageId());
		for (String id : remoteIds) {
			sb.append('\t');
			sb.append(id);
		}
		sb.append('\n');

		m_output.write(sb.toString().getBytes());
	}

	public static final String PIGEON_REQUEST_NAME = "PigeonRequest";

	public static final String PIGEON_RESPONSE_NAME = "PigeonRespone";

	public static final String PIGEON_REQUEST_TYPE = "RemoteCall";

	private void doTransactionChilds(List<String> remoteIds, Transaction t) {
		if (!t.hasChildren()) {
			return;
		}
		for (Message m : t.getChildren()) {
			if (m instanceof Event && // is event
					PIGEON_REQUEST_TYPE.equals(m.getType()) && (PIGEON_REQUEST_NAME.equals(m.getName()) // is pigeon request
					|| PIGEON_RESPONSE_NAME.equals(m.getName()))) { // is pigeon response
				Event e = (Event) m;
				String requestMessageId = (String) e.getData();
				remoteIds.add(requestMessageId);
			} else if (m instanceof Transaction) {
				doTransactionChilds(remoteIds, (Transaction) m);
			}
		}
	}

}
