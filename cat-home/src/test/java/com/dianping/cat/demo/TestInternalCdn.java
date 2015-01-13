package com.dianping.cat.demo;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMetric;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;

public class TestInternalCdn {

	@Test
	public void test() {
		List<String> keys = Arrays.asList("DiLian", "TengXun", "WangSu");
		List<String> ips = Arrays.asList("*.*.*.*", "*.*.*.*");
		Random r = new Random();

		while (true) {
			try {
				int random = r.nextInt(100);
				String key = keys.get(random % keys.size()) + ": " + ips.get(random % ips.size());
				Metric metric = Cat.getProducer().newMetric("cdn", key);
				DefaultMetric defaultMetric = (DefaultMetric) metric;

				defaultMetric.setTimestamp(System.currentTimeMillis());
				defaultMetric.setStatus("C");
				defaultMetric.addData(String.valueOf(100));

				MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
				Message message = tree.getMessage();

				if (message instanceof Transaction) {
					((DefaultTransaction) message).setTimestamp(System.currentTimeMillis());
				}
				tree.setDomain("piccenter-display");
				metric.complete();
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
