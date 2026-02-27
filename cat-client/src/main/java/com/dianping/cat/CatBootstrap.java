package com.dianping.cat;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import com.dianping.cat.apiguardian.api.API;
import com.dianping.cat.apiguardian.api.API.Status;
import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.DefaultComponentContext;
import com.dianping.cat.component.factory.CatComponentFactory;
import com.dianping.cat.component.factory.ServiceLoaderComponentFactory;
import com.dianping.cat.component.lifecycle.Logger;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.configuration.ConfigureSource;
import com.dianping.cat.configuration.model.ClientConfigHelper;
import com.dianping.cat.configuration.model.entity.ClientConfig;
import com.dianping.cat.configuration.model.entity.Domain;
import com.dianping.cat.configuration.model.entity.Server;
import com.dianping.cat.message.internal.MilliSecondTimer;
import com.dianping.cat.network.ClientTransportManager;
import com.dianping.cat.status.StatusUpdateTask;
import com.dianping.cat.support.Threads;
import com.dianping.cat.support.Threads.AbstractThreadListener;

/**
 * Utility to bootstrap CAT client.
 * 
 * Any one of following approaches can bring up CAT client.
 * <uL>
 * <li><code>Cat.getBootstrap().initialize(File configFile)</code></li>
 * <li><code>Cat.getBootstrap().initialize(String... servers)</code></li>
 * <li><code>Cat.getBootstrap().initializeByDomain(String domain, String... servers)</code></li>
 * <li><code>Cat.getBootstrap().initializeByDomain(String domain, int tcpPort, int httpPort, String... servers)</code></li>
 * <li>or CAT will be lazy initialized automatically</li>
 * </ul>
 * <p>
 * 
 * Missing configure will be detected in the following order:
 * <ol>
 * <li>domain: property <code>app.name</code> in /META-INF/app.properties</li>
 * <li>domain and servers: elements in &lt;CAT_HOME&gt;/client.xml</li>
 * </ol>
 * 
 * @author Frankie Wu
 */
public class CatBootstrap {
	private ComponentContext m_ctx = new DefaultComponentContext();

	private AtomicBoolean m_initialized = new AtomicBoolean();

	private AtomicBoolean m_testMode = new AtomicBoolean();

	private Logger m_logger;

	private File m_catHome;

	CatBootstrap() {
		m_ctx.registerFactory(new ServiceLoaderComponentFactory()); // higher priority
		m_ctx.registerFactory(new CatComponentFactory());

		m_logger = m_ctx.lookup(Logger.class);
	}

	@API(status = Status.EXPERIMENTAL, since = "3.2.0")
	public File getCatHome() {
		// check from system properties(cat.home) and environment variable(CAT_HOME)
		if (m_catHome == null) {
			String catHome = System.getProperty("cat.home", null);

			if (catHome == null) {
				catHome = System.getenv("CAT_HOME");
			}

			if (catHome != null) {
				File file = new File(catHome);

				file.mkdirs();

				if (file.isDirectory())
					m_catHome = file;
			}
		}

		// check ~/.cat directory
		if (m_catHome == null) {
			String userHome = System.getProperty("user.home");
			File file = new File(userHome, ".cat");

			file.mkdirs();

			if (file.isDirectory()) {
				m_catHome = file;
			}
		}

		// check <tmpdir>/.cat directory
		if (m_catHome == null) {
			String userHome = System.getProperty("java.io.tmpdir");
			File file = new File(userHome, ".cat");

			file.mkdirs();

			if (file.isDirectory()) {
				m_catHome = file;
			}
		}

		return m_catHome;
	}

	@API(status = Status.INTERNAL, since = "3.2.0")
	public ComponentContext getComponentContext() {
		return m_ctx;
	}

	// WARN: It's reserved for CAT internal use only.
	@API(status = Status.INTERNAL, since = "3.2.0")
	public synchronized void initialize(final ClientConfig config) {
		if (!m_initialized.get()) {
			File catHome = getCatHome();

			System.setProperty("CAT_HOME", catHome.getPath());

			if (!m_testMode.get()) {
				m_logger.info("CAT home: %s", catHome);
				m_logger.info("User dir: %s", System.getProperty("user.dir"));
				
				// tracking thread start/stop
				Threads.addListener(new CatThreadListener());
			}

			// initialize high resolution timer
			MilliSecondTimer.initialize();

			m_ctx.registerComponent(ConfigureSource.class, new ConfigureSource<ClientConfig>() {
				@Override
				public ClientConfig getConfig() throws Exception {
					return config;
				}

				@Override
				public int getOrder() {
					return 0;
				}
			});

			ConfigureManager configureManager = m_ctx.lookup(ConfigureManager.class);

			if (!m_testMode.get()) {
				m_logger.info("CAT client configuration: %s", configureManager);

				if (configureManager.isEnabled()) {
					// bring up TransportManager
					m_ctx.lookup(ClientTransportManager.class).start();

					StatusUpdateTask statusUpdateTask = m_ctx.lookup(StatusUpdateTask.class);

					Threads.forGroup("Cat").start(statusUpdateTask);

					LockSupport.parkNanos(10 * 1000 * 1000L); // wait 10 ms
				}
			}

			m_initialized.set(true);
		}
	}

	@API(status = Status.STABLE, since = "3.1.0")
	public void initialize(File clientXmlFile) {
		if (clientXmlFile.isFile()) {
			try {
				ClientConfig config = ClientConfigHelper.fromXml(new FileInputStream(clientXmlFile));

				initialize(config);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			m_logger.warn("CAT config(%s) is not found! SKIPPED", clientXmlFile);
			initialize(new ClientConfig());
		}
	}

	// domain is from property app.name of resource /META-INF/app.properties
	@API(status = Status.EXPERIMENTAL, since = "3.1.0")
	public void initialize(String... servers) {
		initializeByDomain(null, servers);
	}

	@API(status = Status.EXPERIMENTAL, since = "3.1.0")
	public void initializeByDomain(String domain, int tcpPort, int httpPort, String... servers) {
		ClientConfig config = new ClientConfigBuilder().build(domain, tcpPort, httpPort, servers);

		initialize(config);
	}

	@API(status = Status.EXPERIMENTAL, since = "3.1.0")
	public void initializeByDomain(String domain, String... servers) {
		initializeByDomain(domain, 2280, 8080, servers);
	}

	@API(status = Status.EXPERIMENTAL, since = "3.1.0")
	public boolean isInitialized() {
		return m_initialized.get();
	}

	@API(status = Status.INTERNAL, since = "3.2.0")
	public boolean isTestMode() {
		return m_testMode.get();
	}

	@API(status = Status.INTERNAL, since = "3.2.0")
	public void reset() {
		if (m_initialized.get()) {
			m_ctx.dispose();
			m_initialized.set(false);
		}
	}

	// For test case to skip StatusUpdateTask
	@API(status = Status.INTERNAL, since = "3.2.0")
	public void testMode() {
		m_testMode.set(true);
	}

	private final class CatThreadListener extends AbstractThreadListener {
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
	}

	private static class ClientConfigBuilder {
		public ClientConfig build(String domain, int tcpPort, int httpPort, String... servers) {
			ClientConfig config = new ClientConfig();

			if (domain != null) {
				config.setDomain(new Domain().setName(domain));
			}

			for (String server : servers) {
				config.addServer(new Server(server).setPort(tcpPort).setHttpPort(httpPort));
			}

			return config;
		}
	}
}
