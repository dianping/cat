package com.dianping.abtest.sample;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.unidal.helper.Splitters;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;

public class IPDistributionStrategy implements ABTestGroupStrategy {
	
	public static final String ID = "ip-distribution";

	public IPDistributionStrategy(){
		System.out.println("new " + ID + " created");
	}
	
	@Override
	public void apply(ABTestContext ctx) {
		ABTestEntity entity = ctx.getEntity();
		String config = entity.getGroupStrategyConfiguration();
		List<String> ips = Splitters.by(',').trim().split(config);
		HttpServletRequest req = ctx.getHttpServletRequest();
		String address = getRemoteAddr(req);

		for (String ip : ips) {
			if (ip.equals(address)) {
				ctx.setGroupName("A");
				return;
			}
		}
	}

	public String getRemoteAddr(HttpServletRequest req) {
		String ip = req.getHeader("X-Forwarded-For");

		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("Proxy-Client-IP");
		}
		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("WL-Proxy-Client-IP");
		}
		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("HTTP_CLIENT_IP");
		}
		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getRemoteAddr();
			if(ip.equals("127.0.0.1") && ip.startsWith("0:0:0:0:0:0:0:1")){
				ip = IPUtils.getFirstNoLoopbackIP4Address();
			}
		}

		return ip;
	}
}
