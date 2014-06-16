package com.dianping.cat.agent.monitor.puppet.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;


public class GetReaderPostion {
	private Logger puppetLogger = Logger.getLogger("myLogger"); 
	
	/**
	 * 
	 * @param line_file,记录文件读取位置的文件
	 * @return 记录的数据，否则返回0 
	 * 读取文件失败的时候是否创建文件line_file
	 * 
	 */
	public long getReaderPostion(String line_file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(line_file));
			String str = reader.readLine();
			if (str != null) {
				return Long.parseLong(str);
			}else{
				return 0L;
			}
		}catch(FileNotFoundException e1){
			File filename = new File(line_file);
			try {
				filename.createNewFile();
			} catch (IOException e2) {
				puppetLogger.error("创建文件失败:" + line_file);
				puppetLogger.error(e2.getMessage(),e2);
			}	
		} catch (Exception e3) {
			puppetLogger.error(e3.getMessage(),e3);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					puppetLogger.error(e.getMessage(),e);
				}
			}

		}
		return 0L;
	}
}
