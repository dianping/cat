package com.dianping.cat.report;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Files;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageProducer;
import com.dianping.cat.report.model.ModelRequest;

public class HttpService implements Initializable {
	private ExecutorService m_threadPool;

	private int m_maxThreads = 50;

	private String m_prefixUri = "/cat/r/model";

	private URL buildUrl(Pair<String, Integer> endpoint, String name, ModelRequest request) throws MalformedURLException {
		StringBuilder sb = new StringBuilder(128);

		for (Entry<String, String> e : request.getProperties().entrySet()) {
			if (e.getValue() != null) {
				sb.append('&');
				sb.append(e.getKey()).append('=').append(e.getValue());
			}
		}

		String url = String.format("http://%s:%s%s/%s/%s/%s?op=xml%s", endpoint.getKey(), endpoint.getValue(), m_prefixUri, name,
		      request.getDomain(), request.getPeriod(), sb.toString());

		return new URL(url);
	}

	@Override
	public void initialize() throws InitializationException {
		m_threadPool = Threads.forPool().getFixedThreadPool("Cat-HttpService", m_maxThreads);
	}

	public void invoke(Pair<String, Integer> endpoint, Transaction parent, String name, ModelRequest request,
	      HttpServiceCallback callback) throws IOException {
		URL url = buildUrl(endpoint, name, request);

		m_threadPool.submit(new HttpServiceInvoker(parent, name, url, callback));
	}

	public void setMaxThreads(int maxThreads) {
		m_maxThreads = maxThreads;
	}

	public static interface HttpServiceCallback {
		public void onComplete(String xml);

		public void onException(Exception e, boolean timeout);
	}

	class HttpServiceInvoker extends TrackableTask {
		private String m_name;

		private URL m_url;

		private HttpServiceCallback m_callback;

		public HttpServiceInvoker(Transaction parent, String name, URL url, HttpServiceCallback callback) {
			super(name, parent);

			m_name = name;
			m_url = url;
			m_callback = callback;
		}

		@Override
		public void run() {
			Transaction t = newTransaction("ModelService", m_name);

			try {
				t.addData(m_url.toString());

				String xml = Files.forIO().readFrom(m_url.openStream(), "utf-8");
				int len = xml == null ? 0 : xml.length();

				t.addData("length", len);

				if (len > 0) {
					m_callback.onComplete(xml);
					t.setStatus(Message.SUCCESS);
				} else {
					t.setStatus("NoReport");
				}
			} catch (Exception e) {
				logError(e);
				t.setStatus(e);
			} finally {
				t.complete();
				Cat.reset();
			}
		}

		@Override
		public void shutdown() {
		}
	}

	static abstract class TrackableTask implements Task {
		private String m_name;

		private Transaction m_parent;

		public TrackableTask(String name, Transaction parent) {
			m_name = name;
			m_parent = parent;
		}

		@Override
		public String getName() {
			return m_name;
		}

		protected void logError(Throwable cause) {
			StringWriter writer = new StringWriter(2048);

			cause.printStackTrace(new PrintWriter(writer));

			if (cause instanceof Error) {
				logEvent("Error", cause.getClass().getName(), "ERROR", writer.toString());
			} else if (cause instanceof RuntimeException) {
				logEvent("RuntimeException", cause.getClass().getName(), "ERROR", writer.toString());
			} else {
				logEvent("Exception", cause.getClass().getName(), "ERROR", writer.toString());
			}
		}

		protected void logEvent(String type, String name, String status, String nameValuePairs) {
			DefaultMessageProducer cat = (DefaultMessageProducer) Cat.getProducer();
			Event event = cat.newEvent(m_parent, type, name);

			if (nameValuePairs != null && nameValuePairs.length() > 0) {
				event.addData(nameValuePairs);
			}

			event.setStatus(status);
			event.complete();
		}

		protected Transaction newTransaction(String type, String name) {
			DefaultMessageProducer cat = (DefaultMessageProducer) Cat.getProducer();
			Transaction transaction = cat.newTransaction(m_parent, type, name);

			return transaction;
		}

		protected void setParentTransaction(Transaction parentTransaction) {
			m_parent = parentTransaction;
		}
	}
}
