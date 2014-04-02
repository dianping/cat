package com.dianping.cat.report.task.metric;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Files;
import org.unidal.helper.Splitters;
import org.unidal.helper.Threads;
import org.unidal.helper.Urls;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.model.metric.MetricReportMerger;
import com.dianping.cat.report.page.model.spi.internal.ModelServiceWithCalSupport;
import com.dianping.cat.service.ModelRequest;

public class RemoteMetricReportService extends ModelServiceWithCalSupport implements Initializable {
	private static ExecutorService s_threadPool = Threads.forPool().getFixedThreadPool("Cat-Metric-Reload", 10);

	@Inject
	private ServerConfigManager m_configManager;

	private List<Pair<String, Integer>> m_servers = new ArrayList<Pair<String, Integer>>();

	private String m_serviceUri = "/cat/r/model";

	protected MetricReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}

	public URL buildUrl(ModelRequest request, Pair<String, Integer> hostPorts) throws MalformedURLException {
		StringBuilder sb = new StringBuilder(64);

		String host = hostPorts.getKey();
		int port = hostPorts.getValue();

		for (Entry<String, String> e : request.getProperties().entrySet()) {
			if (e.getValue() != null) {
				sb.append('&');
				sb.append(e.getKey()).append('=').append(e.getValue());
			}
		}
		String url = String.format("http://%s:%s%s/%s/%s/%s?op=xml%s", host, port, m_serviceUri, "metric",
		      request.getDomain(), request.getPeriod(), sb.toString());

		return new URL(url);
	}

	@Override
	public void initialize() throws InitializationException {
		String remoteServers = m_configManager.getConsoleRemoteServers();
		List<String> endpoints = Splitters.by(',').noEmptyItem().trim().split(remoteServers);

		for (String endpoint : endpoints) {
			int pos = endpoint.indexOf(':');
			String host = (pos > 0 ? endpoint.substring(0, pos) : endpoint);
			int port = (pos > 0 ? Integer.parseInt(endpoint.substring(pos + 1)) : 2281);

			m_servers.add(new Pair<String, Integer>(host, port));
		}
	}

	public MetricReport invoke(final ModelRequest request) {
		final Semaphore semaphore = new Semaphore(0);
		final Transaction t = Cat.getProducer().newTransaction("ModelService", getClass().getSimpleName());
		final List<MetricReport> reports = Collections.synchronizedList(new ArrayList<MetricReport>());
		final List<String> ips = Collections.synchronizedList(new ArrayList<String>());
		int count = 0;
		t.setStatus(Message.SUCCESS);
		t.addData("request", request);
		t.addData("thread", Thread.currentThread());

		setParentTransaction(t);

		for (Pair<String, Integer> temp : m_servers) {
			final Pair<String, Integer> server = temp;

			s_threadPool.submit(new Runnable() {
				@Override
				public void run() {
					try {
						MetricReport report = invoke(request, server);

						if (report != null) {
							reports.add(report);
							ips.add(server.getKey());
						}
					} catch (Exception e) {
						logError(e);
						t.setStatus(e);
					} finally {
						semaphore.release();
						Cat.reset();
					}
				}
			});
			count++;
		}

		try {
			semaphore.tryAcquire(count, 10000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// ignore it
			t.setStatus(e);
		} finally {
			t.complete();
		}

		if (reports.size() != count) {
			Cat.logError(new FetchMetricReportException("Error when request metric http api, servers:" + ips.size() + " "
			      + ips.toString()));
			return null;
		} else {
			MetricReportMerger merger = new MetricReportMerger(new MetricReport(request.getDomain()));

			for (MetricReport report : reports) {
				report.accept(merger);
			}
			return merger.getMetricReport();
		}
	}

	public MetricReport invoke(ModelRequest request, Pair<String, Integer> hostPorts) {
		Transaction t = newTransaction("ModelService", getClass().getSimpleName());

		try {
			URL url = buildUrl(request, hostPorts);

			t.addData(url.toString());

			InputStream in = Urls.forIO().connectTimeout(300).readTimeout(3000).openStream(url.toExternalForm());
			String xml = Files.forIO().readFrom(in, "utf-8");
			int len = xml == null ? 0 : xml.length();

			t.addData("length", len);

			if (len > 0) {
				MetricReport report = buildModel(xml);

				t.setStatus(Message.SUCCESS);
				return report;
			} else {
				t.setStatus("NoReport");
			}
		} catch (Exception e) {
			logError(e);
			t.setStatus(e);
		} finally {
			t.complete();
		}
		return null;
	}
}
