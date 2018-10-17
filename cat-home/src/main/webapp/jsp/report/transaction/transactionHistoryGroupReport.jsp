<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"
	type="com.dianping.cat.report.page.transaction.Context" scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.report.page.transaction.Payload" scope="request" />
<jsp:useBean id="model"
	type="com.dianping.cat.report.page.transaction.Model" scope="request" />

<a:historyReport title="History Report"
	navUrlPrefix="type=${payload.encodedType}&queryname=${model.queryName}">
	<jsp:attribute name="subtitle">${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
<table class="machines">
	<tr style="text-align: left">
		<th>
		<c:forEach items="${model.ips}" var="value">
    		<c:if test="${value == 'All'}">
        		<c:set var="found" value="true" scope="request" />
    		</c:if>
		</c:forEach>
		<c:if test="${found != true}">
			&nbsp;[ &nbsp;<a href="?op=history&domain=${model.domain}&date=${model.date}&type=${payload.encodedType}&queryname=${model.queryName}&reportType=${payload.reportType}${model.customDate}">All</a>
			&nbsp;]&nbsp;
		</c:if>
   	  	<c:forEach var="ip" items="${model.ips}">&nbsp;[&nbsp;
						<a href="?op=history&domain=${model.domain}&date=${model.date}&ip=${ip}&type=${payload.encodedType}&queryname=${model.queryName}&reportType=${payload.reportType}${model.customDate}">${ip}</a>
   	 		&nbsp;]&nbsp;
		</c:forEach>
		</th>
	</tr>
</table>

<table class="groups">
	<tr class="left">
		<th>
   	 		<c:forEach var="group" items="${model.groups}">
				<c:choose>
							<c:when test="${payload.group eq group}">
		   	  		&nbsp;[&nbsp;
		   	  			<a class="current" href="?op=historyGroupReport&domain=${model.domain}&group=${group}&date=${model.date}&reportType=${payload.reportType}&type=${payload.encodedType}">${group}</a>
		   	 		&nbsp;]&nbsp;
	   	 		</c:when>
	   	 		<c:otherwise>
		   	  		&nbsp;[&nbsp;
		   	  			<a href="?op=historyGroupReport&domain=${model.domain}&group=${group}&date=${model.date}&reportType=${payload.reportType}&type=${payload.encodedType}">${group}</a>
		   	 		&nbsp;]&nbsp;
	   	 		</c:otherwise>
						</c:choose>
			 </c:forEach>
		</th>
	</tr>
</table>
<script type="text/javascript" src="/cat/js/appendHostname.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		appendHostname(${model.ipToHostnameStr});

		$.each($('table.machines a'), function(index, item) {
			var id = $(item).text();
			<c:forEach var="ip" items="${model.groupIps}">
			group = '${ip}';
			if(id.indexOf(group)!=-1){
				$(item).addClass('current');
			}
			</c:forEach>
		});

	});
