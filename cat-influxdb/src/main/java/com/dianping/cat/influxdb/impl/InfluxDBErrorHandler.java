package com.dianping.cat.influxdb.impl;

import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.dianping.cat.Cat;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

class InfluxDBErrorHandler implements ErrorHandler {

	@Override
	public Throwable handleError(final RetrofitError cause) {
		Response r = cause.getResponse();
		if (r != null && r.getStatus() >= 400) {
			try (InputStreamReader reader = new InputStreamReader(r.getBody().in(), Charsets.UTF_8)) {
				return new RuntimeException(CharStreams.toString(reader));
			} catch (IOException e) {
				Cat.logError(e);
				e.printStackTrace();
			}
		}
		return cause;
	}
}
