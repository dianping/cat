package com.dianping.cat.message;

import io.netty.buffer.ByteBuf;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.helper.Reflects;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.PlexusContainer;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.io.MessageSender;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;

public class MessageTest extends ComponentTestCase {
	private Queue<MessageTree> m_queue = new LinkedBlockingQueue<MessageTree>();

	private void checkMessage(String expected) {
		StringBuilder sb = new StringBuilder(1024);
		MessageCodec codec = new MockMessageCodec(sb);

		while (true) {
			MessageTree tree = m_queue.poll();

			if (tree != null) {
				codec.encode(tree, null);
			} else {
				break;
			}
		}
		
		Assert.assertEquals(expected, sb.toString());
	}

	protected File getConfigurationFile() {
		try {
			ClientConfig config = new ClientConfig();

			config.setMode("client");
			config.addDomain(new Domain("cat").setMaxMessageSize(8));
			config.addServer(new Server("localhost"));

			File file = new File("target/cat-config.xml");

			Files.forIO().writeTo(file, config.toString());
			return file;
		} catch (IOException e) {
			throw new RuntimeException("Unable to create cat-config.xml file!");
		}
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		defineComponent(TransportManager.class, null, MockTransportManager.class);

		MockTransportManager transportManager = (MockTransportManager) lookup(TransportManager.class);
		transportManager.setQueue(m_queue);

		File configurationFile = getConfigurationFile();
		Cat.initialize(configurationFile);

		ClientConfigManager configManager = lookup(ClientConfigManager.class);
		configManager.initialize(configurationFile);

		m_queue.clear();
		
		Reflects.forMethod().invokeDeclaredMethod(Cat.getInstance(), "setContainer", PlexusContainer.class, getContainer());
	}

	@Test
	public void testEvent() {
		Event event = Cat.getProducer().newEvent("Review", "New");

		event.addData("id", 12345);
		event.addData("user", "john");
		event.setStatus(Message.SUCCESS);
		event.complete();

		checkMessage("E Review New 0 id=12345&user=john\n");
	}

	@Test
	public void testHeartbeat() {
		Heartbeat heartbeat = Cat.getProducer().newHeartbeat("System", "Status");

		heartbeat.addData("ip", "192.168.10.111");
		heartbeat.addData("host", "host-1");
		heartbeat.addData("load", "2.1");
		heartbeat.addData("cpu", "0.12,0.10");
		heartbeat.addData("memory.total", "2G");
		heartbeat.addData("memory.free", "456M");
		heartbeat.setStatus(Message.SUCCESS);
		heartbeat.complete();

		checkMessage("H System Status 0 ip=192.168.10.111&host=host-1&load=2.1&cpu=0.12,0.10&memory.total=2G&memory.free=456M\n");
	}

	@Test
	public void testMessageTruncatedForDuration() throws IOException {
		Transaction t = Cat.newTransaction("URL", "MyPage");

		try {
			// do your business here
			t.addData("k1", "v1");

			for (int i = 0; i < 3; i++) {
				Cat.logEvent("Event", "Name" + i);
			}

			Transaction t1 = Cat.newTransaction("URL1", "MyPage");

			t1.setStatus(Message.SUCCESS);
			t1.complete();

			// move root transaction to one hour ago
			((DefaultTransaction) t).setTimestamp(t.getTimestamp() - 3600 * 1000L + 1);

			Transaction t2 = Cat.newTransaction("URL2", "MyPage");

			for (int i = 0; i < 3; i++) {
				Cat.logEvent("Event2", "Name" + i);
			}

			t2.setStatus(Message.SUCCESS);
			t2.complete();

			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}

		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("message-truncated-for-duration.txt"), "utf-8");

