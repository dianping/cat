<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.failure.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.failure.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.failure.Model" scope="request" />

<res:useCss value='${res.css.local.style_css}' target="head-css" />
<res:useCss value='${res.css.local.failure_css}' target="head-css" />

<res:useJs value='${res.js.local.jquery_min_js}' target="head-js" />
<res:useJs value='${res.js.local.sql_scripts_js}' target="head-js" />

<script type="text/javascript">
	 var jsonData = ${model.jsonResult};
</script>

<a:report title="Failure Report" timestamp="2012-02-07">

	<table id="failureTable" width="100%" border="0" cellspacing="0"></table>

</a:report>
	
<res:useJs value="${res.js.local.failure_js}" target="bottom-js" />