package com.dianping.cat.agent;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultMessageManager;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Splitters;

public class MmapConsumerTask implements Task, Initializable, LogEnabled {
	@Inject
	private ClientConfigManager m_configManager;

	@Inject
	private DefaultMessageManager m_messageManager;

	private QueueDescriptor m_descriptor;

	private QueueReader m_reader;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	public void initialize() {
		String mmapName = m_configManager.getMmapName();

		m_descriptor = new QueueDescriptor(new File(mmapName + ".idx"));
		m_reader = new QueueReader(new File(mmapName + ".dat"));
	}

	@Override
	public void run() {
		Cat.setup(null);

		try {
			m_descriptor.ensureOpen(0, 24);
			m_reader.ensureOpen(m_descriptor.getReaderIndex(), m_descriptor.getQueueSize());

			while (true) {
				MessageTree tree = m_reader.next();

				if (tree == null) {
					continue;
				}

				m_messageManager.flush(tree);
			}
		} catch (InterruptedException e) {
			// ignore it
		} finally {
			Cat.reset();
		}
	}

	@Override
	public void shutdown() {
	}

	class QueueDescriptor {
		private File m_file;

		private MappedByteBuffer m_buffer;

		public QueueDescriptor(File file) {
			m_file = file;
		}

		public void ensureOpen(long position, long size) throws InterruptedException {
			boolean first = true;

			while (true) {
				if (m_file.canRead()) {
					if (m_buffer == null && position < size) {
						RandomAccessFile raf = null;

						try {
							if (first) {
								m_logger.info(String.format("Opening mmap index file %s ...", m_file.getCanonicalPath()));
							}

							raf = new RandomAccessFile(m_file, "rw");
							m_buffer = raf.getChannel().map(MapMode.READ_WRITE, 0, size);
							m_buffer.load();
							m_buffer.position((int) position);
							m_buffer.order(ByteOrder.LITTLE_ENDIAN);
							break;
						} catch (IOException e) {
							if (first) {
								e.printStackTrace();
								first = false;
							}
						} finally {
							try {
								raf.close(); // we don't need it any more
							} catch (IOException e) {
								// ignore it
							}
						}
					}
				}

				TimeUnit.MILLISECONDS.sleep(100); // sleep 100ms
			}
		}

		public long getQueueSize() {
			return m_buffer.getLong(0);
		}

		public long getReaderIndex() {
			return m_buffer.getLong(16);
		}

		public long getWriterIndex() {
			return m_buffer.getLong(8);
		}

		public void setReaderIndex(long newIndex) {
			m_buffer.putLong(16, newIndex);
		}

		@Override
		public String toString() {
			return String.format("%s[size=%s, writerIndex=%s, readerIndex=%s, file=%s]", getClass().getSimpleName(),
			      getQueueSize(), getWriterIndex(), getReaderIndex(), m_file);
		}
	}

	class QueueReader {
		private File m_file;

		private MappedByteBuffer m_buffer;

		public QueueReader(File file) {
			m_file = file;
		}

		public void ensureOpen(long position, long size) throws InterruptedException {
			boolean first = true;

			while (true) {
				if (m_file.canRead()) {
					if (m_buffer == null) {
						RandomAccessFile raf = null;

						try {
							if (first) {
								m_logger.info(String.format("Opening mmap data file %s ...", m_file.getCanonicalPath()));
							}

							raf = new RandomAccessFile(m_file, "r");
							m_buffer = raf.getChannel().map(MapMode.READ_ONLY, 0, size);
							m_buffer.load();
							m_buffer.position((int) position);
							m_buffer.order(ByteOrder.LITTLE_ENDIAN);
							break;
						} catch (IOException e) {
							if (first) {
								e.printStackTrace();
								first = false;
							}
						} finally {
							try {
								raf.close(); // we don't need it any more
							} catch (IOException e) {
								// ignore it
							}
						}
					}
				}

				TimeUnit.MILLISECONDS.sleep(100); // sleep 100ms
			}
		}

		private String getDomainByMessageId(String id, String defaultValue) {
			try {
				return MessageId.parse(id).getDomain();
			} catch (RuntimeException e) {
				return defaultValue;
			}
		}

