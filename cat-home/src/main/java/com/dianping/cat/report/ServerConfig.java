package com.dianping.cat.report;

import java.util.List;

import com.site.helper.Splitters;
import com.site.lookup.annotation.Inject;

public class ServerConfig {
	
	@Inject
	private List<String> consumerServers;

	@Inject
	private String fileServer;

	public List<String> getConsumerServers() {
   	return consumerServers;
   }

	public void setConsumerServers(String servers){
		consumerServers = Splitters.by(',').noEmptyItem().split(servers);
	}

	public void setFileServer(String fileServer) {
   	this.fileServer = fileServer;
   }

	public String getFileServer() {
   	return fileServer;
   }
	
}
