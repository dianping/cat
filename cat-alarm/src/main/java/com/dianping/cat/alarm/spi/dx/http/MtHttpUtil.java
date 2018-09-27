package com.dianping.cat.alarm.spi.dx.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;

/**
 * @author zhangdongxiao
 * @version 1.0
 * @created Feb 20, 2012
 */
public class MtHttpUtil {

	private static HttpClient httpClient = new DefaultHttpClient(new PoolingClientConnectionManager());

	private final static int DEFAULT_MAX_CONNECTIONS = 20;

	private final static int DEFAULT_SO_TIMEOUT = 10000;

	private final static int DEFAULT_CONN_TIMEOUT = 2000;

	public static void setMaxConnections(int connectionNum) {
		PoolingClientConnectionManager manager = (PoolingClientConnectionManager) httpClient.getConnectionManager();
		if (connectionNum > 0) {
			manager.setMaxTotal(connectionNum > DEFAULT_MAX_CONNECTIONS ? connectionNum : DEFAULT_MAX_CONNECTIONS);
			manager.setDefaultMaxPerRoute(manager.getMaxTotal());
		}
	}

	public static <T> T post(String url, String content, Class<T> returnType, String... headers) {
		return post(url, content, returnType, DEFAULT_SO_TIMEOUT, DEFAULT_CONN_TIMEOUT, headers);
	}

	public static <T> T post(String url, String content, Class<T> returnType, int soTimeout, int conTimeout,
	      String... headers) {
		try {
			HttpPost post = new HttpPost(url);
			HttpParams params = new BasicHttpParams();
			params.setParameter(CoreConnectionPNames.SO_TIMEOUT, soTimeout);
			params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, conTimeout);
			post.setParams(params);
			StringEntity body = new StringEntity(content, "utf-8");
			body.setContentType("Content-Type: application/json; charset=utf-8");
			post.setEntity(body);
			post.setHeader("Content-Type", "application/json; charset=utf-8");
			return execute(post, returnType, headers);
		} catch (UnsupportedEncodingException e) {
			throw new HttpException(506, "UnsupportedEncodingException(utf-8)", e);
		}
	}

	public static <T> T put(String url, String content, Class<T> returnType, String... headers) {
		return put(url, content, returnType, DEFAULT_SO_TIMEOUT, DEFAULT_CONN_TIMEOUT, headers);
	}

	public static <T> T put(String url, String content, Class<T> returnType, int soTimeout, int conTimeout,
	      String... headers) {
		try {
			HttpPut put = new HttpPut(url);
			HttpParams params = new BasicHttpParams();
			params.setParameter(CoreConnectionPNames.SO_TIMEOUT, soTimeout);
			params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, conTimeout);
			put.setParams(params);
			StringEntity body = new StringEntity(content, "utf-8");
			body.setContentType("Content-Type: application/json; charset=utf-8");
			put.setEntity(body);
			put.setHeader("Content-Type", "application/json; charset=utf-8");
			return execute(put, returnType, headers);
		} catch (UnsupportedEncodingException e) {
			throw new HttpException(506, "UnsupportedEncodingException(utf-8)", e);
		}
	}

	public static <T> T get(String url, Class<T> returnType, String... headers) {
		return execute(new HttpGet(url), returnType, headers);
	}

	public static <T> T delete(String url, Class<T> returnType, String... headers) {
		return execute(new HttpDelete(url), returnType, headers);
	}

	@SuppressWarnings("unchecked")
	public static <T> T execute(HttpUriRequest req, Class<T> returnType, String... headers) {

		try {
			// 插入basic验证等基础逻辑
			for (int i = 0; i < headers.length; i += 2) {
				req.addHeader(headers[i], headers[i + 1]);
			}
			HttpResponse jres = httpClient.execute(req);

			HttpEntity ent = jres.getEntity();
			String content = ent != null ? EntityUtils.toString(ent, "UTF-8") : null;
			if (jres.getStatusLine().getStatusCode() >= 200 && jres.getStatusLine().getStatusCode() < 300) {
				if (returnType == String.class) {
					return (T) content;
				}
				return JSON.parseObject(content, returnType);
			} else {
				throw new HttpException(jres.getStatusLine().getStatusCode(), jres.getStatusLine().getReasonPhrase(),
				      content);
			}
		} catch (IOException e) {
			req.abort();
			throw new HttpException(505, "IOException", e);
		}
	}

	public static void post(String url, ByteArrayBody[] files, String... headers) {
		HttpPost post = new HttpPost(url);

		MultipartEntity body = new MultipartEntity();
		for (ByteArrayBody file : files) {
			body.addPart("file", file);
		}
		post.setEntity(body);

		execute(post, Object.class, headers);
	}

	public static class MtResponse<T> {
		private T data;

		private MtError error;

		public T getData() {
			return data;
		}

		public void setData(T data) {
			this.data = data;
		}

		public MtError getError() {
			return error;
		}

		public void setError(MtError error) {
			this.error = error;
		}
	}

	public static class MtError {
		private int code;

		private String type;

		private String message;

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	}
}