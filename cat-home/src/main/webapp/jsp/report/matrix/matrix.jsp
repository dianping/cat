<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.matrix.Context"	scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.matrix.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.matrix.Model"	scope="request" />

<a:hourly_report title="Matrix Report" navUrlPrefix="domain=${model.domain}">
	<jsp:attribute name="subtitle">${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>	
<script type="text/javascript">
	$(document).ready(function() {
		$('#matrix_report').addClass('active');
	});
</script>
<div class="row-fluid">
	<table class='table table-hover table-bordered table-striped table-condensed' style="width: auto;">
	<tr>
		<th class="left" width="5%" rowspan="2">Type</th>
		<th class="left" width="10%" rowspan="2"><a href="?date=${model.date}&domain=${model.domain}&sort=Name">接口路径</a></th>
		<th rowspan="2" width="5%" title="所有请求中总次数"><a href="?date=${model.date}&domain=${model.domain}&sort=Count">
			总量</a></th>
		<th rowspan="2" title="所有请求中平均响应时间"><a href="?date=${model.date}&domain=${model.domain}&sort=Time">平均耗时(ms)</a></th>
		<th rowspan="2" width="3%">样例</th>
		<th colspan="3" title="一次请求中远程调用次数统计">RPC调用比率</th>
		<th colspan="3" title="一次请求中远程调用时间统计">RPC调用开销</th>
		<th colspan="3" title="一次请求中数据库调用次数统计">SQL调用比率</th>
		<th colspan="3" title="一次请求中数据库调用时间统计">SQL调用开销</th>
		<th colspan="3" title="一次请求中缓存调用次数统计">Cache调用比率</th>
		<th colspan="3" title="一次请求中缓存调用时间统计">Cache调用开销</th>
	</tr>
	<tr >
		<td><a href="?date=${model.date}&domain=${model.domain}&sort=callMinCount">最小耗时</a></td>
		<td><a href="?date=${model.date}&domain=${model.domain}&sort=callMaxCount">最大耗时</a></td>
		<td><a href="?date=${model.date}&domain=${model.domain}&sort=callAvgCount">平均耗时</a></td>
		<td><a href="?date=${model.date}&domain=${model.domain}&sort=callAvgTotalTime">Time(ms)</a></td>
		<td><a href="?date=${model.date}&domain=${model.domain}&sort=callTimePercent">Time%</td>
		<td>样例</td>
		
		<td><a href="?date=${model.date}&domain=${model.domain}&sort=sqlMinCount">最小耗时</a></td>
		<td><a href="?date=${model.date}&domain=${model.domain}&sort=sqlMaxCount">最大耗时</a></td>
		<td><a href="?date=${model.date}&domain=${model.domain}&sort=sqlAvgCount">平均耗时</a></td>
		<td><a href="?date=${model.date}&domain=${model.domain}&sort=sqlAvgTotalTime">Time(ms)</a></td>
		<td><a href="?date=${model.date}&domain=${model.domain}&sort=sqlTimePercent">Time%</td>
		<td>样例</td>
		
		<td><a href="?date=${model.date}&domain=${model.domain}&sort=cacheMinCount">最小耗时</a></td>
		<td><a href="?date=${model.date}&domain=${model.domain}&sort=cacheMaxCount">最大耗时</a></td>
		<td><a href="?date=${model.date}&domain=${model.domain}&sort=cacheAvgCount">平均耗时</a></td>
		<td><a href="?date=${model.date}&domain=${model.domain}&sort=cacheAvgTotalTime">Time(ms)</a></td>
		<td><a href="?date=${model.date}&domain=${model.domain}&sort=cacheTimePercent">Time%</td>
		<td>样例</td>
	</tr>
	<c:forEach var="item" items="${model.matrix.matrixs}" varStatus="status">
		<tr class=" right">
			<td class="left">${item.type}</td>
			<td class="left longText" style="white-space:normal">${w:shorten(item.name, 120)}</td>
			<td>${w:format(item.count,'#,###,##0')}</td>
			<td>${w:format(item.avg,'0.0')}</td>
			<td class="center"><a href="/cat/r/m/${item.url}?domain=${model.domain}">L</a></td>
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
</a:hourly_report>

