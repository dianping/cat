<%@ page contentType="text/html; charset=utf-8"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.transaction.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.transaction.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.transaction.Model" scope="request" />

${model.graph}

<%-- <table class="graphs">
	<tr>
		<td><img src="?op=graph&domain=${payload.domain}&type=${payload.type}&name=${payload.name}&graph=1" class="graph"></td>
		<td><img src="?op=graph&domain=${payload.domain}&type=${payload.type}&name=${payload.name}&graph=2" class="graph"></td>
	</tr>
	<tr>
		<td><img src="?op=graph&domain=${payload.domain}&type=${payload.type}&name=${payload.name}&graph=3" class="graph"></td>
		<td><img src="?op=graph&domain=${payload.domain}&type=${payload.type}&name=${payload.name}&graph=4" class="graph"></td>
	</tr>
</table> --%>