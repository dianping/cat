<%@ page contentType="text/html; charset=utf-8"%>
<br/>
<div class="well sidebar-nav" >
    <ul class="nav nav-list">
      <li class='nav-header text-center'><h4>报表中心</h4></li>
      <li id="bug" class="text-right"><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=view"><strong>分BU统计异常</strong></a></li>
      <li id="alert" class="text-right"><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=alert"><strong>异常告警排行榜</strong></a></li>
  	  <li id="utilization" class="text-right"><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=utilization"><strong>线上容量规划</strong></a></li>
  	  <li id="service" class="text-right"><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=service"><strong>服务可用性排行</strong></a></li>
  	  <li id="heavy" class="text-right" ><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=heavy"><strong>重量级访问排行</strong></a></li>
      <li id="summary" class="text-right" ><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=summary"><strong>告警智能分析</strong></a></li>
  	  <li id="reportRecordList" class="text-right" ><a href="/cat/s/alarm?op=reportRecordList"><strong>报表邮件记录</strong></a></li>
	  <li id="scheduledReports" class="text-right" ><a href="/cat/s/alarm?op=scheduledReports"><strong>日常报表订阅</strong></a></li>
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
	.row-fluid .span2{
		width:12%;
	}
</style>