		private DefaultEvent newEvent(String type, String name, String data) {
			DefaultEvent event = new DefaultEvent(type, name);

			if (data != null) {
				event.addData(data);
			}

			event.setStatus(Message.SUCCESS);
			event.complete();
			return event;
		}

		public MessageTree next() throws InterruptedException {
			MessageTree tree = null;
			List<String> list = new ArrayList<String>(16);
			StringBuilder sb = new StringBuilder(1024);
			String childId = null;
			int step = 0;

			while (true) {
				sb.setLength(0);
				readLine(sb);

				if (sb.length() == 0) {
					break;
				}

				String line = sb.toString();

				list.clear();
				Splitters.by('\t').split(line, list);

				int size = list.size();
				int index = 0;

				switch (step) {
				case 0:
					if (size >= 3) {
						String id = list.get(index++);
						String parentId = list.get(index++);
						String rootId = list.get(index++);

						tree = m_messageManager.getThreadLocalMessageTree().copy();

						if (id == null || id.length() == 0) {
							tree.setMessageId(Cat.createMessageId());
						} else {
							childId = id;
							tree.setMessageId(parentId);
							tree.setParentMessageId(rootId);
							tree.setRootMessageId(rootId);
						}

						tree.setDomain(getDomainByMessageId(id, tree.getDomain()));
						step++;
					}
					break;
				case 1:
					if (size >= 13) {
						String name = list.get(index++);
						String status = list.get(index++);
						String url = list.get(index++);
						String requestHeaderLen = list.get(index++);
						String upstreamUrl = list.get(index++);
						String responseHeaderLen = list.get(index++);
						String responseBodyLen = list.get(index++);
						String responseBodyBlocks = list.get(index++);
						long t0 = toLong(list.get(index++));
						long t1 = toLong(list.get(index++));
						long t2 = toLong(list.get(index++));
						long t3 = toLong(list.get(index++));
						long t4 = toLong(list.get(index++));

						DefaultTransaction t = new DefaultTransaction(name, url, m_messageManager);

						t.addChild(newEvent(name + ".Status2", status, null));

						if (childId != null && childId.length() > 0) {
							t.addChild(newEvent("RemoteCall", upstreamUrl, childId));
						}

						if (t1 >= t0) {
							t.addData("_m", (t1 - t0) + "," + (t2 - t1) + "," + (t3 - t2) + "," + (t4 - t3));
						}

						t.addData("in", requestHeaderLen);
						t.addData("out", responseHeaderLen + "," + responseBodyLen);
						t.addData("blocks", responseBodyBlocks);
						t.addData("url", upstreamUrl);

						if ("200".equals(status)) {
							t.setStatus(Message.SUCCESS);
						} else {
							t.setStatus(status);
						}

						t.setDurationInMillis(t4 - t0);
						t.setCompleted(true);
						tree.setMessage(t);
						step++;
					}

					break;
				default:
					// shouldn't go here
					System.err.println("Unexpected line: " + line + ".");
					break;
				}
			}

			return tree;
		}

		private String readLine(StringBuilder sb) throws InterruptedException {
			int size = m_buffer.limit();
			long readerIndex = m_descriptor.getReaderIndex();

			try {
				while (true) {
					long writerIndex = m_descriptor.getWriterIndex();

					if (readerIndex == writerIndex) {
						// buffer is empty
						TimeUnit.MILLISECONDS.sleep(10);
						continue;
					}

					if (readerIndex > writerIndex) {
						while (readerIndex < size) {
							byte b = m_buffer.get();

							readerIndex++;

							if (b == '\n') {
								return sb.toString();
							} else {
								sb.append((char) (b & 0xFF));
							}
						}

						readerIndex = 0;
						m_buffer.rewind();
					}

					while (readerIndex < writerIndex) {
						byte b = m_buffer.get();

						readerIndex++;

						if (b == '\n') {
							return sb.toString();
						} else {
							sb.append((char) (b & 0xFF));
						}
					}
				}
			} finally {
				m_descriptor.setReaderIndex(readerIndex);
			}
		}

		private long toLong(String str) {
			long value = 0;
			int len = str == null ? 0 : str.length();

			for (int i = 0; i < len; i++) {
				char ch = str.charAt(i);

				value = value * 10 + (ch - '0');
			}

			return value;
		}

		@Override
		public String toString() {
			return String.format("%s[file=%s]", getClass().getSimpleName(), m_file);
		}
	}
}
