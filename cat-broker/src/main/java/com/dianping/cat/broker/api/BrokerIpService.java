package com.dianping.cat.broker.api;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.iphub.IpInfo;
import com.dianping.iphub.service.IpService;

public class BrokerIpService implements Initializable {

	private IpService m_ipService;

	@Inject
	private com.dianping.cat.service.IpService m_innerIpService;

	@SuppressWarnings("resource")
	@Override
	public void initialize() throws InitializationException {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("config/ipservice.xml");

		m_ipService = (IpService) ctx.getBean("ipService");
	}

	public IpInfo findByIp(String ip) {
		try {
			IpInfo ipInfo = m_ipService.getIpInfo(ip);

			return ipInfo;
		} catch (Exception e) {
			Cat.logError(e);
			com.dianping.cat.service.IpService.IpInfo info = m_innerIpService.findIpInfoByString(ip);

			if (info != null) {
				IpInfo ipInfo = new IpInfo();

				ipInfo.setProvinceName(info.getProvince());
				ipInfo.setCarrierName(info.getChannel());
				ipInfo.setCountryName(info.getNation());
				ipInfo.setCityName(info.getCity());
				ipInfo.setSourceProvinceName(info.getProvince());
				ipInfo.setSourceCityName(info.getCity());
				return ipInfo;
			} else {
				return null;
			}
		}
	}
}
