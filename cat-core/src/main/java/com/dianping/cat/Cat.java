package com.dianping.cat;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.spi.MessageManager;
import com.site.initialization.DefaultModuleContext;
import com.site.initialization.Module;
import com.site.initialization.ModuleContext;
import com.site.initialization.ModuleInitializer;
import com.site.lookup.ContainerLoader;

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
		// synchronized (s_instance) {
		// if (s_instance.m_container == null) {
		// s_instance.setContainer(container);
		// }
		// }
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