</script>
<table class='table table-hover table-striped table-condensed ' >
	<c:choose>
		<c:when test="${empty payload.type}">
		<tr>
			<th style="text-align: left;"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&sort=type${model.customDate}">Type</a></th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&sort=total${model.customDate}">Total</a></th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&sort=failure${model.customDate}">Failure</a></th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&sort=failurePercent${model.customDate}">Failure%</a></th>
			<th class="right">Sample Link</th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&sort=min${model.customDate}">Min</a>(ms)</th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&sort=max${model.customDate}">Max</a>(ms)</th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&sort=avg${model.customDate}">Avg</a>(ms)</th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&sort=95line${model.customDate}">95Line</a>(ms)</th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&sort=99line${model.customDate}">99Line</a>(ms)</th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&sort=std${model.customDate}">Std</a>(ms)</th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&sort=total${model.customDate}">QPS</a></th>
					</tr>
			<c:forEach var="item" items="${model.displayTypeReport.results}"
						varStatus="status">
				<c:set var="e" value="${item.detail}" />
				<c:set var="lastIndex" value="${status.index}" />
				<tr class=" right">
					<td style="text-align: left">
							<a
								href="?op=historyGroupGraph&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&type=${item.type}${model.customDate}"
								class="history_graph_link" data-status="${status.index}">[:: show ::]</a>
							&nbsp;&nbsp;&nbsp;<a
								href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&type=${item.type}${model.customDate}">${e.id}</a>
							</td>
					<td>${w:format(e.totalCount,'#,###,###,###,##0')}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent/100,'0.0000%')}</td>
					<td><a
								href="/cat/r/m/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}?domain=${model.domain}">Log View</a></td>
					<td>${w:format(e.min,'0.#')}</td>
					<td>${w:format(e.max,'0.#')}</td>
					<td>${w:format(e.avg,'0.0')}</td>
					<td>${w:format(e.line95Value,'0.0')}</td>
					<td>${w:format(e.line99Value,'0.0')}</td>
					<td>${w:format(e.std,'0.0')}</td>
					<td>${w:format(e.tps,'0.0')}</td>
				</tr>
				<tr class="graphs">
					<td colspan="12" style="display: none"><div id="${status.index}"
								style="display: none"></div></td>
					</tr><tr></tr>
			</c:forEach>
		</c:when>
		<c:otherwise>
		<tr>
						<th style="text-align: left;" colspan='13'>
			<input type="text" id="queryname" size="40"
							value="${model.queryName}">
		    <input class="btn btn-primary btn-sm" value="Filter"
							onclick="filterByName('${model.date}','${model.domain}','${model.ipAddress}','${payload.type}')"
							type="submit">
		    支持多个字符串查询，例如sql|url|task，查询结果为包含任一sql、url、task的列
		</th>
					</tr>
					<script>
						function filterByName(date, domain, ip, type) {
							var customDate = '${model.customDate}';
							var reportType = '${payload.reportType}';
							var type = '${payload.type}';
							var queryname = $("#queryname").val();
							window.location.href = "?op=historyGroupReport&domain="
									+ domain
									+ "&ip="
									+ ip
									+ "&date="
									+ date
									+ '&type='
									+ type
									+ '&reportType='
									+ reportType
									+ "&queryname="
									+ queryname
									+ customDate;
						}
					</script>
			<tr>
			<th style="text-align: left;">
			<a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=type${model.customDate}&queryname=${model.queryName}">Name</a>
						</th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=total${model.customDate}&queryname=${model.queryName}">Total</a></th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=failure${model.customDate}&queryname=${model.queryName}">Failure</a></th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=failurePercent${model.customDate}&queryname=${model.queryName}">Failure%</a></th>
			<th class="right">Sample Link</th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=min${model.customDate}&queryname=${model.queryName}">Min</a>(ms)</th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=max${model.customDate}&queryname=${model.queryName}">Max</a>(ms)</th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=avg${model.customDate}&queryname=${model.queryName}">Avg</a>(ms)</th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=95line${model.customDate}&queryname=${model.queryName}">95Line</a>(ms)</th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=99line${model.customDate}&queryname=${model.queryName}">99.9Line</a>(ms)</th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=std${model.customDate}&queryname=${model.queryName}">Std</a>(ms)</th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=total${model.customDate}&queryname=${model.queryName}">QPS</a></th>
			<th class="right"><a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=total${model.customDate}&queryname=${model.queryName}">Percent%</a></th>
					</tr>
			<c:forEach var="item" items="${model.displayNameReport.results}"
						varStatus="status">
				<c:set var="e" value="${item.detail}" />
				<c:set var="lastIndex" value="${status.index}" />
				<tr class=" right">
					<td class="longText" style="text-align: left; white-space: normal">
					<c:choose>
					<c:when test="${status.index > 0}">
					<a href="?op=historyGroupGraph&domain=${model.domain}&date=${model.date}&group=${payload.group}&reportType=${payload.reportType}&type=${payload.encodedType}&name=${item.name}${model.customDate}"
											class="history_graph_link" data-status="${status.index}">[:: show ::]</a> 
					</c:when>
					<c:otherwise></c:otherwise>
								</c:choose>
					&nbsp;&nbsp;&nbsp;${w:shorten(e.id, 120)}</td>
					<td>${w:format(e.totalCount,'#,###,###,###,##0')}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent/100,'0.0000%')}</td>
					<td><a
								href="/cat/r/m/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}?domain=${model.domain}">Log View</a></td>
					<td>${w:format(e.min,'0.#')}</td>
					<td>${w:format(e.max,'0.#')}</td>
					<td>${w:format(e.avg,'0.0')}</td>
					<td>${w:format(e.line95Value,'0.0')}</td>
					<td>${w:format(e.line99Value,'0.0')}</td>
					<td>${w:format(e.std,'0.0')}</td>
					<td>${w:format(e.tps,'0.0')}</td>
					<td>${w:format(e.totalPercent,'0.00%')}</td>
				</tr>
				<tr class="graphs">
						<td colspan="12" style="display: none"><div id="${status.index}" style="display: none"></div></td>
				</tr>
				<tr></tr>
			</c:forEach>
		</c:otherwise>
	</c:choose>
</table>

<font color="white">${lastIndex+1}</font>
<res:useJs value="${res.js.local.transaction_js}" target="bottom-js" />
<c:choose>
	<c:when test="${not empty payload.type}">
		<table>
			<tr>
				<td><div id="transactionGraph" class="pieChart"></div>
				</td>
			</tr>
		</table>
		<script type="text/javascript">
			var data = ${model.pieChart};
			graphPieChart(document.getElementById('transactionGraph'), data);
		</script>
	</c:when>
</c:choose>
</br>
</jsp:body>

</a:historyReport>