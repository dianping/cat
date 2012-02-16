<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"
	type="com.dianping.cat.report.page.transaction.Context" scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.report.page.transaction.Payload" scope="request" />
<jsp:useBean id="model"
	type="com.dianping.cat.report.page.transaction.Model" scope="request" />
<script type="text/javascript">
	var data = ${model.jsonResult};
	var nowtype = "${model.type}";
</script>

<res:useCss value='${res.css.local.style_css}' target="head-css" />
<res:useCss value='${res.css.local.default_css}' target="head-css" />
<res:useJs value='${res.js.local.jquery_min_js}' target="head-js" />

<a:report title="Transaction Report" timestamp="2012-02-07">
	<jsp:attribute name="domain">
	<div class="domain">
	<c:forEach var="domain" items="${model.domains}">
		&nbsp;[
		<c:choose>
			<c:when test="${payload.domain eq domain}">
					<a href="?domain=${domain}" class="current">&nbsp;${domain}&nbsp;</a>
				</c:when>
			<c:otherwise>
					<a href="?domain=${domain}">&nbsp;${domain}&nbsp;</a>
				</c:otherwise>
		</c:choose>
		]&nbsp;
	</c:forEach>
	</div>
</jsp:attribute>
	<jsp:attribute name="nav">
[ <a href="">-1d</a> ] [ <a href="">-2h</a> ] [ <a href="">-1h</a> ] [ <a
			href="">+1h</a> ] [ <a href="">+2h</a> ] [ <a href="">+1d</a> ]
</jsp:attribute>

	<jsp:body>

<res:useCss value='${res.css.local.ip_css}' target="head-css" />

<table id="transactionTable" width="100%" border="0" cellspacing="0"></table>
<res:useJs value="${res.js.local.transaction_js}" target="bottom-js" />
</jsp:body>
</a:report>
