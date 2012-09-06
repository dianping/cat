<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.database.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.database.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.database.Model" scope="request" />
<c:set var="report" value="${model.report}" />


<a:body>
	<res:bean id="res" />
	<res:useCss value='${res.css.local.body_css}' target="head-css" />
	<res:useCss value='${res.css.local.report_css}' target="head-css" />
	<res:useCss value="${res.css.local.database_css}" target="head-css" />
	<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js" />

	<div class="report">
		<table class="header">
		<tr>
			<td class="title">&nbsp;&nbsp;From ${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</td>
			<td class="switch"><a href="?database=${model.database}">Switch To Hourly Mode</a>
			</td>
			<td class="nav">
					&nbsp;&nbsp;<c:forEach var="nav" items="${model.historyNavs}">
					<c:choose>
						<c:when test="${nav.title eq model.reportType}">
								&nbsp;&nbsp;[ <a href="?op=history&database=${model.database}&date=${model.date}&reportType=${nav.title}" class="current">${nav.title}</a> ]
						</c:when>
						<c:otherwise>
								&nbsp;&nbsp;[ <a href="?op=history&database=${model.database}&date=${model.date}&reportType=${nav.title}">${nav.title}</a> ]&nbsp;&nbsp;
						</c:otherwise>
					</c:choose>
				</c:forEach>
				&nbsp;&nbsp;[ <a href="?op=history&database=${model.database}&date=${model.date}&reportType=${model.reportType}&step=-1&${navUrlPrefix}">${model.currentNav.last}</a> ]&nbsp;&nbsp;
					&nbsp;&nbsp;[ <a href="?op=history&database=${model.database}&date=${model.date}&reportType=${model.reportType}&step=1&${navUrlPrefix}">${model.currentNav.next}</a> ]&nbsp;&nbsp;
					&nbsp;&nbsp;[ <a href="?op=history&database=${model.database}&reportType=${model.reportType}&nav=next&${navUrlPrefix}">now</a> ]&nbsp;&nbsp;
					
			</td>
		</tr>
	</table>

		<table class="navbar">
			<tr>
				<td class="domain">
					<div class="database">
						<c:forEach var="database" items="${model.databases}">
						&nbsp;<c:choose>
								<c:when test="${model.database eq database}">
									<a href="${model.baseUri}?op=history&database=${database}&date=${model.date}"
										class="current">[&nbsp;${database}&nbsp;]</a>
								</c:when>
								<c:otherwise>
									<a href="${model.baseUri}?op=history&database=${database}&date=${model.date}">[&nbsp;${database}&nbsp;]</a>
								</c:otherwise>
							</c:choose>&nbsp;
					</c:forEach>
					</div>
				</td>
			</tr>
		</table>

		</br>
		
		<table class="machines">
			<tr style="text-align: left">
				<th>Domains: &nbsp; <c:forEach var="domain" items="${model.domains}">
   	  		&nbsp;[&nbsp;
   	  				<c:choose>
							<c:when test="${model.domain eq domain}">
								<a href="?op=history&database=${model.database}&domain=${domain}&date=${model.date}"
									class="current">${domain}</a>
							</c:when>
							<c:otherwise>
								<a href="?op=history&database=${model.database}&domain=${domain}&date=${model.date}">${domain}</a>
							</c:otherwise>
					</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
				</th>
			</tr>
		</table>
		
		</br>

		<table class="database">
			<tr>
			    <th></th>
				<th class="left"><a href="?op=history&database=${model.database}&date=${model.date}&domain=${model.domain}&sort=name">Table</a></th>
				<th><a href="?op=history&database=${model.database}&date=${model.date}&domain=${model.domain}&sort=total">Total</a></th>
				<th><a href="?op=history&database=${model.database}&date=${model.date}&domain=${model.domain}&sort=failure">Failure</a></th>
				<th><a href="?op=history&database=${model.database}&date=${model.date}&domain=${model.domain}&sort=failurePercent">Failure%</a></th>
				<th><a href="?op=history&database=${model.database}&date=${model.date}&domain=${model.domain}&sort=avg">Avg(ms)</a></th>
				<th>Percent%</th>
				<th>TPS</th>
			</tr>
			<c:forEach var="item" items="${model.displayDatabase.results}"
				varStatus="status">
				<tr  class="${status.index  mod 2==0 ? 'even' : 'odd'}">
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
		<table class="footer">
			<tr>
				<td>[ end ]</td>
			</tr>
		</table>
	</div>
	</br>

<res:useJs value="${res.js.local.database_js}" target="bottom-js" />
</a:body>
