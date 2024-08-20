package com.dianping.cat;

import com.dianping.cat.status.model.entity.Extension;
import com.dianping.cat.status.model.entity.GcInfo;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.13
 */
public class Test {

	public static void main(String[] args) {
		List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();

		for (GarbageCollectorMXBean mxbean : beans) {
			if (mxbean.isValid()) {
				String name = mxbean.getName();
				long collectionCount = mxbean.getCollectionCount();
				long collectionTime = mxbean.getCollectionTime();
				System.out.println("Garbage Collector: " + name);
				System.out.println("  Collection Count: " + collectionCount);
				System.out.println("  Collection Time: " + collectionTime + " ms");
			}
		}

		for (MemoryPoolMXBean mpBean : ManagementFactory.getMemoryPoolMXBeans()) {
			long count = mpBean.getUsage().getUsed();
			String name = mpBean.getName();

			System.out.println(name + ":" + count);
		}
	}
}
