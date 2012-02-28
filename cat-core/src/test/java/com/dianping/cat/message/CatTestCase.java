package com.dianping.cat.message;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.junit.After;
import org.junit.Before;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.model.entity.App;
import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.configuration.model.entity.Server;
import com.site.helper.Files;
import com.site.lookup.ComponentTestCase;

public abstract class CatTestCase extends ComponentTestCase {
	@Before
	public void before() throws Exception {
		Cat.initialize(getContainer(), getConfigurationFile());
		Cat.setup(null);
	}

	protected File getConfigurationFile() {
		if (isCatServerAlive()) {
			try {
				Config config = new Config();

				config.setMode("client");
				config.setApp(new App().setDomain("Test"));
				config.addServer(new Server().setIp("localhost").setPort(2280));

				File file = new File("target/cat-config.xml");

				Files.forIO().writeTo(file, config.toString());
				return file;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	protected boolean isCatServerAlive() {
		// detect if a CAT server listens on localhost:2280
		try {
			SocketChannel channel = SocketChannel.open(new InetSocketAddress("localhost", 2280));

			channel.close();
			return true;
		} catch (Exception e) {
			// ignore it
		}

		return false;
	}

	@After
	public void after() throws Exception {
		Cat.reset();
		Cat.destroy();
	}
}