<%@ page contentType="text/html; charset=utf-8"%>
<div class="well sidebar-nav">
         <ul class="nav nav-list">
	       <li class='nav-header'><h4>CAT日常报表</h4></li>
	       <li id="reportRecordList"><a href="?op=reportRecordList"><strong>报表邮件记录</strong></a></li>
	       <li id="scheduledReports"><a href="?op=scheduledReports"><strong>日常报表订阅</strong></a></li>
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

