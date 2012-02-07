<%@ page contentType="text/html; charset=utf-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.ebay.com/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.ip.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.ip.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.ip.Model" scope="request" />

<a:report title="Hot IP Report" timestamp="2012-02-07">

<jsp:attribute name="domain">
	<div class="domain">
	<c:forEach var="domain" items="${model.domains}">
		&nbsp;[
		<c:choose>
			<c:when test="${payload.domain eq domain}"><a href="?domain=${domain}" class="current">&nbsp;${domain}&nbsp;</a></c:when>
			<c:otherwise><a href="?domain=${domain}">&nbsp;${domain}&nbsp;</a></c:otherwise>
		</c:choose>
		]&nbsp;
	</c:forEach>
	</div>
</jsp:attribute>
<jsp:attribute name="nav">
[ <a href="">-1d</a> ] [ <a href="">-2h</a> ] [ <a href="">-1h</a> ] [ <a href="">+1h</a> ] [ <a href="">+2h</a> ] [ <a href="">+1d</a> ]
</jsp:attribute>

<jsp:body>

<res:useCss value='${res.css.local.ip_css}' target="head-css"/>

<table class="ip-table">
	<tr><th>IP</th><th>last 1 min</th><th>last 5 mins</th><th>last 15 mins</th></tr>
	<c:forEach var="m" items="${model.displayModels}" varStatus="status">
		<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
			<td>${m.address}</td>
			<td>${m.lastOne}</td>
			<td>${m.lastFive}</td>
			<td>${m.lastFifteen}</td>
		</tr>
	</c:forEach>
</table>

</jsp:body>
	
</a:report>
