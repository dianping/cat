package com.dianping.cat;

import java.io.File;
import java.io.InputStream;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import com.dianping.cat.configuration.model.ClientConfigValidator;
import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.configuration.model.transform.DefaultParser;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.spi.MessageManager;
import com.site.helper.Files;

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

	private Cat() {
	}

	static Cat getInstance() {
		if (!s_instance.m_initialized) {
			try {
				s_instance.setContainer(new DefaultPlexusContainer());
			} catch (PlexusContainerException e) {
				throw new RuntimeException("Error when creating Plexus container, "
				      + "please make sure the environment was setup correctly!", e);
			}
		}

		return s_instance;
	}

	public static MessageProducer getProducer() {
		return getInstance().m_producer;
	}

	// this should be called during application initialization time
	public static void initialize(File configFile) {
		initialize(null, configFile);
	}

	public static void initialize(PlexusContainer container, File configFile) {
		Config config = null;

		// read config from local file system
		try {
			if (configFile != null) {
				String xml = Files.forIO().readFrom(configFile.getCanonicalFile(), "utf-8");

				config = new DefaultParser().parse(xml);
			}

			if (config == null) {
				InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CAT_CLIENT_XML);

				if (in == null) {
					in = Cat.class.getResourceAsStream(CAT_CLIENT_XML);
				}

				if (in != null) {
					String xml = Files.forIO().readFrom(in, "utf-8");

					config = new DefaultParser().parse(xml);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(String.format("Error when loading configuration file: %s!", configFile), e);
		}

		if (container != null) {
			if (!s_instance.m_initialized) {
				s_instance.setContainer(container);
			} else {
				throw new RuntimeException("Cat has already been initialized before!");
			}
		}

		if (config != null) {
			ClientConfigValidator validator = new ClientConfigValidator();

			config.accept(validator);
			getInstance().m_manager.initialize(config);
		} else {
			System.out.println("[WARN] Cat client is disabled due to no config file found!");
		}
	}

	// this should be called when a thread ends to clean some thread local data
	public static void reset() {
		getInstance().m_manager.reset();
	}

	// this should be called when a thread starts to create some thread local
	// data
	public static void setup(String sessionToken, String requestToken) {
		getInstance().m_manager.setup(sessionToken, requestToken);
	}

	void setContainer(PlexusContainer container) {
		m_initialized = true;

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
