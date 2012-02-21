package com.dianping.cat.storage.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.storage.MessageBucket;
import com.site.helper.Joiners;
import com.site.helper.Splitters;
import com.site.lookup.annotation.Inject;

public class DefaultMessageBucket implements MessageBucket, LogEnabled {
	private static final String[] EMPTY = new String[0];

	@Inject
	private MessageCodec m_codec;

	@Inject
	private String m_baseDir;

	// key => offset of record
	private Map<String, Long> m_idToOffsets = new HashMap<String, Long>();

	// tag => list of ids
	private Map<String, List<String>> m_tagToIds = new HashMap<String, List<String>>();

	private File m_file;

	private RandomAccessFile m_out;

	private Logger m_logger;

	@Override
	public void close() {
		try {
			m_out.close();
			m_idToOffsets.clear();
			m_tagToIds.clear();
		} catch (IOException e) {
			// ignore it
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public List<MessageTree> findAllByIds(List<String> ids) {
		List<MessageTree> list = new ArrayList<MessageTree>(ids.size());

		for (String id : ids) {
			list.add(findById(id));
		}

		return list;
	}

	@Override
	public List<String> findAllIdsByTag(String tag) {
		List<String> ids = m_tagToIds.get(tag);

		if (ids == null) {
			return Collections.emptyList();
		} else {
			return ids;
		}
	}

	@Override
	public MessageTree findById(String id) {
		Long offset = m_idToOffsets.get(id);

		if (offset != null) {
			try {
				long old = m_out.getFilePointer();

				m_out.seek(offset);
				m_out.readLine(); // first line is header, get rid of it

				int num = Integer.parseInt(m_out.readLine());
				byte[] data = new byte[num];

				m_out.readFully(data);

				ChannelBuffer buf = ChannelBuffers.wrappedBuffer(data);
				MessageTree tree = new DefaultMessageTree();

				m_codec.decode(buf, tree);
				m_out.seek(old);

				return tree;
			} catch (Exception e) {
				m_logger.error(String.format("Error when reading file(%s)!", m_file), e);
			}
		}

		return null;
	}

	@Override
	public MessageTree findNextById(String id, Direction direction, String tag) {
		List<String> ids = m_tagToIds.get(tag);

		if (ids != null) {
			int index = ids.indexOf(id);

			switch (direction) {
			case FORWARD:
				index++;
				break;
			case BACKWARD:
				index--;
				break;
			}

			if (index >= 0 && index < ids.size()) {
				String nextId = ids.get(index);

				return findById(nextId);
			}
		}

		return null;
	}

	@Override
	public void initialize(Class<?> type, String path) throws IOException {
		m_file = new File(m_baseDir, path);
		m_file.getParentFile().mkdirs();
		m_out = new RandomAccessFile(m_file, "rw");

		if (m_file.exists()) {
			loadIndexes();
			m_out.seek(m_out.length());
		}
	}

	protected void loadIndexes() throws IOException {
		byte[] data = new byte[8192];

		while (true) {
			long offset = m_out.getFilePointer();
			String first = m_out.readLine();

			if (first == null) { // EOF
				break;
			}

			int num = Integer.parseInt(m_out.readLine());

			if (num > data.length) {
				int newSize = data.length;

				while (newSize < num) {
					newSize += newSize / 2;
				}

				data = new byte[newSize];
			}

			m_out.readFully(data, 0, num); // get rid of it
			m_out.readLine(); // get rid of empty line

			List<String> parts = Splitters.by('\t').split(first);
			if (parts.size() > 0) {
				String id = parts.get(0);

				parts.remove(0);
				updateIndex(id, parts.toArray(new String[0]), offset);
			}
		}
	}

	public void setBaseDir(String baseDir) {
		m_baseDir = baseDir;
	}

	public void setCodec(MessageCodec codec) {
		m_codec = codec;
	}

	@Override
	public boolean storeById(String id, MessageTree tree) {
		return storeById(id, tree, EMPTY);
	}

	/**
	 * Store the message in the format of:<br>
	 * 
	 * <xmp>
	 * <id>\t<tag1>\t<tag2>\t...\n
	 * <length of message>\n
	 * <message>\n
	 * </xmp>
	 */
	@Override
	public boolean storeById(String id, MessageTree tree, String... tags) {
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);

		m_codec.encode(tree, buf);

		int length = buf.readInt();
		String attributes = id + "\t" + Joiners.by('\t').join(tags) + "\n";

		try {
			byte[] first = attributes.getBytes("utf-8");
			byte[] num = String.valueOf(length).getBytes("utf-8");
			long offset = m_out.getFilePointer();

			m_out.write(first);
			m_out.write(num);
			m_out.write('\n');
			m_out.write(buf.array(), buf.readerIndex(), length);
			m_out.write('\n');
			m_out.getChannel().force(false);

			updateIndex(id, tags, offset);

			return true;
		} catch (Exception e) {
			m_logger.error(String.format("Error when writing to file(%s)!", m_file), e);

			return false;
		}
	}

	protected void updateIndex(String id, String[] tags, long offset) {
		m_idToOffsets.put(id, offset);

		for (String tag : tags) {
			List<String> ids = m_tagToIds.get(tag);

			if (ids == null) {
				ids = new ArrayList<String>();
				m_tagToIds.put(tag, ids);
			}

			ids.add(id);
		}
	}
}
