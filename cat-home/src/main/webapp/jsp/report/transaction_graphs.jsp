<%@ page contentType="text/html; charset=utf-8"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.transaction.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.transaction.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.transaction.Model" scope="request" />

<table>
	<tr>
		<td>${model.graph1}</td>
		<td>${model.graph2}</td>
	</tr>
	<tr>
		<td>${model.graph3}</td>
		<td>${model.graph4}</td>
	</tr>
</table>
