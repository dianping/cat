package com.dianping.cat.notify.server.processor;

import com.dianping.cat.notify.job.HandworkJob;
import com.dianping.cat.notify.job.JobContext;
import com.dianping.cat.notify.server.ContainerHolder;
import com.dianping.cat.notify.server.Processor;
import com.dianping.cat.notify.server.Request;
import com.dianping.cat.notify.server.Response;
import com.dianping.cat.notify.util.TimeUtil;

public class SendMailProccessor implements Processor {
	
	private HandworkJob m_sendMailJob;

	//TODO
	//http://localhost:8080?action=sendMail&domain=cat&day=2012-07-01
	@Override
	public void process(Request req, Response rep) {
		String domain = req.getParameter("domain");
		String day = req.getParameter("day");
		rep.setTemplate("handworkResult.vm");
		if(null == domain || domain.toString().length() == 0){
			rep.assign("result", "please input [domain]");
			return;
		} 
		if(null == day){
			rep.assign("result", "please input [day]");
			return;
		} 
		long timeSpan = TimeUtil.getTimeFromString(day);
      if(timeSpan == -1){
      	rep.assign("result", String.format("invalid day[%s]. Format:[%s]",day,"yyyy-MM-dd"));
			return;
      }
      JobContext context = new JobContext();
      context.addData("domain", domain);
      context.addData("day", timeSpan);
      
      if(m_sendMailJob.doHandwork(context)){
      	rep.assign("result", String.format("sendmail success! domain[%s]  day[%s]",domain,day));
      }else{
      	rep.assign("result", String.format("fail to sendmail! domain[%s]  day[%s]",domain,day));
      }
	}

	@Override
   public void init(ContainerHolder holder) {
		m_sendMailJob = holder.lookup(HandworkJob.class, "sendMailJob");
   }

}
