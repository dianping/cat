package com.dianping.cat.agent.monitor.puppet.util;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import com.dianping.cat.agent.monitor.puppet.Alertation;


public class Parse {
	private Logger puppetLogger = Logger.getLogger("myLogger"); 

	public Alertation parse(String line) {
		RunSysCmd runsyscmd=new RunSysCmd();
		String title_add="";
		String group="puppet";
		String type="puppet";
		String user="puppet";
		String url="";
		String op="insert";
		String host="";
		String IP="";
		String date="";
		String domain="";
		String content="";
		String title="puppet";  
		String regEx = ".*puppet-agent.*\\(\\/Stage";
		String regEx_time = ".*[0-9]{2}:[0-9]{2}:[0-9]{2}.*";

		String regEx_Filebucketed=".*Filebucketed.*";
		String tmp="";
		Alertation alertation =new Alertation();
		if (Pattern.compile(regEx).matcher(line).find()) {
			String[] tmp_list = line.split(" ");
			Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			InetAddress ia=null;
			if(tmp_list.length>=4){
				if(Pattern.compile(regEx_time).matcher(tmp_list[3]).find()){
					date = tmp_list[0] +" " + tmp_list[2] + " "+tmp_list[3] + " " +Integer.toString(year);
				}else{
					date = tmp_list[0] +" " + tmp_list[1]+ " " + tmp_list[2] + " " +Integer.toString(year);
				}
			}
			String all_content=line.split("\\(")[1];
			String[] tmp_string=all_content.split("\\)");
			String[] tmp_string_main=tmp_string[0].split("\\[main\\]\\/");
			if (tmp_string_main.length >=2){
				title=tmp_string_main[1].split("\\[")[1].split("\\]")[0];
				if(title==""){
					title="puppet";
				}
			}
			if(tmp_string.length>=2){
				content=all_content.split("\\)")[1];
				if(content.split(" ").length>=3){
					title_add=content.split(" ")[1]+" "+content.split(" ")[2];
					title=title+" "+title_add;
				}
			}
			if(Pattern.compile(regEx_Filebucketed).matcher(content).find()){
				String new_file=content.split(" ")[2];
				if(content.split(" ").length >=8){
					String old_file_index=content.split(" ")[7];
					//==========test======
//					String old_file_dir=runsyscmd.runSysCmd("/usr/bin/find /var/lib/puppet/clientbucket -name "+old_file_index).toString().split("\n")[0];
					String old_file_dir=runsyscmd.runSysCmd("find /var/lib/puppet/clientbucket -name "+old_file_index).toString().split("\n")[0];
					String old_file=old_file_dir+"/contents";	    			
					if(new File(new_file).exists() && new File(old_file).exists() ){
		    			puppetLogger.info("diff content ing "+" "+old_file+" "+new_file );
						tmp=runsyscmd.runSysCmd("diff "+old_file+" "+ new_file).toString();
					}
					if(tmp.trim()!=""){
						content=tmp;
					}else{
		    			puppetLogger.warn("diff return content is empty");
					}
				}
			}
			SimpleDateFormat sdf_mmm = new SimpleDateFormat("MMMM dd HH:mm:ss yyyy",Locale.US);
			SimpleDateFormat sdf_normal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);			
			try {
				date=sdf_normal.format(sdf_mmm.parse(date));
				ia = InetAddress.getLocalHost(); 
			} catch (ParseException e1) {
				puppetLogger.error(e1.getMessage(),e1);
				sdf_mmm = new SimpleDateFormat("MMMM  dd HH:mm:ss yyyy",Locale.US);
				try {
					date=sdf_normal.format(sdf_mmm.parse(date));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return null;
			}catch(UnknownHostException e){  
				alertation.setHostname("Unknow_hostname");
				alertation.setDomain("Unknow_domain");
                e.printStackTrace();  
			}
			if(ia!=null){
				host = ia.getHostName();
				IP= ia.getHostAddress();
				domain=host.split("[0-9]")[0].split("-sl-|-gp-|-ppe")[0];
			}
			alertation.setHostname(host);
			alertation.setIp(IP);
			alertation.setDomain(domain);
			alertation.setTitle(title);
			alertation.setContent(content);
			alertation.setOp(op);
			alertation.setUrl(url);
			alertation.setUser(user);
			alertation.setType(type);
			alertation.setDate(date);
			alertation.setGroup(group);
		}
		else{
			alertation=null;
		}
		return alertation;
	}

}
