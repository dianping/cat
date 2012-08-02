package com.dianping.cat.notify.dao;

import java.util.List;

import org.junit.Test;

import com.dianping.cat.notify.BaseTest;
import com.dianping.cat.notify.model.Subscriber;

public class SubscriberDaoTest extends BaseTest {
	
	@Test
	public void testInsert(){
		SubscriberDao subscriberDao = (SubscriberDao)super.context.getBean("subscriberDao");
		try {
			List<Object> subscriberList = subscriberDao.getAllMailSubscriber();
			for(Object element : subscriberList){
				Subscriber subscriber = (Subscriber) element;
				subscriber.getAddress();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
