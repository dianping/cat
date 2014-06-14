package com.dianping.cat.agent.monitor.puppet.util;

import java.io.File;

import org.apache.log4j.Logger;

public class CreatDir {
	private Logger puppetLogger = Logger.getLogger("myLogger"); 

	public  boolean creatDir(String dir){
		File file =new File(dir);
		if(!file .exists()){
			file.mkdirs();
			return true;
		}else{
			return false;
		}
	}

}
