<%@ page contentType="text/html; charset=utf-8"%>
<br/>
<div class="well sidebar-nav" >
    <ul class="nav nav-list">
      <li class="nav-header"><h5>监控大盘</h5></li>
		<li><a  href="/cat/r/dependency?op=metricDashboard&domain=${model.domain}">系统报错大盘</a></li>
		<li><a  href="/cat/r/metric?op=dashboard&domain=${model.domain}">业务监控大盘</a></li>
		<li><a  href="/cat/r/network?op=dashboard&domain=${model.domain}">网络监控大盘</a></li>
		<li><a  href="/cat/r/dependency?op=dashboard&domain=${model.domain}">应用监控大盘</a></li>
		<li class="nav-header"><h5>监控报表</h5></li>
		<li><a  href="/cat/r/cdn?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">CDN监控</a></li>
		<li><a  href="/cat/r/network?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">网络监控</a></li>
		<li><a  href="/cat/r/database?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">数据库监控</a></li>
		<li><a  href="/cat/r/system?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">PAAS系统监控</a></li>
		<li><a  href="/cat/r/alteration?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">线上变更监控</a></li>
		<li><a  href="/cat/r/alert?domain=${model.domain}&op=${payload.action.name}">告警信息查询</a></li>
		<li class='nav-header'><h5>离线报表</h5></li>
		<li id="matrix"><a  href="/cat/r/matrix?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">项目资源消耗</a></li>
		<li id="highload"><a  href="/cat/r/highload?&op=${payload.action.name}">全局资源消耗</a></li>
		<li id="overload"><a  href="/cat/r/overload?domain=${model.domain}&op=${payload.action.name}">报表容量统计</a></li>
		<li id="bug"><a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=view">全局统计异常</a></li>
		<li id="alert"><a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=alert">异常告警排行</a></li>
		<li id="service"><a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=service">服务可用性排行</a></li>
		<li id="utilization"><a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=utilization">线上容量规划</a></li>
		<li id="jar"><a href="/cat/r/statistics?op=jar&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=utilization">线上JAR版本</a></li>
		<li id="heavy"><a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=heavy">重量级访问排行</a></li>
		<li id="summary"><a href="/cat/r/statistics?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=summary">告警智能分析</a></li>
		<li class="nav-header"><h5>订阅报表</h5></li>
		<li id="reportRecordList"><a href="/cat/s/alarm?op=reportRecordList">报表邮件记录</a></li>
		<li id="scheduledReports"><a href="/cat/s/alarm?op=scheduledReports">日常报表订阅</a></li>
   </ul>
</div>

