package com.dianping.cat.report.page.model.spi.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Splitters;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;

public abstract class BaseCompositeModelService<T> extends ModelServiceWithCalSupport implements ModelService<T>,
      Initializable {
	private static ExecutorService s_threadPool = Threads.forPool().getFixedThreadPool("Cat-ModelService", 50);

	// introduce another list is due to a bug inside Plexus ComponentList
	private List<ModelService<T>> m_allServices = new ArrayList<ModelService<T>>();

	@Inject
	private ServerConfigManager m_configManager;

	private String m_name;

	@Inject
	private List<ModelService<T>> m_services;

	public BaseCompositeModelService(String name) {
		m_name = name;
	}

	protected abstract BaseRemoteModelService<T> createRemoteService();

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public void initialize() throws InitializationException {
		m_allServices.addAll(m_services);

		String remoteServers = m_configManager.getConsoleRemoteServers();
		List<String> endpoints = Splitters.by(',').noEmptyItem().trim().split(remoteServers);

		for (String endpoint : endpoints) {
			int pos = endpoint.indexOf(':');
			String host = (pos > 0 ? endpoint.substring(0, pos) : endpoint);
			int port = (pos > 0 ? Integer.parseInt(endpoint.substring(pos + 1)) : 2281);
			BaseRemoteModelService<T> remote = createRemoteService();

			remote.setHost(host);
			remote.setPort(port);
			m_allServices.add(remote);
		}
	}

	@Override
	public ModelResponse<T> invoke(final ModelRequest request) {
		int size = m_allServices.size();
		final List<ModelResponse<T>> responses = new ArrayList<ModelResponse<T>>(size);
		final Semaphore semaphore = new Semaphore(0);
		final Transaction t = Cat.getProducer().newTransaction("ModelService", getClass().getSimpleName());
		int count = 0;

		t.setStatus(Message.SUCCESS);
		t.addData("request", request);
		t.addData("thread", Thread.currentThread());

		for (final ModelService<T> service : m_allServices) {
			if (!service.isEligable(request)) {
				continue;
			}

			// save current transaction so that child thread can access it
			if (service instanceof ModelServiceWithCalSupport) {
				((ModelServiceWithCalSupport) service).setParentTransaction(t);
			}

			s_threadPool.submit(new Runnable() {
				@Override
				public void run() {
					Cat.setup("model-service");

					try {
						ModelResponse<T> response = service.invoke(request);

						if (response.getException() != null) {
							logError(response.getException());
						}

						responses.add(response);
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
			semaphore.tryAcquire(count, 10000, TimeUnit.MILLISECONDS); // 10
			                                                           // seconds
			                                                           // timeout
		} catch (InterruptedException e) {
			// ignore it
			t.setStatus(e);
		} finally {
			t.complete();
		}

		ModelResponse<T> aggregated = new ModelResponse<T>();
		T report = merge(request, responses);

		aggregated.setModel(report);
		return aggregated;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		for (ModelService<T> service : m_allServices) {
			if (service.isEligable(request)) {
				return true;
			}
		}

		return false;
	}

	protected abstract T merge(ModelRequest request, final List<ModelResponse<T>> responses);

	public void setSerivces(ModelService<T>... services) {
		for (ModelService<T> service : services) {
			m_allServices.add(service);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append(getClass().getSimpleName()).append('[');
		sb.append("name=").append(m_name);
		sb.append(']');

		return sb.toString();
	}
}
