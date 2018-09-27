package com.dianping.cat.influxdb.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import retrofit.RestAdapter;
import retrofit.client.Header;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.mime.TypedString;

import com.dianping.cat.Cat;
import com.dianping.cat.influxdb.InfluxDB;
import com.dianping.cat.influxdb.dto.BatchPoints;
import com.dianping.cat.influxdb.dto.Point;
import com.dianping.cat.influxdb.dto.Pong;
import com.dianping.cat.influxdb.dto.Query;
import com.dianping.cat.influxdb.dto.QueryResult;
import com.dianping.cat.influxdb.impl.BatchProcessor.BatchEntry;
import com.dianping.cat.message.Transaction;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.squareup.okhttp.OkHttpClient;

public class InfluxDBImpl implements InfluxDB {
	private final String username;

	private final String password;

	private final RestAdapter restAdapter;

	private final InfluxDBService influxDBService;

	private BatchProcessor batchProcessor;

	private final AtomicBoolean batchEnabled = new AtomicBoolean(false);

	private final AtomicLong writeCount = new AtomicLong();

	private final AtomicLong unBatchedCount = new AtomicLong();

	private final AtomicLong batchedCount = new AtomicLong();

	private LogLevel logLevel = LogLevel.NONE;

	public InfluxDBImpl(final String url, final String username, final String password) {
		super();
		this.username = username;
		this.password = password;
		OkHttpClient okHttpClient = new OkHttpClient();
		this.restAdapter = new RestAdapter.Builder().setEndpoint(url).setErrorHandler(new InfluxDBErrorHandler())
		      .setClient(new OkClient(okHttpClient)).build();
		this.influxDBService = this.restAdapter.create(InfluxDBService.class);
	}

	@Override
	public void createDatabase(final String name) {
		Preconditions.checkArgument(!name.contains("-"), "Databasename cant contain -");
		this.influxDBService.query(this.username, this.password, "CREATE DATABASE " + name);
	}

	@Override
	public void deleteDatabase(final String name) {
		this.influxDBService.query(this.username, this.password, "DROP DATABASE " + name);
	}

	@Override
	public List<String> describeDatabases() {
		QueryResult result = this.influxDBService.query(this.username, this.password, "SHOW DATABASES");
		// {"results":[{"series":[{"name":"databases","columns":["name"],"values":[["mydb"]]}]}]}
		// Series [name=databases, columns=[name], values=[[mydb], [unittest_1433605300968]]]
		List<List<Object>> databaseNames = result.getResults().get(0).getSeries().get(0).getValues();
		List<String> databases = Lists.newArrayList();
		if (databaseNames != null) {
			for (List<Object> database : databaseNames) {
				databases.add(database.get(0).toString());
			}
		}
		return databases;
	}

	@Override
	public void disableBatch() {
		this.batchEnabled.set(false);
		this.batchProcessor.flush();
		if (this.logLevel != LogLevel.NONE) {
			System.out.println("total writes:" + this.writeCount.get() + " unbatched:" + this.unBatchedCount.get()
			      + "batchPoints:" + this.batchedCount);
		}
	}

	@Override
	public InfluxDB enableBatch(final int actions, final int flushDuration, final TimeUnit flushDurationTimeUnit) {
		if (this.batchEnabled.get()) {
			throw new IllegalArgumentException("BatchProcessing is already enabled.");
		}
		this.batchProcessor = BatchProcessor.builder(this).actions(actions)
		      .interval(flushDuration, flushDurationTimeUnit).build();
		this.batchEnabled.set(true);
		return this;
	}

	@Override
	public Pong ping() {
		Stopwatch watch = Stopwatch.createStarted();
		Response response = null;
		Transaction t = Cat.newTransaction(InfluxDB.ID, "ping");

		try {
			response = this.influxDBService.ping();
			t.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			Cat.logError(e);
			t.setStatus(e);
		} finally {
			t.complete();
		}

		if (response != null) {
			List<Header> headers = response.getHeaders();
			String version = "unknown";

			for (Header header : headers) {
				if (null != header.getName() && header.getName().equalsIgnoreCase("X-Influxdb-Version")) {
					version = header.getValue();
				}
			}
			Pong pong = new Pong();
			pong.setVersion(version);
			pong.setResponseTime(watch.elapsed(TimeUnit.MILLISECONDS));
			return pong;
		} else {
			return null;
		}
	}

	@Override
	public QueryResult query(final Query query) {
		Transaction t = Cat.newTransaction(InfluxDB.ID, query.getMethod());

		try {
			QueryResult response = this.influxDBService.query(this.username, this.password, query.getDatabase(),
			      query.getCommand());

			t.setStatus(Transaction.SUCCESS);
			return response;
		} catch (Exception e) {
			Cat.logError("Query command: " + query.getCommand(), e);
			t.setStatus(e);
			return null;
		} finally {
			t.complete();
		}
	}

	@Override
	public QueryResult query(final Query query, final TimeUnit timeUnit) {
		Transaction t = Cat.newTransaction(InfluxDB.ID, query.getMethod());

		try {
			QueryResult response = this.influxDBService.query(this.username, this.password, query.getDatabase(),
			      TimeUtil.toTimePrecision(timeUnit), query.getCommand());

			t.setStatus(Transaction.SUCCESS);
			return response;
		} catch (Exception e) {
			Cat.logError("Query command: " + query.getCommand(), e);
			t.setStatus(e);
			return null;
		} finally {
			t.complete();
		}
	}

	@Override
	public InfluxDB setLogLevel(final LogLevel logLevel) {
		switch (logLevel) {
		case NONE:
			this.restAdapter.setLogLevel(retrofit.RestAdapter.LogLevel.NONE);
			break;
		case BASIC:
			this.restAdapter.setLogLevel(retrofit.RestAdapter.LogLevel.BASIC);
			break;
		case HEADERS:
			this.restAdapter.setLogLevel(retrofit.RestAdapter.LogLevel.HEADERS);
			break;
		case FULL:
			this.restAdapter.setLogLevel(retrofit.RestAdapter.LogLevel.FULL);
			break;
		default:
			break;
		}
		this.logLevel = logLevel;
		return this;
	}

	@Override
	public String version() {
		return ping().getVersion();
	}

	@Override
	public void write(final BatchPoints batchPoints) {
		this.batchedCount.addAndGet(batchPoints.getPoints().size());
		TypedString lineProtocol = new TypedString(batchPoints.lineProtocol());
		Transaction t = Cat.newTransaction(InfluxDB.ID, "batchInsert");

		try {
			this.influxDBService.writePoints(this.username, this.password, batchPoints.getDatabase(), batchPoints
			      .getRetentionPolicy(), TimeUtil.toTimePrecision(TimeUnit.NANOSECONDS), batchPoints.getConsistency()
			      .value(), lineProtocol);
			t.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			Cat.logError(batchPoints.toString(), e);
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}

	@Override
	public void write(final String database, final String retentionPolicy, final Point point) {
		if (this.batchEnabled.get()) {
			BatchEntry batchEntry = new BatchEntry(point, database, retentionPolicy);
			this.batchProcessor.put(batchEntry);
		} else {
			BatchPoints batchPoints = BatchPoints.database(database).retentionPolicy(retentionPolicy).build();
			batchPoints.point(point);

			Transaction t = Cat.newTransaction(InfluxDB.ID, "insert");

			try {
				this.write(batchPoints);
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				Cat.logError(batchPoints.toString(), e);
				t.setStatus(e);
			} finally {
				t.complete();
			}
			this.unBatchedCount.incrementAndGet();
		}
		this.writeCount.incrementAndGet();
	}

}
