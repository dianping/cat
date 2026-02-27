package com.dianping.cat.message.pipeline;

public interface MessageSource {
	Object getMessage();
	
	MessagePipeline pipeline();
}
