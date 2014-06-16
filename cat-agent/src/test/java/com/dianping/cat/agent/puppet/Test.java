package com.dianping.cat.agent.puppet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class Test {
	public static void main(String args[]){
		String m_logFile = "/var/log/messages";
		RandomAccessFile reader=null;
		try {
			reader = new RandomAccessFile(m_logFile, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			System.out.println(reader.length());
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
}