package com.dianping.cat.agent.monitor.puppet.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

public class SetReaderPostion {
	private Logger puppetLogger = Logger.getLogger("myLogger"); 

	public void setReaderPostion(String line_file,Long end_position) {
		BufferedWriter output = null ;
		try{
			output = new BufferedWriter(new FileWriter(line_file));
			output.write(Long.toString(end_position));
			puppetLogger.info("写入Succ:"+line_file+" position:"+end_position);
			output.close();

		}catch(IOException e){
			puppetLogger.error("写入文件异:"+line_file);
			puppetLogger.error(e.getMessage(),e);
		}
	}

}
