<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="model"
	type="com.dianping.cat.report.page.logview.Model" scope="request" />

<style type="text/css">
tr.odd td {
	background-color: #eee;
	font-size: small;
	white-space: nowrap;
	vertical-align: top;
}

tr.even td {
	background-color: white;
	font-size: small;
	white-space: nowrap;
	vertical-align: top;
}

tr.link td {
	font-size: small;
	white-space: nowrap;
	vertical-align: top;
}

.warn {
	color: yellow;
}

.error {
	color: red;
}

.header {
	font-size: small;
	white-space: nowrap;
	color: white;
}

.nav {
	color: white;
	display: none;
	font-size: small;
}
</style>
${model.mobileResponse}
