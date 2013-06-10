<%@ page contentType="text/html; charset=utf-8"%>
<div class="well sidebar-nav">
         <ul class="nav nav-list">
           <li class='nav-header'><h4>个人邮件记录</h4></li>
	       <li id="alarmRecordList"><a href="?op=alarmRecordList"><strong>告警邮件记录</strong></a></li>
	       <li id="reportRecordList"><a href="?op=reportRecordList"><strong>报表邮件记录</strong></a></li>
	       <li class='nav-header'><h4>报表告警订阅</h4></li>
	       <li id="exceptionAlarmRules"><a href="?op=exceptionAlarmRules"><strong>异常告警订阅</strong></a></li>
	       <li id="serviceAlarmRules"><a href="?op=serviceAlarmRules"><strong>服务告警订阅</strong></a></li>
	       <li id="scheduledReports"><a href="?op=scheduledReports"><strong>日常报表订阅</strong></a></li>
	       <li class='nav-header'><h4>告警模板配置</h4></li>
	       <li id="alarmTemplateListexception"><a href="?op=alarmTemplateList&templateName=exception"><strong>异常模板配置</strong></a></li>
	       <li id="alarmTemplateListservice"><a href="?op=alarmTemplateList&templateName=service"><strong>服务调用配置</strong></a></li>
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

