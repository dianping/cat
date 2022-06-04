package com.dianping.cat;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import com.dianping.cat.analyzer.LocalAggregator;
import com.dianping.cat.apiguardian.api.API;
import com.dianping.cat.apiguardian.api.API.Status;
import com.dianping.cat.component.CatComponentFactory;
import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.DefaultComponentContext;
import com.dianping.cat.component.Logger;
import com.dianping.cat.component.ServiceLoaderComponentFactory;
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultSaxParser;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.internal.MilliSecondTimer;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.status.StatusUpdateTask;
import com.dianping.cat.util.Threads;
import com.dianping.cat.util.Threads.AbstractThreadListener;

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
	private AtomicBoolean m_initialized = new AtomicBoolean();

	private ComponentContext m_ctx = new DefaultComponentContext();

	private Cat m_cat;

	private Logger m_logger;

	private File m_catHome;

	CatBootstrap(Cat cat) {
		m_ctx.registerFactory(new ServiceLoaderComponentFactory()); // higher priority
		m_ctx.registerFactory(new CatComponentFactory());

		m_logger = m_ctx.lookup(Logger.class);
		m_cat = cat;
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

	public ComponentContext getComponentContext() {
		return m_ctx;
	}

	protected synchronized void initialize(ClientConfig config) {
		if (!m_initialized.get()) {
			m_logger.info("Working directory: %s", System.getProperty("user.dir"));

			// setup client configure
			ClientConfigManager manager = m_ctx.lookup(ClientConfigManager.class);

			manager.initialize(config);

			// initialize message id factory
			MessageIdFactory factory = m_ctx.lookup(MessageIdFactory.class);

			factory.initialize(manager.getDomain().getId());

			m_cat.setup(m_ctx);

			// initialize high resolution timer
			MilliSecondTimer.initialize();

			// tracking thread start/stop
			Threads.addListener(new CatThreadListener());

			// bring up TransportManager
			m_ctx.lookup(TransportManager.class);

			if (manager.isCatEnabled()) {
				// start status update task
				StatusUpdateTask statusUpdateTask = m_ctx.lookup(StatusUpdateTask.class);

				Threads.forGroup("cat").start(statusUpdateTask);
				Threads.forGroup("cat").start(new LocalAggregator.DataUploader());

				LockSupport.parkNanos(10 * 1000 * 1000L); // wait 10 ms
			}

			m_initialized.set(true);
		}
	}

	@API(status = Status.STABLE, since = "3.1.0")
	public void initialize(File clientXmlFile) {
		if (clientXmlFile.isFile()) {
			try {
				ClientConfig config = DefaultSaxParser.parse(new FileInputStream(clientXmlFile));

				initialize(config);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			m_logger.warn("CAT config(%s) is not found! SKIPPED", clientXmlFile);
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

	public void reset() {
		if (m_initialized.get()) {
			m_ctx.dispose();
			m_initialized.set(false);
		}
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
			ClientConfig config = new ClientConfig().setMode("client").setDumpLocked(false);

			if (domain != null) {
				config.addDomain(new Domain(domain).setEnabled(true));
			}

			for (String server : servers) {
				config.addServer(new Server(server).setPort(tcpPort).setHttpPort(httpPort));
			}

			return config;
		}
	}
}
