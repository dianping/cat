<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"
	type="com.dianping.cat.report.page.network.Context" scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.report.page.network.Payload" scope="request" />
<jsp:useBean id="model"
	type="com.dianping.cat.report.page.network.Model" scope="request" />


<c:choose>
	<c:when test="${payload.fullScreen}">
	</c:when>
	<c:otherwise>
		<a:body>
			<res:useJs value="${res.js.local['netgraph.js']}" target="head-js" />

			<style type="text/css">
.row-fluid .span2{
	width:10%;
}
.row-fluid .span10{
	width:87%;
}
			
.topology {
	width: 520px;
	text-align: center;
	margin: 20px 0;
	height: 640px;
	float: left;
}

p {
	font-size: 18px;
	font-weight: bold;
}
</style>



			<div class="row-fluid">

				<div class="span2">
					<div class="well sidebar-nav">
						<ul class="nav nav-list">
							<li class='nav-header active' id="metric_nettopology"><a href="?op=topo"><strong>核心拓扑</strong></a></li>
							<c:forEach var="item" items="${model.metricAggregationGroup}" varStatus="status">
				              <li class='nav-header' id="metric_${item.id}"><a href="?op=dashboard&group=${item.id}&timeRange=${payload.timeRange}&date=${model.date}&domain=${model.domain}"><strong>${item.id}</strong></a></li>
			             </c:forEach>
			             
			             <c:forEach var="item" items="${model.productLines}" varStatus="status">
			              <li class='nav-header' id="metric_${item.id}"><a href="?date=${model.date}&domain=${model.domain}&product=${item.id}&timeRange=${payload.timeRange}"><strong>${item.id}</strong></a></li>
			            </c:forEach>
							<li>&nbsp;</li>
						</ul>
					</div>
					<!--/.well -->
				</div>
				<!--/span-->

				<div id="content" class="span10">

					<c:forEach var="topo" items="${model.topoData}" varStatus="idx">
						<div class="topology">
							<p>${topo.key}</p>
							<div id="topo-${idx.index}"></div>
						</div>
					</c:forEach>

				</div>

			</div>

			<script type="text/javascript">

<c:forEach var="topo" items="${model.topoData}" varStatus="idx">
	$_netgraph.build("topo-${idx.index}",${topo.value});
</c:forEach>

</script>

		</a:body>

	</c:otherwise>
</c:choose>
