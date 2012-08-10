package com.dianping.cat.report.project;

public class ProjectInfoCache {

	private static ProjectInfoCache PROJECT = new ProjectInfoCache();

	private ProjectInfoCache() {
	}

	public static synchronized ProjectInfoCache getIntance() {
		return PROJECT;
	}

	public String getProjectNameFromIp(String ip) {
		return "Project"+ip.substring(0, 5);
	}
	
	public boolean isInTheProject(String ip,String projectName){
		return true;
	}
}
