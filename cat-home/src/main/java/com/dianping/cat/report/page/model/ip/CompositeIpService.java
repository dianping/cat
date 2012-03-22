package com.dianping.cat.report.page.model.ip;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.ip.model.transform.DefaultMerger;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.site.lookup.annotation.Inject;

public class CompositeIpService implements ModelService<IpReport>, Initializable {
	@Inject
	private List<ModelService<IpReport>> m_services;

	private ExecutorService m_threadPool;

	@Override
	public void initialize() throws InitializationException {
		m_threadPool = Executors.newFixedThreadPool(10);
	}

	@Override
	public ModelResponse<IpReport> invoke(final ModelRequest request) {
		int size = m_services.size();
		final List<ModelResponse<IpReport>> responses = new ArrayList<ModelResponse<IpReport>>(size);
		final CountDownLatch latch = new CountDownLatch(size);

		final Transaction t = Cat.getProducer().newTransaction("ModelService", "Ip");
		t.setStatus(Message.SUCCESS);
		t.addData("request", request);

		for (final ModelService<IpReport> service : m_services) {
			m_threadPool.submit(new Runnable() {
				@Override
				public void run() {
					try {
						responses.add(service.invoke(request));
						
						t.addData(service.toString());
						logEvent(t, "Client", "Ip", Message.SUCCESS, service.toString());
					} catch (Exception e) {
						logError(t, e);
						t.setStatus(e);
					} finally {
						latch.countDown();
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
		}

		try {
			latch.await(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			t.setStatus(e);// ignore it
		} finally {
			t.complete();
		}

		ModelResponse<IpReport> aggregated = new ModelResponse<IpReport>();
		DefaultMerger merger = null;

		for (ModelResponse<IpReport> response : responses) {
			if (response != null) {
				IpReport model = response.getModel();

				if (model != null) {
					if (merger == null) {
						merger = new IpReportMerger(model);
					} else {
						model.accept(merger);
					}
				}
			}
		}

		aggregated.setModel(merger == null ? null : merger.getIpReport());
		return aggregated;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		for (ModelService<IpReport> service : m_services) {
			if (service.isEligable(request)) {
				return true;
			}
		}

		return false;
	}

	public void setSerivces(ModelService<IpReport>... services) {
		m_services = Arrays.asList(services);
	}
	
	public void setRemoteServers(String servers) {
		
	}

	@Override
   public String getName() {
	   // TODO Auto-generated method stub
	   return null;
   }
}
