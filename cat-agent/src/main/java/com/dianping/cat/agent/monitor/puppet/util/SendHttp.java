package com.dianping.cat.agent.monitor.puppet.util;

import com.dianping.cat.agent.monitor.puppet.Alertation;

//import org.apache.log4j.Logger;

public class SendHttp {
//	private Logger puppetLogger = Logger.getLogger("myLogger"); 
	
	public void sendHttp(Alertation alertation) {
		HttpPostUtils httppost=new HttpPostUtils();
//		StringBuffer pars_str=new StringBuffer();
		String[] catip={"10.1.110.23","10.1.6.102","10.1.6.128 "};
		
		String[] pars= new String[11];
		
		pars[0]="type="+alertation.getType();
		pars[1]="title="+alertation.getTitle();
		pars[2]="domain="+alertation.getDomain();
		pars[3]="ip="+alertation.getIp();
		pars[4]="user="+alertation.getUser();
		pars[5]="content="+alertation.getContent();
		pars[6]="url="+alertation.getUrl();
		pars[7]="op="+alertation.getOp();
		pars[8]="alterationDate="+alertation.getDate();
		pars[9]="hostname="+alertation.getHostname();
		pars[10]="group="+alertation.getGroup();
//		for(int i=0;i<pars.length;i++){
//			pars_str.append(pars[i]);
//			pars_str.append(";");
//		}
//		puppetLogger.info(pars_str);

		for(String ip:catip){
			String url="http://"+ip+":8080/cat/r/alteration";
			httppost.setUrlAddress(url);
			if(httppost.httpPost(pars)){
				break;
			}else{
				continue;
			}
		}

	}

}
