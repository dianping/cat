<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.matrix.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.matrix.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.matrix.Model" scope="request" />

<a:historyReport title="History Report" navUrlPrefix="domain=${model.domain}&reportType=${payload.reportType}">
	<jsp:attribute name="subtitle">${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
<script type="text/javascript">
	$(document).ready(function() {
		$('#matrix_report').addClass('active');
	});
</script>
<div class="row-fluid">
	<table  class='table table-hover table-bordered table-striped table-condensed ' >
	<tr>
		
		<th class="left" rowspan="2">Type</th>
		<th class="left" width="20%" rowspan="2"><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=Name">Name</a></th>
		<th rowspan="2" title="所有请求中总次数"><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=Count">Total<br/>Hits</a></th>
		<th rowspan="2" title="所有请求中平均响应时间"><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=Time">Avg<br/>Duration(ms)</a></th>
		<th rowspan="2">Log</th>
		<th colspan="3" title="一次请求中远程调用次数统计">Call Ratio</th>
		<th colspan="3" title="一次请求中远程调用时间统计">Call Cost</th>
		<th colspan="3" title="一次请求中数据库调用次数统计">SQL Ratio</th>
		<th colspan="3" title="一次请求中数据库调用时间统计">SQL Cost</th>
		<th colspan="3" title="一次请求中缓存调用次数统计">Cache Ratio</th>
		<th colspan="3" title="一次请求中缓存调用时间统计">Cache Cost</th>
	</tr>
	<tr >
		<td><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=callMinCount">Min</a></td>
		<td><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=callMaxCount">Max</a></td>
		<td><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=callAvgCount">Avg</a></td>
		<td><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=callAvgTotalTime">Time(ms)</a></td>
		<td><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=callTimePercent">Time%</td>
		<td>Log</td>
		
		<td><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=sqlMinCount">Min</a></td>
		<td><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=sqlMaxCount">Max</a></td>
		<td><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=sqlAvgCount">Avg</a></td>
		<td><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=sqlAvgTotalTime">Time(ms)</a></td>
		<td><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=sqlTimePercent">Time%</td>
		<td>Log</td>
		
		<td><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=cacheMinCount">Min</a></td>
		<td><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=cacheMaxCount">Max</a></td>
		<td><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=cacheAvgCount">Avg</a></td>
		<td><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=cacheAvgTotalTime">Time(ms)</a></td>
		<td><a href="?op=history&date=${model.date}&domain=${model.domain}&reportType=${payload.reportType}${model.customDate}&sort=cacheTimePercent">Time%</td>
		<td>Log</td>
	</tr>
	<c:forEach var="item" items="${model.matrix.matrixs}"
				varStatus="status">
				<tr class="  right">
					<td  class="left">${item.type}</td>
					<td  class="left longText" style="white-space:normal">${w:shorten(item.name, 120)}</td>
					<td>${item.count}</td>
					<td>${w:format(item.avg,'0.0')}</td>
					<td><a href="/cat/r/m/${item.url}?domain=${model.domain}">L</a></td>
					<td>${item.callMin}</td>
					<td>${item.callMax}</td>
					<td>${w:format(item.callAvg,'0.0')}</td>
					<td>${item.callTime}</td>
					<td>${w:format(item.callTimePercent,'00.0%')}</td>
					<td><a href="/cat/r/m/${item.callUrl}?domain=${model.domain}">L</a></td>
				
					<td>${item.sqlMin}</td>
					<td>${item.sqlMax}</td>
					<td>${w:format(item.sqlAvg,'0.0')}</td>
					<td>${item.sqlTime}</td>
					<td>${w:format(item.sqlTimePercent,'00.0%')}</td>
					<td><a href="/cat/r/m/${item.sqlUrl}?domain=${model.domain}">L</a></td>
				
					<td>${item.cacheMin}</td>
					<td>${item.cacheMax}</td>
					<td>${w:format(item.cacheAvg,'0.0')}</td>
					<td>${item.cacheTime}</td>
					<td>${w:format(item.cacheTimePercent,'00.0%')}</td>
					<td><a href="/cat/r/m/${item.cacheUrl}?domain=${model.domain}">L</a></td>
				</tr>
			</c:forEach>
</table></div>

</jsp:body>

</a:historyReport>