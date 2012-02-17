<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.transaction.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.transaction.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.transaction.Model" scope="request" />

<script type="text/javascript">
	var data = ${model.jsonResult};
	var nowtype = '${model.type}';
	var domain = '${model.currentDomain}';
</script>

<res:useCss value='${res.css.local.default_css}' target="head-css" />
<res:useCss value='${res.css.local.style_css}' target="head-css" />
<res:useCss value='${res.css.local.failure_css}' target="head-css" />

<res:useJs value='${res.js.local.jquery_min_js}' target="head-js" />

<a:report title="Transaction Report" timestamp="2012-02-07">

<table id="transactionTable" width="100%" border="0" cellspacing="0"></table>

</a:report>

	<res:useJs value="${res.js.local.transaction_js}" target="bottom-js" />