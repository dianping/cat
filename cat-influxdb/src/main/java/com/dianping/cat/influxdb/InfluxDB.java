package com.dianping.cat.influxdb;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.dianping.cat.influxdb.dto.BatchPoints;
import com.dianping.cat.influxdb.dto.Point;
import com.dianping.cat.influxdb.dto.Pong;
import com.dianping.cat.influxdb.dto.Query;
import com.dianping.cat.influxdb.dto.QueryResult;

public interface InfluxDB {

	public enum ConsistencyLevel {
		/** Write succeeds only if write reached all cluster members. */
		ALL("all"),
		/** Write succeeds if write reached any cluster members. */
		ANY("any"),
		/** Write succeeds if write reached at least one cluster members. */
		ONE("one"),
		/** Write succeeds only if write reached a quorum of cluster members. */
		QUORUM("quorum");
		private final String value;

		private ConsistencyLevel(final String value) {
			this.value = value;
		}

		public String value() {
			return this.value;
		}
	}

	/** Controls the level of logging of the REST layer. */
	public enum LogLevel {
		/** No logging. */
		NONE,
		/** Log only the request method and URL and the response status code and execution time. */
		BASIC,
		/** Log the basic information along with request and response headers. */
		HEADERS,
		/**
		 * Log the headers, body, and metadata for both requests and responses.
		 * <p>
		 * Note: This requires that the entire request and response body be buffered in memory!
		 */
		FULL;
	}

	public static final String ID = "Influx";

	public void createDatabase(final String name);

	public void deleteDatabase(final String name);

	public List<String> describeDatabases();

	public void disableBatch();

	public InfluxDB enableBatch(final int actions, final int flushDuration, final TimeUnit flushDurationTimeUnit);

	public Pong ping();

	public QueryResult query(final Query query);

	public QueryResult query(final Query query, TimeUnit timeUnit);

	public InfluxDB setLogLevel(final LogLevel logLevel);

	public String version();

	public void write(final BatchPoints batchPoints);

	public void write(final String database, final String retentionPolicy, final Point point);

}