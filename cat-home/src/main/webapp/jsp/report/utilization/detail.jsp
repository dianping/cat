	<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<div class="row-fluid">
      <div class="span2">
		<%@include file="../bugTree.jsp"%>
	</div>
	<div class="span10">
		<div class="tabbable "  > <!-- Only required for left/right tabs -->
			<ul class="nav nav-tabs alert-info">
			 	<li class="text-right "><a id="tab1Href" href="#tab1" data-toggle="tab"><strong>Web</strong></a></li>
			 	<li class="text-right "><a id="tab2Href" href="#tab2" data-toggle="tab"><strong>Service</strong></a></li>
			</ul>
			<div class="tab-content">
				<div class="tab-pane active" id="tab1">
					<div class="report">
						<table class="table table-striped table-bordered table-condensed">
							<tr>
								<th class="left">id</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&tab=tab1">Machine Number</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=urlCount&tab=tab1">URL Count</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=urlResponse&tab=tab1">URL Response Time</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=serviceCount&tab=tab1">Service Count</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=serviceResponse&tab=tab1">Service Response Time</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=sqlCount&tab=tab1">SQL Count</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=pigeonCallCount&tab=tab1">Pigeon Call Count</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=swallowCallCount&tab=tab1">Swallow Call Count</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=memcacheCount&tab=tab1">Memcache Count</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=webScore&tab=tab1">Web Score</th>
							</tr>
						
							<c:forEach var="item" items="${model.utilizationWebList}" varStatus="status">
								<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
									<td>${item.id}</td>
									<td style="text-align:right">${item.machineNumber}</td>
									<td style="text-align:right">${w:format(item.urlCount,'#,###,###,###,##0')}</td>
									<td style="text-align:right">${w:format(item.urlResponseTime,'0.0')}</td>
									<td style="text-align:right">${w:format(item.serviceCount,'#,###,###,###,##0')}</td>
									<td style="text-align:right">${w:format(item.serviceResponseTime,'0.0')}</td>
									<td style="text-align:right">${w:format(item.sqlCount,'#,###,###,###,##0')}</td>
									<td style="text-align:right">${w:format(item.pigeonCallCount,'#,###,###,###,##0')}</td>
									<td style="text-align:right">${w:format(item.swallowCallCount,'#,###,###,###,##0')}</td>
									<td style="text-align:right">${w:format(item.memcacheCount,'#,###,###,###,##0')}</td>
									<td style="text-align:right">${w:format(item.webScore,'0.00')}</td>
								</tr>
							</c:forEach>
						</table>
					</div>
				</div>
				<div class="tab-pane" id="tab2">
					<div class="report">
						<table class="table table-striped table-bordered table-condensed">
							<tr>
								<th class="left">id</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&tab=tab2">Machine Number</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=urlCount&tab=tab2">URL Count</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=urlResponse&tab=tab2">URL Response Time</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=serviceCount&tab=tab2">Service Count</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=serviceResponse&tab=tab2">Service Response Time</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=sqlCount&tab=tab2">SQL Count</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=pigeonCallCount&tab=tab2">Pigeon Call Count</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=swallowCallCount&tab=tab2">Swallow Call Count</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=memcacheCount&tab=tab2">Memcache Count</th>
								<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=utilization&sort=serviceScore&tab=tab2">Service Score</th>
							</tr>
						
							<c:forEach var="item" items="${model.utilizationServiceList}" varStatus="status">
								<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
									<td>${item.id}</td>
									<td style="text-align:right">${item.machineNumber}</td>
									<td style="text-align:right">${w:format(item.urlCount,'#,###,###,###,##0')}</td>
									<td style="text-align:right">${w:format(item.urlResponseTime,'0.0')}</td>
									<td style="text-align:right">${w:format(item.serviceCount,'#,###,###,###,##0')}</td>
									<td style="text-align:right">${w:format(item.serviceResponseTime,'0.0')}</td>
									<td style="text-align:right">${w:format(item.sqlCount,'#,###,###,###,##0')}</td>
									<td style="text-align:right">${w:format(item.pigeonCallCount,'#,###,###,###,##0')}</td>
									<td style="text-align:right">${w:format(item.swallowCallCount,'#,###,###,###,##0')}</td>
									<td style="text-align:right">${w:format(item.memcacheCount,'#,###,###,###,##0')}</td>
									<td style="text-align:right">${w:format(item.serviceScore,'0.00')}</td>
								</tr>
							</c:forEach>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>