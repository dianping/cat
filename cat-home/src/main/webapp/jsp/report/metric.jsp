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
		<c:forEach var="item" items="${model.display.lineCharts}" varStatus="status">
			var data = ${item.jsonString};
			graphLineChart(document.getElementById('${item.title}'), data);
		</c:forEach>
		
		var product = '${payload.product}';
		var test = '${payload.test}';
		
		$('#'+product).addClass('active');
		$('#'+test).addClass('active');
	});
</script>
<div class="report">
	<table class="header">
		<tr>
			<td class="title">&nbsp;&nbsp;From ${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</td>
			<td class="nav">
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?date=${model.date}&domain=${model.domain}&step=${nav.hours}&product=${payload.product}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}">now</a> ]&nbsp;
			</td>
		</tr>
	</table>
	</br>
	<div class="container-fluid">
      <div class="row-fluid">
        <div class="span2">
          <div class="well sidebar-nav">
            <ul class="nav nav-list">
	            <c:forEach var="item" items="${model.products}" varStatus="status">
	              <li class='nav-header' id="${item}"><a href="?date=${model.date}&domain=${model.domain}&product=${item}"><strong>${item}</strong></a></li>
	              <c:if test="${payload.product eq item }">
		               <c:forEach var="item" items="${model.display.abtests}" varStatus="status">
		               	   <c:if test="${item ne -1}">
				              <li id="${item}"><a href="?date=${model.date}&domain=${model.domain}&product=${payload.product}&test=${item}">AB测试${item}</a></li>
		               	   </c:if>
		       		  </c:forEach>
	              </c:if>
	            </c:forEach>
              <li >&nbsp;</li>
             
            </ul>
          </div><!--/.well -->
        </div><!--/span-->
        <div class="span10">
        	<c:forEach var="item" items="${model.display.lineCharts}" varStatus="status">
       			<div style="float:left;">
       				<h5 class="text-center text-error">${item.title}</h5>
       				<div  id="${item.title}" class="graph"></div>
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
.graph {
	width: 380px;
	height: 250px;
	margin: 4px auto;
}
.row-fluid .span2{
	width:12%;
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
