package com.dianping.cat.report.page.model.problem;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultMerger;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.site.helper.Splitters;
import com.site.lookup.annotation.Inject;

public class CompositeProblemService implements ModelService<ProblemReport>, Initializable {
	@Inject
	private List<ModelService<ProblemReport>> m_services;

	private ExecutorService m_threadPool;

	@Override
	public void initialize() throws InitializationException {
		m_threadPool = Executors.newFixedThreadPool(10);
	}

	@Override
	public ModelResponse<ProblemReport> invoke(final ModelRequest request) {
		int size = m_services.size();
		final List<ModelResponse<ProblemReport>> responses = new ArrayList<ModelResponse<ProblemReport>>(size);
		final Semaphore semaphore = new Semaphore(0);
		final Transaction t = Cat.getProducer().newTransaction("ModelService", "Problem");
		int count = 0;

		t.setStatus(Message.SUCCESS);
		t.addData("request", request);

		for (final ModelService<ProblemReport> service : m_services) {
			if (!service.isEligable(request)) {
				continue;
			}

			m_threadPool.submit(new Runnable() {
				@Override
				public void run() {
					try {
						responses.add(service.invoke(request));
						
						t.addData(service.toString());
					} catch (Exception e) {
						logError(t, e);
						t.setStatus(e);
					} finally {
						semaphore.release();
					}
				}

				void logError(Transaction t, Throwable cause) {
					StringWriter writer = new StringWriter(2048);

					cause.printStackTrace(new PrintWriter(writer));

					if (cause instanceof Error) {
						logEvent(t, "Error", cause.getClass().getName(), "ERROR", writer.toString());
					} else if (cause instanceof RuntimeException) {
						logEvent(t, "RuntimeException", cause.getClass().getName(), "ERROR", writer.toString());
					} else {
						logEvent(t, "Exception", cause.getClass().getName(), "ERROR", writer.toString());
					}
				}

				void logEvent(Transaction t, String type, String name, String status, String nameValuePairs) {
					Event event = new DefaultEvent(type, name);

					if (nameValuePairs != null && nameValuePairs.length() > 0) {
						event.addData(nameValuePairs);
					}

					event.setStatus(status);
					event.complete();
					t.addChild(event);
				}
			});
			count++;
		}

		try {
			semaphore.tryAcquire(count, 5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// ignore it
			t.setStatus(e);
		} finally {
			t.complete();
		}

		ModelResponse<ProblemReport> aggregated = new ModelResponse<ProblemReport>();
		DefaultMerger merger = null;

		for (ModelResponse<ProblemReport> response : responses) {
			if (response != null) {
				ProblemReport model = response.getModel();

				if (model != null) {
					if (merger == null) {
						merger = new DefaultMerger(model);
					} else {
						model.accept(merger);
					}
				}
			}
		}

		aggregated.setModel(merger == null ? null : merger.getProblemReport());
		return aggregated;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		for (ModelService<ProblemReport> service : m_services) {
			if (service.isEligable(request)) {
				return true;
			}
		}

		return false;
	}

	public void setSerivces(ModelService<ProblemReport>... services) {
		m_services = Arrays.asList(services);
	}

	/**
	 * Inject remote servers to load transaction model.
	 * <p>
	 * 
	 * For example, servers: 192.168.1.1:2281,192.168.1.2,192.168.1.3
	 * 
	 * @param servers
	 *           server list separated by comma(',')
	 */
	public void setRemoteServers(String servers) {
		List<String> endpoints = Splitters.by(',').split(servers);
		String localAddress = null;
		String localHost = null;

		try {
			localAddress = InetAddress.getLocalHost().getHostAddress();
			localHost = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// ignore it
		}

		for (String endpoint : endpoints) {
			int pos = endpoint.indexOf(':');
			String host = (pos > 0 ? endpoint.substring(0, pos) : endpoint);
			int port = (pos > 0 ? Integer.parseInt(endpoint.substring(pos) + 1) : 2281);

			if (port == 2281) {
				if ("localhost".equals(host) || host.startsWith("127.0.")) {
					// exclude localhost
					continue;
				} else if (host.equals(localAddress) || host.equals(localHost)) {
					// exclude itself
					continue;
				}
			}

			RemoteProblemService remote = new RemoteProblemService();

			remote.setHost(host);
			remote.setPort(port);
			m_services.add(remote);
		}
	}
}
