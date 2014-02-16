<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.dependency.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.dependency.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.dependency.Model" scope="request"/>

<res:bean id="res" />
<res:useCss value='${res.css.local.table_css}' target="head-css" />
<res:useJs value="${res.js.local['dependencyConfig.js']}" target="head-js" />
<res:useCss value='${res.css.local.body_css}' target="head-css" />
<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js" />
<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js" />
<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>
	
<style>
	.tooltip-inner {
		max-width:36555px;
	 }
	.tab-content	table {
	  max-width: 100%;
	  background-color: transparent;
	  border-collapse: collapse;
	  border-spacing: 0; 
	}
</style>
 <c:choose>
	<c:when test="${payload.fullScreen}">
		<div class="report">
			<div class="row-fluid">
				<div class="span12 text-center">
					<%@ include file="dependencyOpNav.jsp"%>
			 		<%@ include file="dependencyTimeNavTab1.jsp"%>
			</div></div>
			<div id="fullScreenData">
				<div class="text-center" id="container" style="width:1400px;height:1600px;border:solid 1px #ccc;"></div>
				<br/>
				<%@ include file="../top/topMetric.jsp"%>
			</div>
	    </div>
	</c:when>
	<c:otherwise>
			<a:report title="Dependency Report"
		navUrlPrefix="domain=${model.domain}&op=metricDashboard">
		<jsp:attribute name="subtitle">From ${w:format(model.reportStart,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.reportEnd,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
		<jsp:body>
		<div class="report">
			<div class="row-fluid">
				<div class="span12 text-center">
					<%@ include file="dependencyOpNav.jsp"%>
			 		<%@ include file="dependencyTimeNavTab1.jsp"%>
			</div>
			</div>
			<div id="fullScreenData">
			<div class="row-fluid">
				<div class="span12">
				   <c:forEach var="item" items="${model.lineCharts}" varStatus="status">
		   				<div style="float:left;">
				   				<div id="${item.title}" class="metricGraph"></div>
				   			</div>
					</c:forEach>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span12">
					<%@ include file="../top/topMetric.jsp"%>
				</div>
			</div>
	    </div>
	</jsp:body>
	</a:report>
	</c:otherwise>
</c:choose>

<script type="text/javascript">
	$(document).ready(function() {
		$('#minute'+${model.minute}).addClass('disabled');
		$('.hreftip').tooltip({container:'body', html:true, delay:{show:0, hide:0}});
		$('.position').hide();
		$('.switch').hide();
	});
</script>
<script type="text/javascript">
	$(document).ready(function() {
		<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
			var data = ${item.jsonString};
			graphMetricChart(document.getElementById('${item.title}'), data);
		</c:forEach>
	});
</script>
<style>
.pagination{
		margin:4px 0;
	}
	.pagination ul{
		margin-top:0px;
	}
	.pagination ul > li > a, .pagination ul > li > span{
		padding:3px 10px;
	}
</style>
