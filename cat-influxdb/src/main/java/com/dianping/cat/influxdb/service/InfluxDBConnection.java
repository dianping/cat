package com.dianping.cat.influxdb.service;

import com.dianping.cat.influxdb.InfluxDB;

public class InfluxDBConnection {

	private String m_dataBase;

	private InfluxDB m_influxDB;

	public InfluxDBConnection(InfluxDB influxDB, String database) {
		m_influxDB = influxDB;
		m_dataBase = database;
	}

	public String getDataBase() {
		return m_dataBase;
	}

	public InfluxDB getInfluxDB() {
		return m_influxDB;
	}

	public void setDataBase(String dataBase) {
		m_dataBase = dataBase;
	}

	public void setInfluxDB(InfluxDB influxDB) {
		m_influxDB = influxDB;
	}
}
