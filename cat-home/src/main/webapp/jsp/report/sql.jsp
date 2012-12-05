<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.sql.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.sql.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.sql.Model"scope="request" />
<c:set var="report" value="${model.report}" />

<a:report
	title="Sql Report"
	navUrlPrefix="database=${model.database}&domain=${model.domain}"
	timestamp="${w:format(model.creatTime,'yyyy-MM-dd HH:mm:ss')}">

	<jsp:attribute name="subtitle">From ${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>

	<jsp:body>

	<res:useCss value="${res.css.local.sql_css}" target="head-css" />
	<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js" />

</br>
<table class="databases">
	<tr style="text-align: left">
		<th>Databases: &nbsp;[&nbsp; <c:choose>
				<c:when test="${model.database eq 'All'}">
					<a href="?domain=${model.domain}&date=${model.date}"
								class="current">All</a>
				</c:when>
				<c:otherwise>
					<a href="?domain=${model.domain}&date=${model.date}">All</a>
				</c:otherwise>
			</c:choose> &nbsp;]&nbsp; <c:forEach var="database" items="${model.databases}">
   	  		&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${model.database eq database}">
						<a href="?domain=${model.domain}&database=${database}&date=${model.date}"
									class="current">${database}</a>
					</c:when>
					<c:otherwise>
						<a href="?domain=${model.domain}&database=${database}&date=${model.date}">${database}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th>
	</tr>
</table>
<br>
<table class="sql">
			<tr>
			    <th></th>
				<th class="left"><a
					href="?database=${model.database}&date=${model.date}&domain=${model.domain}&sort=name">Table</a></th>
				<th><a href="?database=${model.database}&date=${model.date}&domain=${model.domain}&sort=total">Total</a></th>
				<th><a href="?database=${model.database}&date=${model.date}&domain=${model.domain}&sort=failure">Failure</a></th>
				<th><a href="?database=${model.database}&date=${model.date}&domain=${model.domain}&sort=failurePercent">Failure%</a></th>
				<th><a href="?database=${model.database}&date=${model.date}&domain=${model.domain}&sort=avg">Avg(ms)</a></th>
				<th>Percent%</th>
				<th>TPS</th>
			</tr>
			<c:forEach var="item" items="${model.displaySqlReport.results}"
				varStatus="status">
				<tr class="${status.index  mod 2==0 ? 'even' : 'odd'}">
					<td class="left"><a href="" class="graph_link" data-status="${status.index}">[:: show ::]</a></td>
					<td class="left">${item.id}</td>
					<td>${w:format(item.totalCount,'#,###,###,###,##0')}</td>
					<td>${w:format(item.failCount,'#,###,###,###,##0')}</td>
					<td>${w:format(item.failPercent,'0.00%')}</td>
					<td>${w:format(item.avg,'0.00')}</td>
					<td>${w:format(item.totalPercent,'0.00%')}</td>
					<td>${w:format(item.tps,'0.00')}</td>
				</tr>
				<tr id="${status.index}" style="display:none">
					<td colspan="8">
					<table>
						<th>Method</th>
						<th>Total</th>
						<th>Failure</th>
						<th>Failure%</th>
						<th>Avg(ms)</th>
						<th>Percent%</th>
						<th>TPS</th>

						<c:forEach var="methodEntry" items="${item.methods}"
							varStatus="status1">
							<tr class="${status1.index  mod 2==0 ? 'even' : 'odd'}">
								<c:set var="method" value="${methodEntry.value}" />
								<td>${method.id}</td>
								<td>${w:format(method.totalCount,'#,###,###,###,##0')}</td>
								<td>${w:format(method.failCount,'#,###,###,###,##0')}</td>
								<td>${w:format(method.failPercent,'0.00%')}</td>
								<td>${w:format(method.avg,'0.00')}</td>
								<td>${w:format(method.totalPercent,'0.00%')}</td>
								<td>${w:format(method.tps,'0.00')}</td>
							</tr>
						</c:forEach>
					</table></td>
				</tr>
			</c:forEach>

		</table>
<font color="white">${lastIndex+1}</font>

<res:useJs value="${res.js.local.sql_js}" target="bottom-js" />
</jsp:body>
</a:report>
