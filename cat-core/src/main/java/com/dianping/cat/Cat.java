package com.dianping.cat;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.configuration.ClientConfigMerger;
import com.dianping.cat.configuration.ClientConfigValidator;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.transform.DefaultXmlParser;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.spi.MessageManager;
import com.site.helper.Files;
import com.site.helper.Threads;
import com.site.helper.Threads.DefaultThreadListener;

/**
 * This is the main entry point to the system.
 * 
 * @author Frankie Wu
 */
public class Cat {
	public static final String CAT_CLIENT_XML = "/META-INF/cat/client.xml";

	private static Cat s_instance = new Cat();

	private volatile boolean m_initialized;

	private MessageProducer m_producer;

	private MessageManager m_manager;

	private PlexusContainer m_container;

	private Logger m_logger;

	private Cat() {
	}

	public static void destroy() {
		s_instance = new Cat();
	}

	static Cat getInstance() {
		synchronized (s_instance) {
			if (!s_instance.m_initialized) {
				throw new RuntimeException("Cat has not been initialized yet, please call Cat.initialize(...) first!");
			}
		}

		return s_instance;
	}

	public static MessageManager getManager() {
		return getInstance().m_manager;
	}

	public static MessageProducer getProducer() {
		return getInstance().m_producer;
	}

	// this should be called during application initialization time
	public static void initialize(File configFile) {
		try {
			PlexusContainer container = new DefaultPlexusContainer();

			initialize(container, configFile);
		} catch (PlexusContainerException e) {
			throw new RuntimeException("Error when creating Plexus container, "
			      + "please make sure the environment was setup correctly!", e);
		}
	}

	public static void initialize(PlexusContainer container, File configFile) {
		if (container != null) {
			synchronized (s_instance) {
				if (!s_instance.m_initialized) {
					s_instance.setContainer(container);
					s_instance.m_initialized = true;
				} else {
					throw new RuntimeException("Cat has already been initialized before!");
				}
			}
		}

		ClientConfig config = loadClientConfig(configFile);
		Cat instance = getInstance();

		instance.m_logger.info("Current working directory is " + System.getProperty("user.dir"));

		if (config != null) {
			instance.m_manager.initializeClient(config);
			instance.m_logger.info("Cat client is initialized!");
		} else {
			instance.m_manager.initializeClient(null);
			instance.m_logger.warn("Cat client is disabled due to no config file found!");
		}
	}

	public static boolean isInitialized() {
		return s_instance.m_initialized;
	}

	static ClientConfig loadClientConfig(File configFile) {
		ClientConfig globalConfig = null;
		ClientConfig clientConfig = null;

		try {
			// read the global configure from local file system
			// so that OPS can:
			// - configure the cat servers to connect
			// - enable/disable Cat for specific domain(s)
			if (configFile != null) {
				if (configFile.exists()) {
					String xml = Files.forIO().readFrom(configFile.getCanonicalFile(), "utf-8");

					globalConfig = new DefaultXmlParser().parse(xml);
				} else {
					getInstance().m_logger.warn(String.format("Global config file(%s) not found, IGNORED.", configFile));
				}
			}

			// load the client configure from Java class-path
			if (clientConfig == null) {
				InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CAT_CLIENT_XML);

				if (in == null) {
					in = Cat.class.getResourceAsStream(CAT_CLIENT_XML);
				}

				if (in != null) {
					String xml = Files.forIO().readFrom(in, "utf-8");

					clientConfig = new DefaultXmlParser().parse(xml);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(String.format("Error when loading configuration file(%s)!", configFile), e);
		}

		// merge the two configures together to make it effected
		if (globalConfig != null && clientConfig != null) {
			globalConfig.accept(new ClientConfigMerger(clientConfig));
		}

		// do validation
		if (clientConfig != null) {
			clientConfig.accept(new ClientConfigValidator());
		}

		return clientConfig;
	}

	public static <T> T lookup(Class<T> role) throws ComponentLookupException {
		return lookup(role, null);
	}

	@SuppressWarnings("unchecked")
	public static <T> T lookup(Class<T> role, String hint) throws ComponentLookupException {
		return (T) getInstance().m_container.lookup(role, hint);
	}

	// this should be called when a thread ends to clean some thread local data
	public static void reset() {
		getInstance().m_manager.reset();
	}

	// this should be called when a thread starts to create some thread local
	// data
	public static void setup(String sessionToken) {
		MessageManager manager = getInstance().m_manager;

		manager.setup();
		manager.getThreadLocalMessageTree().setSessionToken(sessionToken);
	}

	private boolean isCatServerFound(PlexusContainer container) {
		try {
			return container.getContext().get("Cat.ThreadListener") != null;
		} catch (ContextException e) {
			return false;
		}
	}

	void setContainer(PlexusContainer container) {
		m_container = container;

		try {
			m_logger = container.getLoggerManager().getLoggerForComponent(MessageManager.class.getName());
		} catch (Exception e) {
			throw new RuntimeException("Unable to get instance of Logger, "
			      + "please make sure the environment was setup correctly!", e);
		}

		if (!isCatServerFound(container)) {
			Threads.addListener(new DefaultThreadListener() {
				@Override
				public void onThreadGroupCreated(ThreadGroup group, String name) {
					m_logger.info(String.format("Thread group(%s) created.", name));
				}

				@Override
				public void onThreadPoolCreated(ExecutorService pool, String name) {
					m_logger.info(String.format("Thread pool(%s) created.", name));
				}

				@Override
				public void onThreadStarting(Thread thread, String name) {
					m_logger.info(String.format("Starting thread(%s) ...", name));
				}

				@Override
				public void onThreadStopping(Thread thread, String name) {
					m_logger.info(String.format("Stopping thread(%s).", name));
				}

				@Override
				public boolean onUncaughtException(Thread thread, Throwable e) {
					m_logger.error(String.format("Uncaught exception thrown out of thread(%s)", thread.getName()), e);
					return true;
				}
			});
		}

		try {
			m_manager = (MessageManager) container.lookup(MessageManager.class);
		} catch (ComponentLookupException e) {
			throw new RuntimeException("Unable to get instance of MessageManager, "
			      + "please make sure the environment was setup correctly!", e);
		}

		try {
			m_producer = (MessageProducer) container.lookup(MessageProducer.class);
		} catch (ComponentLookupException e) {
			throw new RuntimeException("Unable to get instance of MessageProducer, "
			      + "please make sure the environment was setup correctly!", e);
		}
	}
}
