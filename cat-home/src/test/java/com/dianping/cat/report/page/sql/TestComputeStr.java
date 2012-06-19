package com.dianping.cat.report.page.sql;


import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

public class TestComputeStr {

	Handler handler = new Handler();
	@Test
	public void test() {
		String s1="0:1,5:1,10:1,15:1,20:1,25:1,30:1,35:1,40:1,45:1,50:1,55:1,60:1";
		String s7="0:1,5:1,10:1,15:1,20:2,25:2,31:2,32:2,33:3,34:3,36:4,37:4,38:4";
		String s3="0:0,1:0,2:0,4:3,8:2,16:0,32:0,64:0,128:0,256:0,512:0,1024:0,2048:0,4096:0,8192:0,16384:0,32768:0,65536:0";
		String t1=handler.compute(Arrays.asList(s1,new String(s1),new String(s1)));
		String t2=handler.compute(Arrays.asList(s1,new String(s1),new String(s1)));
		String t3=handler.computeDuration(Arrays.asList(s3),Arrays.asList(s3));
		String t4=handler.compute(Arrays.asList(s1,s7));

		Assert.assertEquals("0:3,5:3,10:3,15:3,20:3,25:3,30:3,35:3,40:3,45:3,50:3,55:3,60:3",t1);
		Assert.assertEquals("0:3,5:3,10:3,15:3,20:3,25:3,30:3,35:3,40:3,45:3,50:3,55:3,60:3",t2);
		Assert.assertEquals("0:0.0,5:0.0,10:0.0,15:3.0,20:2.0,25:0.0,30:0.0,35:0.0,40:0.0,45:0.0,50:0.0,55:0.0,60:0.0",t3);
		Assert.assertEquals("0:2,5:2,10:2,15:2,20:3,25:3,30:1,31:2,32:2,33:3,34:3,35:1,36:4,37:4,38:4,40:1,45:1,50:1,55:1,60:1",t4);
	}
}
