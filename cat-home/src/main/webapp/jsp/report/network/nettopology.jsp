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
		<res:bean id="res" />
		<res:useCss value='${res.css.local.table_css}' target="head-css" />
		<res:useCss value='${res.css.local.body_css}' target="head-css" />
		<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
		<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js" />
		<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js" />
		<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js" />
		<res:useJs value="${res.js.local['startopo.js']}" target="head-js" />
		<res:useJs value="${res.js.local['raphael-min.js']}" target="head-js" />
		<res:useJs value="${res.js.local['dependencyConfig.js']}" target="head-js" />
		<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
		<res:useJs value="${res.js.local['netgraph.js']}" target="head-js" />
		<a href="javascript:showOpNav()" id="switch" class="btn btn-small btn-success">隐藏</a>
		<div class="opNav">
		<%@ include file="metricOpNav.jsp" %>
		<%@ include file="TimeNavTab.jsp"%>

		</div>
		<div class="row-fluid">
		<div id="content">

					<c:forEach var="topo" items="${model.netGraphData}" varStatus="idx">
						<div class="topology">
							<p>${topo.key}</p>
							<div id="topo-${idx.index}"></div>
						</div>
					</c:forEach>

				</div>
				</div>
			<style type="text/css">
.topology {
	width: 600px;
	}
	</style>
	</c:when>
	<c:otherwise>
		<a:body>

			<res:useCss value='${res.css.local.table_css}' target="head-css" />
			<res:useJs value="${res.js.local['netgraph.js']}" target="head-js" />


<div class="report">
			<table class="header">
				<tr>
					<td class="title">&nbsp;&nbsp;From
						${w:format(model.startTime,'yyyy-MM-dd HH:mm:ss')} to
						${w:format(model.endTime,'yyyy-MM-dd HH:mm:ss')}</td>
					<td class="nav"><c:forEach var="nav" items="${model.navs}">
							&nbsp;[ <a
								href="${model.baseUri}?date=${model.date}&domain=${model.domain}&step=${nav.hours}&product=${payload.product}&timeRange=${payload.timeRange}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
						</c:forEach> &nbsp;[ <a
						href="${model.baseUri}?${navUrlPrefix}&product=${payload.product}&timeRange=${payload.timeRange}">now</a>
						]&nbsp;</td>
				</tr>
			</table>
			<%@ include file="metricOpNav.jsp"%>
			<%@ include file="TimeNavTab.jsp"%>
			
		

			</div>


		

			<div class="row-fluid">

				<div class="span2">
					<div class="well sidebar-nav">
						<ul class="nav nav-list">
							<li class='nav-header active' id="metric_nettopology"><a
								href="?op=topo"><strong>核心拓扑</strong></a></li>
							<c:forEach var="item" items="${model.metricAggregationGroup}"
								varStatus="status">
								<li class='nav-header' id="metric_${item.id}"><a
									href="?op=dashboard&group=${item.id}&timeRange=${payload.timeRange}&date=${model.date}&domain=${model.domain}"><strong>${item.id}</strong></a></li>
							</c:forEach>

							<c:forEach var="item" items="${model.productLines}"
								varStatus="status">
								<li class='nav-header' id="metric_${item.id}"><a
									href="?date=${model.date}&domain=${model.domain}&product=${item.id}&timeRange=${payload.timeRange}"><strong>${item.id}</strong></a></li>
							</c:forEach>
							<li>&nbsp;</li>
						</ul>
					</div>
					<!--/.well -->
				</div>
				<!--/span-->
				

				<div id="content" class="span10">

					<c:forEach var="topo" items="${model.netGraphData}" varStatus="idx">
						<div class="topology">
							<p>${topo.key}</p>
							<div id="topo-${idx.index}"></div>
						</div>
					</c:forEach>

				</div>
			</div>
			

			</a:body>
			<style type="text/css">
.topology {
	width: 520px;
	}
	</style>
				</c:otherwise>
</c:choose>
			<style type="text/css">
.row-fluid {
	min-width: 1200px;
}			

.row-fluid .span2 {
	width: 10%;
}

.row-fluid .span10 {
	width: 87%;
}

.topology {
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

			<script type="text/javascript">

<c:forEach var="topo" items="${model.netGraphData}" varStatus="idx">
	$_netgraph.build("topo-${idx.index}",${topo.value});
</c:forEach>

function showOpNav() {
	var b = $('#switch').html();
	if (b == '隐藏') {
		$('.opNav').slideUp();
		$('#switch').html("显示");
	} else {
		$('.opNav').slideDown();
		$('#switch').html("隐藏");
	}
}

<c:choose>
<c:when test="${payload.fullScreen}">
	showOpNav();
</c:when>
</c:choose>

</script>

