package com.dianping.cat;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.unidal.helper.Files;
import org.unidal.helper.Properties;
import org.unidal.initialization.DefaultModuleContext;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;
import org.unidal.initialization.ModuleInitializer;
import org.unidal.lookup.ContainerLoader;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.TaggedTransaction;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;

/**
 * This is the main entry point to the system.
 */
public class Cat {
	private static Cat s_instance = new Cat();

	private static volatile boolean s_init = false;

	private MessageProducer m_producer;

	private MessageManager m_manager;

	private PlexusContainer m_container;

	private static void checkAndInitialize() {
		if (!s_init) {
			synchronized (s_instance) {
				if (!s_init) {
					initialize(new File(getCatHome(), "client.xml"));
					log("WARN", "Cat is lazy initialized!");
					s_init = true;
				}
			}
		}
	}

	public static String createMessageId() {
		return Cat.getProducer().createMessageId();
	}

	public static void destroy() {
		s_instance.m_container.dispose();
		s_instance = new Cat();
	}

	public static String getCatHome() {
		String catHome = Properties.forString().fromEnv().fromSystem().getProperty("CAT_HOME", "/data/appdatas/cat");

		return catHome;
	}

	public static String getCurrentMessageId() {
		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

		if (tree != null) {
			String messageId = tree.getMessageId();

			if (messageId == null) {
				messageId = Cat.createMessageId();
				tree.setMessageId(messageId);
			}
			return messageId;
		} else {
			return null;
		}
	}

	public static Cat getInstance() {
		return s_instance;
	}

	public static MessageManager getManager() {
		checkAndInitialize();

		return s_instance.m_manager;
	}

	public static MessageProducer getProducer() {
		checkAndInitialize();

		return s_instance.m_producer;
	}

	// this should be called during application initialization time
	public static void initialize(File configFile) {
		synchronized (s_instance) {
			PlexusContainer container = ContainerLoader.getDefaultContainer();

			initialize(container, configFile);

			s_init = true;
		}
	}

	public static void initialize(PlexusContainer container, File configFile) {
		ModuleContext ctx = new DefaultModuleContext(container);
		Module module = ctx.lookup(Module.class, CatClientModule.ID);

		if (!module.isInitialized()) {
			ModuleInitializer initializer = ctx.lookup(ModuleInitializer.class);

			ctx.setAttribute("cat-client-config-file", configFile);
			initializer.execute(ctx, module);
		}
	}

