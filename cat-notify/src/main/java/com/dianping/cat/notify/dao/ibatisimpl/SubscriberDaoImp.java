package com.dianping.cat.notify.dao.ibatisimpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.notify.dao.SubscriberDao;
import com.dianping.cat.notify.model.Subscriber;

public class SubscriberDaoImp implements SubscriberDao {
	private BaseDao baseDao;

	public void setBaseDao(BaseDao baseDao) {
		this.baseDao = baseDao;
	}
	
	@Override
	public List<Object> getAllMailSubscriber() throws Exception {
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("type", Subscriber.MAIL);
		return baseDao.executeQueryForList("Subscriber.selectAllSubscriber", map);
	}

	@Override
	public Subscriber getSubscriberByDomain(String domain,int type) throws Exception {
		Map<String,Object> map=new HashMap<String,Object>();
		if(type != Subscriber.MAIL && type != Subscriber.SMS){
			return null;
		}
		map.put("domain", domain);
		map.put("type", type);
		return (Subscriber) baseDao.executeQueryForObject("Subscriber.selectSubscriber", map);
	}

}
