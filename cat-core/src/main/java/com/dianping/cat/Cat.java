package com.dianping.cat;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;
import org.unidal.initialization.DefaultModuleContext;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;
import org.unidal.initialization.ModuleInitializer;
import org.unidal.lookup.ContainerLoader;

/**
 * This is the main entry point to the system.
 * 
 * @author Frankie Wu
 */
public class Cat {
	public static final String CAT_GLOBAL_XML = "/data/appdatas/cat/client.xml";

	private static Cat s_instance = new Cat();

	private MessageProducer m_producer;

	private MessageManager m_manager;

	private PlexusContainer m_container;

	private Cat() {
	}

	public static Transaction newTransaction(String type, String name) {
		return Cat.getProducer().newTransaction(type, name);
	}

	public static Event newEvent(String type, String name) {
		return Cat.getProducer().newEvent(type, name);
	}

	public static void logError(Throwable cause) {
		Cat.getProducer().logError(cause);
	}

	public static Heartbeat newHeartbeat(String type, String name) {
		return Cat.getProducer().newHeartbeat(type, name);
	}

	public static String createMessageId() {
		return Cat.getProducer().createMessageId();
	}

	public static boolean isEnabled() {
		return Cat.getProducer().isEnabled();
	}

	public void logEvent(String type, String name) {
		Cat.getProducer().logEvent(type, name);
	}

	public void logEvent(String type, String name, String status, String nameValuePairs) {
		Cat.getProducer().logEvent(type, name, status, nameValuePairs);
	}

	public void logHeartbeat(String type, String name, String status, String nameValuePairs) {
		Cat.getProducer().logHeartbeat(type, name, status, nameValuePairs);
	}

	public static void destroy() {
		s_instance.m_container.dispose();
		s_instance = new Cat();
	}

	public static Cat getInstance() {
		return s_instance;
	}

	public static MessageManager getManager() {
		return s_instance.m_manager;
	}

	public static MessageProducer getProducer() {
		synchronized (s_instance) {
			if (s_instance.m_container == null) {
				initialize(new File(CAT_GLOBAL_XML));
				log("WARN", "Cat is lazy initialized!");
			}
		}

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

	public static boolean isInitialized() {
		synchronized (s_instance) {
			return s_instance.m_container != null;
		}
	}

	static void log(String severity, String message) {
		MessageFormat format = new MessageFormat("[{0,date,MM-dd HH:mm:ss.sss}] [{1}] [{2}] {3}");

		System.out.println(format.format(new Object[] { new Date(), severity, "Cat", message }));
	}

	public static <T> T lookup(Class<T> role) throws ComponentLookupException {
		return lookup(role, null);
	}

	public static <T> T lookup(Class<T> role, String hint) throws ComponentLookupException {
		return s_instance.m_container.lookup(role, hint);
	}

	// this should be called when a thread ends to clean some thread local data
	public static void reset() {
		s_instance.m_manager.reset();
	}

	// this should be called when a thread starts to create some thread local
	// data
	public static void setup(String sessionToken) {
		MessageManager manager = s_instance.m_manager;

		manager.setup();
		manager.getThreadLocalMessageTree().setSessionToken(sessionToken);
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
