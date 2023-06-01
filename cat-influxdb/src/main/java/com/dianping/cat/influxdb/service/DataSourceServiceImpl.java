package com.dianping.cat.influxdb.service;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.influxdb.InfluxDB;
import com.dianping.cat.influxdb.InfluxDBFactory;
import com.dianping.cat.influxdb.config.InfluxDBConfigManager;
import com.dianping.cat.influxdb.config.entity.Influxdb;
import com.dianping.cat.influxdb.config.entity.InfluxdbConfig;
import com.dianping.cat.server.DataSourceService;

@Named(type = DataSourceService.class, value = InfluxDB.ID)
public class DataSourceServiceImpl implements DataSourceService<InfluxDBConnection> {

	@Inject
	private InfluxDBConfigManager m_configManager;

	private InfluxdbConfig m_influxdbConfig;

	private Map<String, InfluxDBConnection> m_connections = new ConcurrentHashMap<String, InfluxDBConnection>();

	public final static String DEFAULT = "default";

	private Map<String, InfluxDBConnection> buildConnections(InfluxdbConfig influxdbConfig) {
		Map<String, InfluxDBConnection> connections = new ConcurrentHashMap<String, InfluxDBConnection>();

		for (Entry<String, Influxdb> entry : influxdbConfig.getInfluxdbs().entrySet()) {
			try {
				Influxdb config = entry.getValue();
				String url = String.format("http://%s:%s", config.getHost(), config.getPort());
				InfluxDB influxDB = InfluxDBFactory.connect(url, config.getUsername(), config.getPassword());
				String database = config.getDatabase();

				influxDB.createDatabase(database);
				connections.put(entry.getKey(), new InfluxDBConnection(influxDB, database));
			} catch (Exception e) {
				Cat.logError("Create database error: " + entry.getValue().toString(), e);
			}
		}
		return connections;
	}

	@Override
	public InfluxDBConnection getConnection(String category) {
		InfluxDBConnection conn = m_connections.get(category);

		if (conn == null) {
			conn = m_connections.get(DEFAULT);
		}
		return conn;
	}

	public Map<String, InfluxDBConnection> getConnections() {
		return m_connections;
	}

	@Override
	public void initialize() throws InitializationException {
		// m_influxdbConfig = m_configManager.getConfig();
		// m_connections = buildConnections(m_influxdbConfig);
		//
		// TimerSyncTask.getInstance().register(new SyncHandler() {
		//
		// @Override
		// public void handle() throws Exception {
		// refreshConfig();
		// }
		//
		// @Override
		// public String getName() {
		// return "Influx-Datasource-Serivce";
		// }
		// });
	}

	protected void refreshConfig() {
		InfluxdbConfig config = m_configManager.getConfig();
		String oldxml = m_influxdbConfig.toString();

		if (!oldxml.equals(config.toString())) {
			m_influxdbConfig = config;

			try {
				m_connections = buildConnections(m_influxdbConfig);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}
}
