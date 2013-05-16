<%@ page contentType="text/html; charset=utf-8"%>
<div class="well sidebar-nav">
         <ul class="nav nav-list">
           <li class='nav-header'><a href="#"><strong>个人邮件记录</strong></a></li>
	       <li id="alarmRecordList"><a href="?op=alarmRecordList">告警邮件记录</a></li>
	       <li id="reportRecordList"><a href="?op=reportRecordList">报表邮件记录</a></li>
	       <li class='nav-header'><a href="#"><strong>报表告警订阅</strong></a></li>
	       <li id="exceptionAlarmRules"><a href="?op=exceptionAlarmRules">异常告警订阅</a></li>
	       <li id="serviceAlarmRules"><a href="?op=serviceAlarmRules">服务告警订阅</a></li>
	       <li id="scheduledReports"><a href="?op=scheduledReports">日常报表订阅</a></li>
	       <li class='nav-header'><a href="#"><strong>告警模板配置</strong></a></li>
	       <li id="alarmTemplateListexception"><a href="?op=alarmTemplateList&templateName=exception">异常模板配置</a></li>
	       <li id="alarmTemplateListservice"><a href="?op=alarmTemplateList&templateName=service">服务调用配置</a></li>
         </ul>
</div>
<style>
	.nav-list  li  a{
		padding:2px 15px;
	}
	.nav li  +.nav-header{
		margin-top:2px;
	}
	.nav-header{
		padding:5px 3px;
	}
</style>

