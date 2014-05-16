package com.dianping.cat.broker;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.broker.api.page.IpService;
import com.dianping.cat.broker.api.page.IpService.IpInfo;

public class IpServiceTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		IpService service = (IpService) lookup(IpService.class);

		for (int i = 0; i < 1000; i++) {
			String ip = i % 255 + "." + i % 255 + "." + i % 255 + "." + i % 255;
			IpInfo info = service.findIpInfoByString(ip);
			
			if(info!=null){
				System.out.println(info.getChannel());
				System.out.println(info.getCity());
				System.out.println(info.getProvince());
			}else{
				System.err.println("====");
			}
		}
	}

}
