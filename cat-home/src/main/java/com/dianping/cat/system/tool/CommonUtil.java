package com.dianping.cat.system.tool;

import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.transform.DefaultDomParser;
import com.site.helper.Files;

public class CommonUtil {

	private static final String DEFAULT = "UNKOWN";

	private static String DOMAIN = DEFAULT;

	private static String IP;

	private static final String CAT_CLIENT_XML = "/META-INF/cat/client.xml";

	// public static void main(String args[]) {
	// System.out.println(CommonUtil.getDomain());
	// System.out.println(CommonUtil.getIp());
	// }

	static {
		try {
			ClientConfig clientConfig = null;
			InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CAT_CLIENT_XML);

			if (in == null) {
				in = CommonUtil.class.getResourceAsStream(CAT_CLIENT_XML);
			}

			if (in != null) {
				String xml = Files.forIO().readFrom(in, "utf-8");

				clientConfig = new DefaultDomParser().parse(xml);
			}

			if (clientConfig != null) {
				Map<String, Domain> domains = clientConfig.getDomains();
				Domain firstDomain = domains.isEmpty() ? null : domains.values().iterator().next();
				DOMAIN = firstDomain.getId();
			}
		} catch (Exception e) {
		}

		try {
			List<NetworkInterface> nis = Collections.list(NetworkInterface.getNetworkInterfaces());
			InetAddress local = null;

			for (NetworkInterface ni : nis) {
				if (ni.isUp()) {
					List<InetAddress> addresses = Collections.list(ni.getInetAddresses());

					for (InetAddress address : addresses) {
						if (address instanceof Inet4Address) {
							if (address.isLoopbackAddress() || address.isSiteLocalAddress()) {
								if (local == null) {
									local = address;
								} else if (local.isLoopbackAddress() && address.isSiteLocalAddress()) {
									// site local address has higher priority than
									// loopback address
									local = address;
								} else if (local.isSiteLocalAddress() && address.isSiteLocalAddress()) {
									// site local address with a host name has higher
									// priority than one without host name
									if (local.getHostName().equals(local.getHostAddress())
									      && !address.getHostName().equals(address.getHostAddress())) {
										local = address;
									}
								}
							}
						}
					}
				}
			}
			IP = local.getHostAddress();
		} catch (Exception e) {
		}
	}

	public static final String getDomain() {
		return DOMAIN;
	}

	public static final String getIp() {
		return IP;
	}
}
