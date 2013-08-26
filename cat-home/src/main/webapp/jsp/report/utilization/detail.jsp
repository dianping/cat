	<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<div class="row-fluid">
      <div class="span2">
		<%@include file="../bugTree.jsp"%>
	</div>
	<div class="span10">
		<div class="report">
			<table class="table table-striped table-bordered table-condensed">
				<tr>
					<th class="left">id</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization">Machine Number</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=urlCount">URL Count</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=urlResponse">URL Response Time</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=serviceCount">Service Count</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=serviceResponse">Service Response Time</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=serviceCount">Service Count</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=serviceResponse">Service Response Time</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=sqlCount">SQL Count</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=pigeonCallCount">Pigeon Call Count</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=swallowCallCount">Swallow Call Count</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=memcacheCount">Memcache Count</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=score">Score</th>
				</tr>
			
				<c:forEach var="item" items="${model.utilizationList}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td>${item.id}</td>
						<td style="text-align:right">${item.machineNumber}</td>
						<td style="text-align:right">${w:format(item.urlCount,'#,###,###,###,##0')}</td>
						<td style="text-align:right">${w:format(item.urlResponseTime,'0.0')}</td>
						<td style="text-align:right">${w:format(item.serviceCount,'#,###,###,###,##0')}</td>
						<td style="text-align:right">${w:format(item.serviceResponseTime,'0.0')}</td>
						<td style="text-align:right">${w:format(item.serviceCount,'#,###,###,###,##0')}</td>
						<td style="text-align:right">${w:format(item.serviceResponseTime,'0.0')}</td>
						<td style="text-align:right">${w:format(item.sqlCount,'#,###,###,###,##0')}</td>
						<td style="text-align:right">${w:format(item.pigeonCallCount,'#,###,###,###,##0')}</td>
						<td style="text-align:right">${w:format(item.swallowCallCount,'#,###,###,###,##0')}</td>
						<td style="text-align:right">${w:format(item.memcacheCount,'#,###,###,###,##0')}</td>
						<td style="text-align:right">${w:format(item.score,'#,###,###,###,##0')}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
</div>