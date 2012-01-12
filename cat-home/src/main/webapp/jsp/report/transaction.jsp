<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="res" uri="http://www.ebay.com/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.transaction.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.transaction.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.transaction.Model" scope="request"/>

<a:body>

<res:useCss value='${res.css.local.default_css}' target="head-css"/>
<res:useCss value='${res.css.local.jquery_css}' target="head-css"/>
<res:useCss value='${res.css.local.jqgrid_css}' target="head-css"/>

<res:useJs value='${res.js.local.jquery_min_js}' target="head-js"/>
<res:useJs value='${res.js.local.jquery_ui_min_js}' target="head-js"/>
<res:useJs value='${res.js.local.grid_js}' target="head-js"/>
<res:useJs value='${res.js.local.jqgrid_min_js}' target="head-js"/>

<script type="text/javascript">
var data =
{
   "transaction-report": ${model.reportInJson}
};
</script>

<table id="gridTable"></table>
<div id="gridPager"></div>

<res:useJs value="${res.js.local.transaction_js}" target="bottom-js"/>

</a:body>