package com.dianping.cat.influxdb.impl;

import com.dianping.cat.influxdb.dto.QueryResult;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Query;
import retrofit.mime.TypedString;

interface InfluxDBService {

	public static final String U = "u";

	public static final String P = "p";

	public static final String Q = "q";

	public static final String DB = "db";

	public static final String RP = "rp";

	public static final String PRECISION = "precision";

	public static final String CONSISTENCY = "consistency";

	public static final String EPOCH = "epoch";

	@GET("/ping")
	public Response ping();

	@GET("/query")
	public QueryResult query(@Query(U) String username, @Query(P) String password, @Query(Q) String query);

	@GET("/query")
	public QueryResult query(@Query(U) String username, @Query(P) String password, @Query(DB) String db,
	      @Query(Q) String query);

	@GET("/query")
	public QueryResult query(@Query(U) String username, @Query(P) String password, @Query(DB) String db,
	      @Query(EPOCH) String epoch, @Query(Q) String query);

	@Headers("Content-Type: text/plain")
	@POST("/write")
	public Response writePoints(@Query(U) String username, @Query(P) String password, @Query(DB) String database,
	      @Query(RP) String retentionPolicy, @Query(PRECISION) String precision, @Query(CONSISTENCY) String consistency,
	      @Body TypedString batchPoints);

}
