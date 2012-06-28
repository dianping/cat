package com.dianping.cat;

public class Test {
	
	private static String emptyHourDate(){
		StringBuilder sb=new StringBuilder();
		for (int i = 0; i < 60; i++) {
	      sb.append("0,");
      }
		return sb.substring(0, sb.length()-1);
	}
	
	public static void main(String[] args) {
		String s=emptyHourDate();
		int ss=s.split(",").length;
	   System.out.println(ss);
   }

}
