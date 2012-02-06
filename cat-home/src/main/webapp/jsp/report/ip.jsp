<%@ page contentType="text/html; charset=utf-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.ebay.com/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.ip.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.ip.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.ip.Model" scope="request" />

<a:report title="Hot IP Report">

<jsp:attribute name="domain">
	Domain: ${payload.domain}
</jsp:attribute>
<jsp:attribute name="time">
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
	
<%-- <xmp>
${model.reportInJson}
</xmp> --%>
</jsp:body>
	
</a:report>
