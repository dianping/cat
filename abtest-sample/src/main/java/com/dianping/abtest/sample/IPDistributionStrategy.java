package com.dianping.abtest.sample;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.unidal.helper.Splitters;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.abtest.model.entity.GroupstrategyDescriptor;
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
		GroupstrategyDescriptor config = entity.getGroupStrategyConfiguration();
		List<String> ips = Splitters.by(',').trim().noEmptyItem().split(config.getFields().get(0).getValue());
		HttpServletRequest req = ctx.getHttpServletRequest();
		String address = getRemoteAddr(req);
		String group = ctx.getCookielet("ab");
		
		if(group != null && group.equals("A")){
			ctx.setGroupName("A");
		}else{
			for (String ip : ips) {
				if (ip.equals(address)) {
					ctx.setGroupName("A");
					ctx.setCookielet("ab", "A");
					ctx.setCookielet("hit", "1");
					return;
				}
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
			if(ip.equals("127.0.0.1") || ip.startsWith("0:0:0:0:0:0:0:1")){
				ip = IPUtils.getFirstNoLoopbackIP4Address();
			}
		}

		return ip;
	}

	@Override
   public void init(ABTestEntity entity) {
   }
}
