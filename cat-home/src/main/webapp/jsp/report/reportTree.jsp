<%@ page contentType="text/html; charset=utf-8"%>
<br/>
<div class="well sidebar-nav" >
    <ul class="nav nav-list">
      <li class='nav-header text-center'><h4>报表中心</h4></li>
	 	<li id="overload"><a  href="/cat/r/overload?domain=${model.domain}&op=${payload.action.name}">数据库容量报表</a></li>
		<li id="matrix"><a  href="/cat/r/matrix?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">资源消耗统计</a></li>
		<li id="bug"><a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=view">分BU统计异常</a></li>
	    <li id="alert"><a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=alert">异常告警排行榜</a></li>
	  	<li id="utilization"><a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=utilization">线上容量规划</a></li>
	  	<li id="service"><a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=service">服务可用性排行</a></li>
	  	<li id="heavy"><a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=heavy">重量级访问排行</a></li>
	    <li id="summary"><a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=summary">告警智能分析</a></li>
	    <li id="reportRecordList"><a href="/cat/s/alarm?op=reportRecordList">报表邮件记录</a></li>
	 	<li id="scheduledReports"><a href="/cat/s/alarm?op=scheduledReports">日常报表订阅</a></li>
	  
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

