<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.transaction.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.transaction.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.transaction.Model" scope="request" />
<style type="text/css">
.graph {
	width: 550px;
	height: 250px;
	margin: 4px auto;
}
</style>
<script type="text/javascript" src="/cat/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="/cat/js/flotr2.js"></script>
<script>
	var responseTrend = ${model.responseTrend};
	var hitTrend = ${model.hitTrend};
</script>

<table>
	<tr>
		<td><div id="responseTrend" class="graph"></div>	</td>
		<td><div id="hitTrend" class="graph"></div>	</td>
	</tr>
</table>
<script type="text/javascript" src="/cat/js/transactionGraph.js"></script>
