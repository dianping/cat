package com.dianping.cat.system.tool;

import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

public class AppConfig {

	private static String s_domain = "UNKOWN";

	private static String s_ip;

	private static final String CAT_CLIENT_XML = "/META-INF/cat/client.xml";

	static {
		try {
			s_domain = readDomain();

			InetAddress local = readIp();
			s_ip = local.getHostAddress();
		} catch (Exception e) {
		}
	}

	public static final String getDomain() {
		return s_domain;
	}

	public static final String getIp() {
		return s_ip;
	}

	public static void main(String args[]) {
		System.out.println(AppConfig.getDomain());
		System.out.println(AppConfig.getIp());
	}

	private static String readDomain() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CAT_CLIENT_XML);

		if (in == null) {
			in = AppConfig.class.getResourceAsStream(CAT_CLIENT_XML);
		}

		Document doc = builder.parse(in);
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile("//config/domain/@id");
		Object result = expr.evaluate(doc, XPathConstants.STRING);
		
		return (String) result;
	}

	private static InetAddress readIp() throws SocketException {
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
								local = address;
							} else if (local.isSiteLocalAddress() && address.isSiteLocalAddress()) {
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
		return local;
	}
}
