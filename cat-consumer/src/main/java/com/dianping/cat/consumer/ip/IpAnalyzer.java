package com.dianping.cat.consumer.ip;

import java.util.List;

import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;

public class IpAnalyzer extends AbstractMessageAnalyzer<IpReport> {

	@Override
   protected void store(List<IpReport> result) {
	   // TODO Auto-generated method stub
	   
   }

	@Override
   public List<IpReport> generate() {
	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   public IpReport generate(String domain) {
	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   protected void process(MessageTree tree) {
	   // TODO Auto-generated method stub
	   
   }

	@Override
   protected boolean isTimeout() {
	   // TODO Auto-generated method stub
	   return false;
   }
	
}
