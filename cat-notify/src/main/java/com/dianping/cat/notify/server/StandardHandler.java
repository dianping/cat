package com.dianping.cat.notify.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandardHandler extends AbstractContainerHolder implements Handler {
	private final static Logger logger = LoggerFactory.getLogger(StandardHandler.class);

	private Map<String, Processor> processors;

	private void addProcessor(String key, Processor processor) {
		if (null == processors) {
			processors = new HashMap<String, Processor>();
		}
		processors.put(key, processor);
	}

	@Override
	public void init() {
		for(Processor processor : processors.values()){
			processor.init(this);	
		}
		logger.info("init StandardHandler");
	}

	@Override
	public Response process(Request req) {
		Response rep = new Response();
		Processor processor = processors.get(req.getProcessorKey());
		if (null != processor) {
			processor.process(req, rep);
		}
		return rep;
	}

	@SuppressWarnings("rawtypes")
   @Override
	public void setProcessors(Map processorMap) {
		if (null == processorMap) {
			return;
		}
		@SuppressWarnings("unchecked")
      Set<Object> entry = processorMap.keySet();
		for (Object key : entry) {
			Processor processor = (Processor) processorMap.get(key);
			addProcessor(key.toString(), processor);
		}
	}

}
