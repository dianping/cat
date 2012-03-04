package com.dianping.cat.report.page.model.transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.site.lookup.annotation.Inject;

public class CompositeTransactionModelService implements ModelService<TransactionReport>, Initializable {
	@Inject
	private List<ModelService<TransactionReport>> m_services;

	private ExecutorService m_threadPool;

	@Override
	public void initialize() throws InitializationException {
		m_threadPool = Executors.newFixedThreadPool(10);
	}

	@Override
	public ModelResponse<TransactionReport> invoke(final ModelRequest request) {
		int size = m_services.size();
		final List<ModelResponse<TransactionReport>> responses = new ArrayList<ModelResponse<TransactionReport>>(size);
		final Semaphore semaphore = new Semaphore(0);
		int count = 0;

		for (final ModelService<TransactionReport> service : m_services) {
			if (service.isEligable(request)) {
				m_threadPool.submit(new Runnable() {
					@Override
					public void run() {
						try {
							responses.add(service.invoke(request));
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							semaphore.release();
						}
					}
				});
				count++;
			}
		}

		try {
			semaphore.tryAcquire(count, 5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// ignore it
		}

		ModelResponse<TransactionReport> aggregated = new ModelResponse<TransactionReport>();
		TransactionReportMerger merger = null;

		for (ModelResponse<TransactionReport> response : responses) {
			if (response != null) {
				TransactionReport model = response.getModel();

				if (model != null) {
					if (merger == null) {
						merger = new TransactionReportMerger(model);
					} else {
						model.accept(merger);
					}
				}
			}
		}

		aggregated.setModel(merger == null ? null : merger.getTransactionReport());
		return aggregated;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		for (ModelService<TransactionReport> service : m_services) {
			if (service.isEligable(request)) {
				return true;
			}
		}

		return false;
	}

	public void setSerivces(ModelService<TransactionReport>... services) {
		m_services = Arrays.asList(services);
	}
}
