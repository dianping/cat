package com.dianping.cat.influxdb.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.dianping.cat.influxdb.InfluxDB;
import com.dianping.cat.influxdb.dto.BatchPoints;
import com.dianping.cat.influxdb.dto.Point;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class BatchProcessor {

	protected final BlockingQueue<BatchEntry> queue = new LinkedBlockingQueue<BatchEntry>();

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	final InfluxDBImpl influxDB;

	final int actions;

	private final TimeUnit flushIntervalUnit;

	private final int flushInterval;

	BatchProcessor(final InfluxDBImpl influxDB, final int actions, final TimeUnit flushIntervalUnit,
	      final int flushInterval) {
		super();
		this.influxDB = influxDB;
		this.actions = actions;
		this.flushIntervalUnit = flushIntervalUnit;
		this.flushInterval = flushInterval;

		// Flush at specified Rate
		this.scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				write();
			}
		}, this.flushInterval, this.flushInterval, this.flushIntervalUnit);

	}

	public static Builder builder(final InfluxDB influxDB) {
		return new Builder(influxDB);
	}

	void flush() {
		this.write();
		this.scheduler.shutdown();
	}

	void put(final BatchEntry batchEntry) {
		this.queue.add(batchEntry);
		if (this.queue.size() >= this.actions) {
			write();
		}
	}

	void write() {
		if (this.queue.isEmpty()) {
			return;
		}

		Map<String, BatchPoints> databaseToBatchPoints = Maps.newHashMap();
		List<BatchEntry> batchEntries = new ArrayList<BatchEntry>(this.queue.size());
		this.queue.drainTo(batchEntries);

		for (BatchEntry batchEntry : batchEntries) {
			String dbName = batchEntry.getDb();
			if (!databaseToBatchPoints.containsKey(dbName)) {
				BatchPoints batchPoints = BatchPoints.database(dbName).retentionPolicy(batchEntry.getRp()).build();
				databaseToBatchPoints.put(dbName, batchPoints);
			}
			Point point = batchEntry.getPoint();
			databaseToBatchPoints.get(dbName).point(point);
		}

		for (BatchPoints batchPoints : databaseToBatchPoints.values()) {
			BatchProcessor.this.influxDB.write(batchPoints);
		}
	}

	static class BatchEntry {
		private final Point point;

		private final String db;

		private final String rp;

		public BatchEntry(final Point point, final String db, final String rp) {
			super();
			this.point = point;
			this.db = db;
			this.rp = rp;
		}

		public String getDb() {
			return this.db;
		}

		public Point getPoint() {
			return this.point;
		}

		public String getRp() {
			return this.rp;
		}
	}

	public static final class Builder {
		private final InfluxDBImpl influxDB;

		private int actions;

		private TimeUnit flushIntervalUnit;

		private int flushInterval;

		public Builder(final InfluxDB influxDB) {
			this.influxDB = (InfluxDBImpl) influxDB;
		}

		public Builder actions(final int maxActions) {
			this.actions = maxActions;
			return this;
		}

		public BatchProcessor build() {
			Preconditions.checkNotNull(this.actions, "actions may not be null");
			Preconditions.checkNotNull(this.flushInterval, "flushInterval may not be null");
			Preconditions.checkNotNull(this.flushIntervalUnit, "flushIntervalUnit may not be null");
			return new BatchProcessor(this.influxDB, this.actions, this.flushIntervalUnit, this.flushInterval);
		}

		public Builder interval(final int interval, final TimeUnit unit) {
			this.flushInterval = interval;
			this.flushIntervalUnit = unit;
			return this;
		}
	}
}
