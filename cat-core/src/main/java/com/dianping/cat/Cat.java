package com.dianping.cat;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.unidal.initialization.DefaultModuleContext;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;
import org.unidal.initialization.ModuleInitializer;
import org.unidal.lookup.ContainerLoader;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;

/**
 * This is the main entry point to the system.
 * 
 * @author Frankie Wu
 */
public class Cat {
	public static final String CAT_GLOBAL_XML = "/data/appdatas/cat/client.xml";

	private static Cat s_instance = new Cat();

	public static final String MetricType = "_MetricType";

	public static final String MetricGroup = "_MetricGroup";

	private static void checkAndInitialize() {
		synchronized (s_instance) {
			if (s_instance.m_container == null) {
				initialize(new File(CAT_GLOBAL_XML));
				log("WARN", "Cat is lazy initialized!");
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
		PlexusContainer container = ContainerLoader.getDefaultContainer();

		initialize(container, configFile);
	}

	public static void initialize(PlexusContainer container, File configFile) {
		ModuleContext ctx = new DefaultModuleContext(container);
		Module module = ctx.lookup(Module.class, CatCoreModule.ID);

		if (!module.isInitialized()) {
			ModuleInitializer initializer = ctx.lookup(ModuleInitializer.class);

			ctx.setAttribute("cat-client-config-file", configFile);
			initializer.execute(ctx, module);
		}
	}

	public static boolean isEnabled() {
		return Cat.getProducer().isEnabled();
	}

	public static boolean isInitialized() {
		synchronized (s_instance) {
			return s_instance.m_container != null;
		}
	}

	static void log(String severity, String message) {
		MessageFormat format = new MessageFormat("[{0,date,MM-dd HH:mm:ss.sss}] [{1}] [{2}] {3}");

		System.out.println(format.format(new Object[] { new Date(), severity, "Cat", message }));
	}

	public static void logError(String message, Throwable cause) {
		Cat.getProducer().logError(new Throwable(message, cause));
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

	private static void recordMetric( String name, double value, String metricType) {
		StringBuilder sb = new StringBuilder();

		sb.append(name).append("=").append(value);
		sb.append("&").append(MetricType).append("=").append(metricType);
		Cat.getProducer().logMetric("default", name, Message.SUCCESS, sb.toString());
	}

	public static void recordMetric( String name, double value) {
		recordMetric( name, value, "SUM");
	}

	public static void recordResponseTimeMetric( String name, double millis) {
		recordMetric( name, millis, "AVG");
	}

	public static void recordCountMetric( String name) {
		recordMetric( name, 1, "SUM");
	}

	public static void logMetric(String name, Object... keyValues) {
		StringBuilder sb = new StringBuilder(1024);
		int len = keyValues.length;
		boolean first = true;

		if (len % 2 == 1) {
			throw new IllegalArgumentException("Key values should be paired!");
		}

		for (int i = 0; i < len; i += 2) {
			Object key = keyValues[i];
			Object value = keyValues[i + 1];

			if (first) {
				first = false;
			} else {
				sb.append('&');
			}

			sb.append(key).append('=');

			if (value != null) {
				sb.append(value);
			}
		}

		Cat.getProducer().logMetric("default", name, Message.SUCCESS, sb.toString());
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

	public static Heartbeat newHeartbeat(String type, String name) {
		return Cat.getProducer().newHeartbeat(type, name);
	}

	public static Transaction newTransaction(String type, String name) {
		return Cat.getProducer().newTransaction(type, name);
	}

	// this should be called when a thread ends to clean some thread local data
	public static void reset() {
		s_instance.m_manager.reset();
	}

	// this should be called when a thread starts to create some thread local
	// data
	public static void setup(String sessionToken) {
		MessageManager manager = s_instance.m_manager;

		if (manager == null) {
			checkAndInitialize();
		}
		manager.setup();
		manager.getThreadLocalMessageTree().setSessionToken(sessionToken);
	}

	private MessageProducer m_producer;

	private MessageManager m_manager;

	private PlexusContainer m_container;

	private Cat() {
	}

	void setContainer(PlexusContainer container) {
		m_container = container;

		try {
			m_manager = container.lookup(MessageManager.class);
		} catch (ComponentLookupException e) {
			throw new RuntimeException("Unable to get instance of MessageManager, "
			      + "please make sure the environment was setup correctly!", e);
		}

		try {
			m_producer = container.lookup(MessageProducer.class);
		} catch (ComponentLookupException e) {
			throw new RuntimeException("Unable to get instance of MessageProducer, "
			      + "please make sure the environment was setup correctly!", e);
		}
	}
}
