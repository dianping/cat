package com.dianping.cat.agent.puppet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.regex.*;


public class PuppetMonitorByLineNo {
	
	public static void main(String[] args) {
		String log_file="/Users/River/messages";
		String line_file="/var/log/line.log";
    	File line_f = new File(line_file);
		while(true){
	        int line_no_start=get_startline_no(line_file,log_file);
	        int line_reading_count=0;
	        String regEx=".*puppet-agent.*\\(\\/Stage"; 
	        System.out.println("line_no_start="+Integer.toString(line_no_start));
			
			try {
	            FileReader reader = new FileReader(log_file);
	            BufferedReader br = new BufferedReader(reader);
	            
	            String str = null;
	            
	            while((str = br.readLine()) != null) {
	            	line_reading_count++;
	            	if(line_reading_count<line_no_start){
	            		continue;
	            	}else{
	            		Pattern pattern1 = Pattern.compile(".*?\\[(.*?)\\].*?");
	            		if(Pattern.compile(regEx).matcher(str).find()){
	//            			System.out.println(str+"/n");
	                        String[] tmp_list=str.split(" ");
	                        String action_time=tmp_list[0]+tmp_list[1]+"  "+tmp_list[2]+" "+tmp_list[3];
	                        
	                        String hostname=tmp_list[4];
	                        String change=str.split("\\(")[1];
	                        String file=change.split("\\)")[0].split("Stage\\[main\\]")[1];
	                        Matcher matcher1 = pattern1.matcher(file);
	                        if (matcher1.matches()) {
	                        	System.out.println(matcher1.group(1));
	                        }
	               
	            			System.out.println(action_time +"  "+hostname+"   "+change+"/n");
	            		}
	            		else
	            			continue;
	            		
	            	}
	//                  System.out.println(str+"/n");
	            }
	            br.close();
	            reader.close();
//	            System.out.println(countFileLine("/Users/River/messages"));
	            System.out.println("本次读取到文件行数:"+line_reading_count);
	            //将上次读取的行，写入文件
				BufferedWriter output = new BufferedWriter(new FileWriter(line_f));
			    output.write(Integer.toString(line_reading_count));
			    output.close();
	            }catch(FileNotFoundException e) {
	            	e.printStackTrace();
	        }catch(IOException e) {
	              e.printStackTrace();
	        }
			
			//slee 5s
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
  }
	
	/**
	 * 
	 * @param line_file 记录上次读取文件的行数
	 * @param log_file 读取文件，用于分析内容，此处用来统计文件总行数
	 * @return 返回上次读取的文件的行数
	 */
	public static int get_startline_no(String line_file,String log_file){
        int line_no_start = 0;
        String str="";
		try{
            FileReader line_reader = new FileReader(line_file);
            BufferedReader line_br = new BufferedReader(line_reader);

            try {
				if(	(str=line_br.readLine())!= null){
					line_no_start=Integer.parseInt(str);
					line_br.close();
				    line_reader.close();
				}else{//file is empty
					line_no_start=countFileLine(log_file)/2;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}catch(FileNotFoundException e){
        	line_no_start=countFileLine(log_file)/2;
        	File f = new File(line_file);
        	try {
				if(f.createNewFile()){
					BufferedWriter output = new BufferedWriter(new FileWriter(f));
				       output.write(Integer.toString(line_no_start)+"\n");
				       output.close();
				}else{
					System.out.println("fail to create file : "+line_file);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}		
		}
		System.out.println("上次读取的文件行数"+Integer.toString(line_no_start));
		return line_no_start;
	}
	/**
	 * 
	 * @param file 文件
	 * @return 文件的总行数
	 */
	public static int countFileLine(String file){
		int count=0;
		try{
			InputStream is = new BufferedInputStream(new FileInputStream(file));
			byte[] c = new byte[65536];
			int readChars = 0;
	        while ((readChars = is.read(c)) != -1) {
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n')
	                    ++count;
	            }
	        }
	        is.close();
	        
		}catch(IOException e){
			e.printStackTrace();	
		}
		System.out.println("文件总行数"+Integer.toString(count));
		return count;
	}

}
