package com.dianping.cat.agent.monitor.puppet.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class RunSysCmd {
	private Logger puppetLogger = Logger.getLogger("myLogger"); 
	
	public StringBuffer runSysCmd(String cmd){
//		puppetLogger.info("执行命令ing:"+cmd);
		String regEx = "^chown.*|^diff.*|^find.*";

		StringBuffer result = new StringBuffer();
		Runtime run = Runtime.getRuntime();
		Process p=null;
		if(!Pattern.compile(regEx).matcher(cmd).find()){
			System.out.println("不支持改命令"+cmd);
			return null;
		}
	        try { 
	            p = run.exec(cmd); 
	            BufferedInputStream in = new BufferedInputStream(p.getInputStream());  
	            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));  
	            String lineStr;  
	            while ((lineStr = inBr.readLine()) != null){ 
	            	result.append(lineStr+"\n");
	            } 
	            if (p.waitFor() != 0) {  
	                if (p.exitValue() != 0)//p.exitValue()==0表示正常结束，1：非正常结束 
	        			puppetLogger.warn("命令执行失败?: "+cmd);
	            }  
	            inBr.close();  
	            in.close();  
	        } catch (Exception e) {  
				puppetLogger.error(e.getMessage(),e);
	        }finally
	        {
	            if ( p != null)
	            {
	                try {
		                p.getOutputStream().close();
		                p.getInputStream().close();
						p.getErrorStream().close();
					} catch (IOException e) {
						puppetLogger.error(e.getMessage(),e);
					}
	            }
	        }  
			return result;

		
	}
}
