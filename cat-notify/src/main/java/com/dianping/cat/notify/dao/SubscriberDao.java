package com.dianping.cat.notify.dao;

import java.util.List;

import com.dianping.cat.notify.model.Subscriber;

public interface SubscriberDao {
	
    public List<Object> getAllMailSubscriber() throws Exception;
    
    public Subscriber getSubscriberByDomain(String domain,int type) throws Exception;

}
