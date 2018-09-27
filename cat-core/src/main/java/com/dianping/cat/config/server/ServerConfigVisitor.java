package com.dianping.cat.config.server;

import com.dianping.cat.configuration.server.entity.ConsumerConfig;
import com.dianping.cat.configuration.server.entity.LongConfig;
import com.dianping.cat.configuration.server.entity.Server;
import com.dianping.cat.configuration.server.entity.StorageConfig;
import com.dianping.cat.configuration.server.transform.BaseVisitor;

public class ServerConfigVisitor extends BaseVisitor {

	private Server m_specificServer;

	public ServerConfigVisitor(Server server) {
		m_specificServer = server;
	}

	@Override
	public void visitConsumer(ConsumerConfig consumer) {
		ConsumerConfig c = m_specificServer.getConsumer();
		LongConfig l = null;

		if (c != null && (l = c.getLongConfig()) != null) {
			LongConfig longConfig = consumer.getLongConfig();

			longConfig.setDefaultServiceThreshold(l.getDefaultServiceThreshold());
			longConfig.setDefaultSqlThreshold(l.getDefaultSqlThreshold());
			longConfig.setDefaultUrlThreshold(l.getDefaultUrlThreshold());
			longConfig.getDomains().putAll(l.getDomains());
		}
	}

	@Override
	public void visitServer(Server server) {
		server.getProperties().putAll(m_specificServer.getProperties());
		super.visitServer(server);
	}

	@Override
	public void visitStorage(StorageConfig storage) {
		StorageConfig s = m_specificServer.getStorage();

		if (s != null) {
			storage.setHarMode(s.getHarMode()).setLocalBaseDir(s.getLocalBaseDir())
			      .setLocalLogivewStorageTime(s.getLocalLogivewStorageTime());
			storage.setLocalReportStorageTime(s.getLocalReportStorageTime())
			      .setMaxHdfsStorageTime(s.getMaxHdfsStorageTime()).setUploadThread(s.getUploadThread());

			storage.getHdfses().putAll(s.getHdfses());
			storage.getHarfses().putAll(s.getHarfses());
			storage.getProperties().putAll(s.getProperties());
		}
	}

}
