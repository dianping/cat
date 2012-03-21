package com.dianping.cat.report.page.model.problem;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultMerger;
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
		final CountDownLatch latch = new CountDownLatch(size);

		for (final ModelService<ProblemReport> service : m_services) {
			m_threadPool.submit(new Runnable() {
				@Override
				public void run() {
					try {
						responses.add(service.invoke(request));
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						latch.countDown();
					}
				}
			});
		}

		try {
			latch.await(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// ignore it
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
