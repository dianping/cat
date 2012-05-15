package com.dianping.cat.report.page.sql;


import java.util.Arrays;

import org.junit.Test;

public class TestComputeStr {

	Handler handler = new Handler();
	@Test
	public void test() {
		String s1="0:1,5:1,10:1,15:1,20:1,25:1,30:1,35:1,40:1,45:1,50:1,55:1,60:1";
		//String s2="0:1,5:1,10:1,15:1,20:2,25:2,30:2,35:2,40:3,45:3,50:4,55:4,60:4";
		String s7="0:1,5:1,10:1,15:1,20:2,25:2,31:2,32:2,33:3,34:3,36:4,37:4,38:4";
		String s3="0:0,1:0,2:0,4:3,8:2,16:0,32:0,64:0,128:0,256:0,512:0,1024:0,2048:0,4096:0,8192:0,16384:0,32768:0,65536:0";
		//String s4="0:0.0,5:0.0,10:0.0,15:0.0,20:0.0,25:0.0,30:0.0,35:0.0,40:0.0,45:0.0,50:0.0,55:2.0,60:0.0";
		String t1=handler.compute(Arrays.asList(s1,new String(s1),new String(s1)));
		String t2=handler.compute(Arrays.asList(s1,new String(s1),new String(s1)));
		String t3=handler.computeDuration(Arrays.asList(s3),Arrays.asList(s3));
		String t4=handler.compute(Arrays.asList(s1,s7));
		System.out.println(t1);
		System.out.println(t2);
		System.out.println(t3);
		System.out.println(t4);
	}
}