		checkMessage(expected);
	}

	@Test
	public void testMessageTruncatedForSize() throws IOException {
		Transaction t = Cat.newTransaction("URL", "MyPage");

		try {
			// do your business here
			t.addData("k1", "v1");
			for (int i = 0; i < 20; i++) {
				Transaction t0 = Cat.newTransaction("URL0", "MyPage");

				t0.setStatus(Message.SUCCESS);
				t0.complete();
			}

			Transaction t1 = Cat.newTransaction("URL1", "MyPage");
			Transaction t2 = Cat.newTransaction("URL2", "MyPage");

			for (int i = 0; i < 20; i++) {
				Cat.logEvent("Event", "Name" + i);
			}
			t2.complete();
			t1.complete();

			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}

		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("message-truncated-for-size.txt"), "utf-8");

		checkMessage(expected);
	}

	@Test
	public void testTransaction() throws Exception {
		Transaction t = Cat.newTransaction("URL", "MyPage");

		try {
			// do your business here
			t.addData("k1", "v1");
			t.addData("k2", "v2");
			t.addData("k3", "v3");

			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}

		checkMessage("A URL MyPage 0 k1=v1&k2=v2&k3=v3\n");
	}

	protected static class MockMessageCodec implements MessageCodec {
		private StringBuilder m_sb;

		public MockMessageCodec(StringBuilder sb) {
			m_sb = sb;
		}

		@Override
		public MessageTree decode(ByteBuf buf) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void decode(ByteBuf buf, MessageTree tree) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void encode(MessageTree tree, ByteBuf buf) {
			encodeMessage(tree.getMessage(), buf);
		}

		private void encodeEvent(Event e, ByteBuf buf) {
			m_sb.append('E');
			m_sb.append(' ').append(e.getType());
			m_sb.append(' ').append(e.getName());
			m_sb.append(' ').append(e.getStatus());

			if (!e.getType().equals("RemoteCall") && !e.getType().equals("TruncatedTransaction")) {
				m_sb.append(' ').append(e.getData());
			}

			m_sb.append('\n');

		}

		private void encodeHeartbeat(Heartbeat h, ByteBuf buf) {
			m_sb.append('H');
			m_sb.append(' ').append(h.getType());
			m_sb.append(' ').append(h.getName());
			m_sb.append(' ').append(h.getStatus());
			m_sb.append(' ').append(h.getData());
			m_sb.append('\n');
		}

		private void encodeMessage(Message message, ByteBuf buf) {
			if (message instanceof Transaction) {
				encodeTransaction((Transaction) message, buf);
			} else if (message instanceof Event) {
				encodeEvent((Event) message, buf);
			} else if (message instanceof Heartbeat) {
				encodeHeartbeat((Heartbeat) message, buf);
			}
		}

		private void encodeTransaction(Transaction t, ByteBuf buf) {
			List<Message> children = t.getChildren();

			if (children.isEmpty()) {
				m_sb.append('A');
				m_sb.append(' ').append(t.getType());
				m_sb.append(' ').append(t.getName());
				m_sb.append(' ').append(t.getStatus());
				m_sb.append(' ').append(t.getData());
				m_sb.append('\n');
			} else {
				m_sb.append('t');
				m_sb.append(' ').append(t.getType());
				m_sb.append(' ').append(t.getName());
				m_sb.append('\n');

				for (Message message : children) {
					encodeMessage(message, buf);
				}

				m_sb.append('T');
				m_sb.append(' ').append(t.getType());
				m_sb.append(' ').append(t.getName());
				m_sb.append(' ').append(t.getStatus());
				m_sb.append(' ').append(t.getData());
				m_sb.append('\n');
			}
		}
	}

	public static class MockTransportManager implements TransportManager {
		private MessageSender m_sender;

		public MockTransportManager() {
		}

		@Override
		public MessageSender getSender() {
			return m_sender;
		}

		public void setQueue(final Queue<MessageTree> queue) {
			m_sender = new MessageSender() {
				@Override
				public void initialize() {
				}

				@Override
				public void send(MessageTree tree) {
					queue.offer(tree);
				}

				@Override
				public void shutdown() {
				}
			};
		}
	}
}
