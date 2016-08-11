package com.dianping.cat.configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.configuration.NetworkInterfaceManager.Address;

public class NetworkInterfaceManagerTest {
	private List<Address> buildAllIps(String[] strs) {
		List<Address> result = new ArrayList<Address>();

		try {
			for (String str : strs) {
				result.add(new Address(InetAddress.getByName(str), null));
			}

			return result;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Test
	public void test() {
		String[] str1 = { "10.1.1.1", "192.168.1.1", "127.0.0.1" };
		InetAddress adress = NetworkInterfaceManager.INSTANCE.findValidateIp(buildAllIps(str1));
		Assert.assertEquals("10.1.1.1", adress.getHostAddress());

		String[] str2 = { "10.1.1.1", "127.0.0.1" };
		adress = NetworkInterfaceManager.INSTANCE.findValidateIp(buildAllIps(str2));
		Assert.assertEquals("10.1.1.1", adress.getHostAddress());

		String[] str3 = { "192.168.1.2", "192.168.1.1", "127.0.0.1" };
		adress = NetworkInterfaceManager.INSTANCE.findValidateIp(buildAllIps(str3));
		Assert.assertEquals("192.168.1.2", adress.getHostAddress());

		String[] str4 = { "10.1.1.1", "127.0.0.1", "192.168.1.1" };
		adress = NetworkInterfaceManager.INSTANCE.findValidateIp(buildAllIps(str4));
		Assert.assertEquals("10.1.1.1", adress.getHostAddress());

		String[] str5 = { "10.1.1.1" };
		adress = NetworkInterfaceManager.INSTANCE.findValidateIp(buildAllIps(str5));
		Assert.assertEquals("10.1.1.1", adress.getHostAddress());

		String[] str6 = { "127.0.0.1", "10.128.120.122" };
		adress = NetworkInterfaceManager.INSTANCE.findValidateIp(buildAllIps(str6));
		Assert.assertEquals("10.128.120.122", adress.getHostAddress());
	}

}
