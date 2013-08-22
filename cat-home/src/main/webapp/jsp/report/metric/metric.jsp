<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.metric.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.metric.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.metric.Model" scope="request"/>

<a:body>

<res:useCss value='${res.css.local.table_css}' target="head-css" />
<res:useJs value="${res.js.local['svgchart.latest.min.js']}" target="head-js"/>
<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>

<script type="text/javascript">
	$(document).ready(function() {
		<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
			var data = ${item.jsonString};
			graphLineChart(document.getElementById('${item.title}'), data);
		</c:forEach>
		
		var product = '${payload.product}';
		var test = '${payload.test}';
		
		$('#'+product).addClass('active');
		$('#'+test).addClass('active');
		$('i[tips]').popover();
	});
</script>
<div class="report">
	<table class="header">
		<tr>
			<td class="title">&nbsp;&nbsp;From ${w:format(model.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.endTime,'yyyy-MM-dd HH:mm:ss')}</td>
			<td class="nav">
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?date=${model.date}&domain=${model.domain}&step=${nav.hours}&product=${payload.product}&test=${payload.test}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}&product=${payload.product}">now</a> ]&nbsp;
			</td>
		</tr>
	</table>
	</br>
	<div class="container-fluid">
      <div class="row-fluid">
        <div class="span2">
          <div class="well sidebar-nav">
            <ul class="nav nav-list">
            	 <li class='nav-header' id="${item.id}"><a href="?op=dashboard&date=${model.date}&domain=${model.domain}"><strong>Dashboard</strong></a></li>
	            <c:forEach var="item" items="${model.productLines}" varStatus="status">
	              <li class='nav-header' id="${item.id}"><a href="?date=${model.date}&domain=${model.domain}&product=${item.id}"><strong>${item.title}</strong></a></li>
	              <c:if test="${payload.product eq item.id }">
		               <c:forEach var="test" items="${model.abtests}" varStatus="status">
		               	   <c:if test="${test.value.id ne -1}">
				              <li id="${test.key}"><a href="?date=${model.date}&domain=${model.domain}&product=${payload.product}&test=${test.key}">${test.key}<i tips="" data-trigger="hover" class="icon-question-sign" data-toggle="popover" data-placement="right" data-content="${test.value.name}"></i></a>
				              </li>
		               	   </c:if>
		       		  </c:forEach>
	              </c:if>
	            </c:forEach>
              <li >&nbsp;</li>
            </ul>
          </div><!--/.well -->
        </div><!--/span-->
        <div class="span10">
        	<h3 class='text-red'>说明：图中纵轴数据为10分钟数据之和</h3>
        	<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
       			<div style="float:left;">
       				<h5 class="text-center text-error">${item.title}</h5>
       				<div  id="${item.title}" class="metricGraph"></div>
       			</div>
			</c:forEach>
        </div>
	<table  class="footer">
		<tr>
			<td>[ end ]</td>
		</tr>
	</table>
</div>
</a:body>
<style type="text/css">
.row-fluid .span2{
	width:10%;
}
.row-fluid .span10{
	width:87%;
}
.well {
padding: 10px 10px 10px 19p;
}
.nav-list  li  a{
	padding:2px 15px;
}

.nav li  +.nav-header{
	margin-top:2px;
}
.nav-header{
	padding:5px 3px;
}
</style>
