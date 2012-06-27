package com.dianping.cat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Date;
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
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultDomParser;
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

	public static void destroy() {
		s_instance = new Cat();
	}

	static Cat getInstance() {
		synchronized (s_instance) {
			if (!s_instance.m_initialized) {
				initialize(null);
				log("WARN", "Please call Cat.initialize(...) at application level first to enable it!");
			}
		}

		return s_instance;
	}

	public static MessageManager getManager() {
		return getInstance().m_manager;
	}

	public static MessageProducer getProducer() {
		if (!isInitialized()) {
			initializeForDev();
			System.out.println(">>>>>>>>>>>>>>>>>>> for Dev");
		}

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
					log("WARN", String.format("Cat can't initialize again with config file(%s), IGNORED!", configFile));
					return;
				}
			}
		}

		ClientConfig config = loadClientConfig(configFile);
		Cat instance = getInstance();

		log("INFO", "Current working directory is " + System.getProperty("user.dir"));

		if (config != null) {
			instance.m_manager.initializeClient(config);
			log("INFO", String.format("Cat client is initialized with config file(%s)!", configFile));
		} else {
			instance.m_manager.initializeClient(null);
			log("WARN", "Cat client is disabled due to no config file found!");
		}
	}

	static void initializeForDev() {
		// this is a hack way to make Cat work without initialization
		// explicitly
		// especially from within unit test
		synchronized (s_instance) {
			if (!isInitialized()) {
				File configFile = new File("/data/appdatas/cat/client.xml");

				if (!configFile.exists()) {
					ClientConfig config = new ClientConfig();

					config.setMode("client");
					config.addServer(new Server().setIp("192.168.7.43").setPort(2280));

					configFile.getParentFile().mkdirs();

					try {
						File tempConfigFile = File.createTempFile("cat-client.", ".xml");

						Files.forIO().writeTo(tempConfigFile, config.toString());
						configFile = tempConfigFile;
						tempConfigFile.deleteOnExit();
					} catch (IOException e) {
						log("WARN", String.format("Error when creating file(%s). Message: %s", configFile, e));
					}
				}

				log("INFO", "CAT lazy initialization ...");

				initialize(configFile);
			}
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

					globalConfig = new DefaultDomParser().parse(xml);
				} else {
					log("WARN", String.format("Global config file(%s) not found, IGNORED.", configFile));
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

					clientConfig = new DefaultDomParser().parse(xml);
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
		} else if (globalConfig != null) { // for test purpose
			return globalConfig;
		}

		return clientConfig;
	}

	static void log(String severity, String message) {
		Logger logger = s_instance.m_logger;

		if (logger != null) {
			if ("INFO".equals(severity)) {
				logger.info(message);
			} else if ("WARN".equals(severity)) {
				logger.warn(message);
			} else if ("ERROR".equals(severity)) {
				logger.error(message);
			} else {
				throw new RuntimeException("Unsupported log severity: " + severity);
			}
		} else {
			MessageFormat format = new MessageFormat("[{0,date,MM-dd HH:mm:ss.sss}] [{1}] [{2}] {3}");

			System.out.println(format.format(new Object[] { new Date(), severity, "Cat", message }));
		}
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

	private volatile boolean m_initialized;

	private MessageProducer m_producer;

	private MessageManager m_manager;

	private PlexusContainer m_container;

	private Logger m_logger;

	private Cat() {
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
