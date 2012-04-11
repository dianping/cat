package com.dianping.cat.consumer.logview;

import java.text.MessageFormat;
import java.util.Date;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class LogviewUploader extends ContainerHolder implements Initializable {
	@Inject
	private MessagePathBuilder m_builder;

	private String m_baseDir = "target/bucket/logview";

	public void upload(long timestamp, String domain) {
		MessageFormat format = new MessageFormat("{0,date,yyyyMMdd}/{1}");
		String path = format.format(new Object[] { new Date(timestamp), domain });
		
	}

	@Override
	public void initialize() throws InitializationException {
		ServerConfigManager configManager = lookup(ServerConfigManager.class);
		ServerConfig serverConfig = configManager.getServerConfig();

		if (serverConfig != null) {
			m_baseDir = serverConfig.getStorage().getLocalBaseDir();
		}
	}
}
