package com.dianping.cat.report.service;

import java.util.ArrayList;
import java.util.Collections;
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
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public abstract class BaseCompositeModelService<T> extends ModelServiceWithCalSupport implements ModelService<T>,
      Initializable {
	private static ExecutorService s_threadPool = Threads.forPool().getFixedThreadPool("Cat-ModelService", 30);

	// introduce another list is due to a bug inside Plexus ComponentList
	private List<ModelService<T>> m_allServices = new ArrayList<ModelService<T>>();

	@Inject
	protected ServerConfigManager m_configManager;

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
		int requireSize = 0;
		final List<ModelResponse<T>> responses = Collections.synchronizedList(new ArrayList<ModelResponse<T>>());
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
			requireSize++;
			
			s_threadPool.submit(new Runnable() {
				@Override
				public void run() {
					try {
						ModelResponse<T> response = service.invoke(request);

						if (response.getException() != null) {
							logError(response.getException());
						}
						if (response != null && response.getModel() != null) {
							responses.add(response);
						}
					} catch (Exception e) {
						logError(e);
						t.setStatus(e);
					} finally {
						semaphore.release();
					}
				}
			});

			count++;
		}

		try {
			semaphore.tryAcquire(count, 10000, TimeUnit.MILLISECONDS); // 10 seconds timeout
		} catch (InterruptedException e) {
			// ignore it
			t.setStatus(e);
		} finally {
			t.complete();
		}

		String requireAll = request.getProperty("requireAll");

		if (requireAll != null && responses.size() != requireSize) {
			String data = "require:" + requireSize + " actual:" + responses.size();
			Cat.logEvent("FetchReportError:" + this.getClass().getSimpleName(), request.getDomain(), Event.SUCCESS, data);

			return null;
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
