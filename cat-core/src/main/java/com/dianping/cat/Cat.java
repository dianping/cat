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
	public static final String CAT_CONFIG_XML = "/META-INF/cat/config.xml";

	private static PlexusContainer s_container;

	private static MessageProducer s_producer;

	private static MessageManager s_manager;

	static {
		try {
			s_container = new DefaultPlexusContainer();
		} catch (PlexusContainerException e) {
			throw new RuntimeException("Error when creating Plexus container, "
			      + "please make sure the environment was setup correctly!", e);
		}

		try {
			s_manager = (MessageManager) s_container.lookup(MessageManager.class);
		} catch (ComponentLookupException e) {
			throw new RuntimeException("Unable to get instance of MessageManager, "
			      + "please make sure the environment was setup correctly!", e);
		}

		try {
			s_producer = (MessageProducer) s_container.lookup(MessageProducer.class);
		} catch (ComponentLookupException e) {
			throw new RuntimeException("Unable to get instance of MessageProducer, "
			      + "please make sure the environment was setup correctly!", e);
		}
	}

	public static MessageProducer getProducer() {
		return s_producer;
	}

	// this should be called during application initialization time
	public static void initialize(File configFile) {
		Config config = null;

		// read config from local file system
		try {
			if (configFile != null) {
				String xml = Files.forIO().readFrom(configFile.getCanonicalFile(), "utf-8");

				config = new DefaultParser().parse(xml);
			}

			if (config == null) {
				InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CAT_CONFIG_XML);

				if (in == null) {
					in = Cat.class.getResourceAsStream(CAT_CONFIG_XML);
				}

				if (in != null) {
					String xml = Files.forIO().readFrom(in, "utf-8");

					config = new DefaultParser().parse(xml);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(String.format("Error when loading configuration file: %s!", configFile), e);
		}

		if (config != null) {
			ClientConfigValidator validator = new ClientConfigValidator();

			config.accept(validator);
			s_manager.initialize(config);
		}
	}

	// this should be called when a thread ends to clean some thread local data
	public static void reset() {
		s_manager.reset();
	}

	// this should be called when a thread starts to create some thread local
	// data
	public static void setup(String sessionToken, String requestToken) {
		s_manager.setup(sessionToken, requestToken);
	}
}