	public static void initialize(String... servers) {
		File configFile = null;

		try {
			configFile = File.createTempFile("cat-client", ".xml");
			ClientConfig config = new ClientConfig().setMode("client");

			for (String server : servers) {
				config.addServer(new Server(server));
			}

			Files.forIO().writeTo(configFile, config.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		initialize(configFile);
	}

	public static boolean isInitialized() {
		synchronized (s_instance) {
			return s_instance.m_container != null;
		}
	}

	static void log(String severity, String message) {
		MessageFormat format = new MessageFormat("[{0,date,MM-dd HH:mm:ss.sss}] [{1}] [{2}] {3}");

		System.out.println(format.format(new Object[] { new Date(), severity, "cat", message }));
	}

	public static void logError(String message, Throwable cause) {
		Cat.getProducer().logError(message, cause);
	}

	public static void logError(Throwable cause) {
		Cat.getProducer().logError(cause);
	}

	public static void logEvent(String type, String name) {
		Cat.getProducer().logEvent(type, name);
	}

	public static void logEvent(String type, String name, String status, String nameValuePairs) {
		Cat.getProducer().logEvent(type, name, status, nameValuePairs);
	}

	public static void logHeartbeat(String type, String name, String status, String nameValuePairs) {
		Cat.getProducer().logHeartbeat(type, name, status, nameValuePairs);
	}

	public static void logMetric(String name, Object... keyValues) {
		// TO REMOVE ME
	}

	/**
	 * Increase the counter specified by <code>name</code> by one.
	 * 
	 * @param name
	 *           the name of the metric default count value is 1
	 */
	public static void logMetricForCount(String name) {
		logMetricInternal(name, "C", "1");
	}

	/**
	 * Increase the counter specified by <code>name</code> by one.
	 * 
	 * @param name
	 *           the name of the metric
	 */
	public static void logMetricForCount(String name, int quantity) {
		logMetricInternal(name, "C", String.valueOf(quantity));
	}

	/**
	 * Increase the metric specified by <code>name</code> by <code>durationInMillis</code>.
	 * 
	 * @param name
	 *           the name of the metric
	 * @param durationInMillis
	 *           duration in milli-second added to the metric
	 */
	public static void logMetricForDuration(String name, long durationInMillis) {
		logMetricInternal(name, "T", String.valueOf(durationInMillis));
	}

	/**
	 * Increase the sum specified by <code>name</code> by <code>value</code> only for one item.
	 * 
	 * @param name
	 *           the name of the metric
	 * @param value
	 *           the value added to the metric
	 */
	public static void logMetricForSum(String name, double value) {
		logMetricInternal(name, "S", String.format("%.2f", value));
	}

	/**
	 * Increase the metric specified by <code>name</code> by <code>sum</code> for multiple items.
	 * 
	 * @param name
	 *           the name of the metric
	 * @param sum
	 *           the sum value added to the metric
	 * @param quantity
	 *           the quantity to be accumulated
	 */
	public static void logMetricForSum(String name, double sum, int quantity) {
		logMetricInternal(name, "S,C", String.format("%s,%.2f", quantity, sum));
	}

	private static void logMetricInternal(String name, String status, String keyValuePairs) {
		Cat.getProducer().logMetric(name, status, keyValuePairs);
	}

	public static void logRemoteCallClient(Context ctx) {
		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
		String messageId = tree.getMessageId();

		if (messageId == null) {
			messageId = Cat.createMessageId();
			tree.setMessageId(messageId);
		}

		String childId = Cat.createMessageId();
		Cat.logEvent(CatConstants.TYPE_REMOTE_CALL, "", Event.SUCCESS, childId);

		String root = tree.getRootMessageId();

		if (root == null) {
			root = messageId;
		}

		ctx.addProperty(Context.ROOT, root);
		ctx.addProperty(Context.PARENT, messageId);
		ctx.addProperty(Context.CHILD, childId);
	}

	public static void logRemoteCallServer(Context ctx) {
		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
		String messageId = ctx.getProperty(Context.CHILD);
		String rootId = ctx.getProperty(Context.ROOT);
		String parentId = ctx.getProperty(Context.PARENT);

		if (messageId != null) {
			tree.setMessageId(messageId);
		}
		if (parentId != null) {
			tree.setParentMessageId(parentId);
		}
		if (rootId != null) {
			tree.setRootMessageId(rootId);
		}
	}

	public static void logTrace(String type, String name) {
		Cat.getProducer().logTrace(type, name);
	}

	public static void logTrace(String type, String name, String status, String nameValuePairs) {
		Cat.getProducer().logTrace(type, name, status, nameValuePairs);
	}

	public static <T> T lookup(Class<T> role) throws ComponentLookupException {
		return lookup(role, null);
	}

	public static <T> T lookup(Class<T> role, String hint) throws ComponentLookupException {
		return s_instance.m_container.lookup(role, hint);
	}

	public static Event newEvent(String type, String name) {
		return Cat.getProducer().newEvent(type, name);
	}

	public static ForkedTransaction newForkedTransaction(String type, String name) {
		return Cat.getProducer().newForkedTransaction(type, name);
	}

	public static Heartbeat newHeartbeat(String type, String name) {
		return Cat.getProducer().newHeartbeat(type, name);
	}

	public static TaggedTransaction newTaggedTransaction(String type, String name, String tag) {
		return Cat.getProducer().newTaggedTransaction(type, name, tag);
	}

	public static Trace newTrace(String type, String name) {
		return Cat.getProducer().newTrace(type, name);
	}

	public static Transaction newTransaction(String type, String name) {
		return Cat.getProducer().newTransaction(type, name);
	}
	
	// this should be called when a thread ends to clean some thread local data
	public static void reset() {
		// remove me
	}

	// this should be called when a thread starts to create some thread local data
	public static void setup(String sessionToken) {
		Cat.getManager().setup();
	}

	private Cat() {
	}

	void setContainer(PlexusContainer container) {
		try {
			m_container = container;
			m_manager = container.lookup(MessageManager.class);
			m_producer = container.lookup(MessageProducer.class);
		} catch (ComponentLookupException e) {
			throw new RuntimeException("Unable to get instance of MessageManager, "
			      + "please make sure the environment was setup correctly!", e);
		}
	}

	public static interface Context {

		public final String ROOT = "_catRootMessageId";

		public final String PARENT = "_catParentMessageId";

		public final String CHILD = "_catChildMessageId";

		public void addProperty(String key, String value);

		public String getProperty(String key);
	}

}
