<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.network.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.network.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.network.Model" scope="request" />
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
		<div id="content">
					<c:forEach var="topo" items="${model.netGraphData}" varStatus="idx">
						<div class="topology">
							<p>${topo.key}</p>
							<div id="topo-${idx.index}"></div>
						</div>
					</c:forEach>
				</div>
			<style type="text/css">
		.topology {
	width: 600px;
	}
	.row-fluid {
	min-width: 1200px;
}	
	</style>
	</c:when>
	<c:otherwise>
		<a:application>
			<res:useCss value='${res.css.local.table_css}' target="head-css" />
			<res:useJs value="${res.js.local['netgraph.js']}" target="head-js" />

		<div class="report">
			<div class="breadcrumbs" id="breadcrumbs">
			<span class="text-danger title"></span><span class="text-success">&nbsp;&nbsp;${w:format(model.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.endTime,'yyyy-MM-dd HH:mm:ss')}</span>
			<div class="nav-search nav" id="nav-search">
				<c:forEach var="nav" items="${model.navs}">
						&nbsp;[ <a
							href="${model.baseUri}?date=${model.date}&domain=${model.domain}&step=${nav.hours}&product=${payload.product}&timeRange=${payload.timeRange}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
					</c:forEach> &nbsp;[ <a href="${model.baseUri}?op=${payload.action.name}&timeRange=${payload.timeRange}">now</a>
					]&nbsp;
			</div></div>
			<%@ include file="TimeNavTab.jsp"%>
			</div>
			<div class="col-xs-12">
				<c:forEach var="topo" items="${model.netGraphData}" varStatus="idx">
					<div class="topology" >
						<p>${topo.key}</p>
						<div id="topo-${idx.index}"></div>
					</div>
				</c:forEach>
			</div>
			</a:application>
			<style type="text/css">
		.topology {
			width: 520px;
		}
	</style>
				</c:otherwise>
</c:choose>
<style type="text/css">

.topology {
	text-align: center;
	margin: 20px 0;
	height: 840px;
	float: left;
}

</style>

<script type="text/javascript">

<c:forEach var="topo" items="${model.netGraphData}" varStatus="idx">
	$_netgraph.build("topo-${idx.index}",${topo.value});
</c:forEach>

$(document).ready(function() {
	$('#Dashboard_report').addClass("open active");
	$('#dashbord_network').addClass("active");
});

</script>

