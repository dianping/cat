<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.metric.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.metric.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.metric.Model" scope="request"/>

<a:body>
<res:useJs value="${res.js.local['highcharts.js']}" target="head-js"/>
<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>

<script type="text/javascript">
	$(document).ready(function() {
		<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
			var data = ${item.jsonString};
			graphMetricChart(document.getElementById('${item.title}'), data);
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
					&nbsp;[ <a href="${model.baseUri}?date=${model.date}&domain=${model.domain}&step=${nav.hours}&product=${payload.product}&timeRange=${payload.timeRange}&test=${payload.test}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}&product=${payload.product}&timeRange=${payload.timeRange}">now</a> ]&nbsp;
			</td>
		</tr>
	</table>	
	<table>
		<tr style="text-align: left">
			<th>&nbsp;&nbsp;时间段选择: 
				<c:forEach var="range" items="${model.allRange}">
					<c:choose>
						<c:when test="${payload.timeRange eq range.duration}">
							&nbsp;&nbsp;&nbsp;[ <a href="?date=${model.date}&domain=${model.domain}&product=${payload.product}&test=${payload.test}&timeRange=${range.duration}" class="current">${range.title}</a> ]
						</c:when>
						<c:otherwise>
							&nbsp;&nbsp;&nbsp;[ <a href="?date=${model.date}&domain=${model.domain}&product=${payload.product}&test=${payload.test}&timeRange=${range.duration}">${range.title}</a> ]
						</c:otherwise>
						</c:choose>
				</c:forEach>
			</th>
		</tr>
	</table>
	<div class="row-fluid" style="margin-top:2px;height:30px;"></div>
      <div class="row-fluid">
        <div class="span2">
          <div class="well sidebar-nav">
            <ul class="nav nav-list">
            	 <li class='nav-header' id="${item.id}"><a href="?op=dashboard&timeRange=${payload.timeRange}&date=${model.date}&domain=${model.domain}"><strong>业务大盘</strong></a></li>
	            <c:forEach var="item" items="${model.productLines}" varStatus="status">
	              <li class='nav-header' id="${item.id}"><a href="?date=${model.date}&domain=${model.domain}&product=${item.id}&timeRange=${payload.timeRange}"><strong>${item.title}</strong></a></li>
	              <%-- <c:if test="${payload.product eq item.id }">
		               <c:forEach var="test" items="${model.abtests}" varStatus="status">
		               	   <c:if test="${test.value.id ne -1}">
				              <li id="${test.key}"><a href="?date=${model.date}&domain=${model.domain}&product=${payload.product}&timeRange=${payload.timeRange}&test=${test.key}">${test.key}<i tips="" data-trigger="hover" class="icon-question-sign" data-toggle="popover" data-placement="right" data-content="${test.value.name}"></i></a>
				              </li>
		               	   </c:if>
		       		  </c:forEach>
	              </c:if> --%>
	            </c:forEach>
              <li >&nbsp;</li>
            </ul>
          </div><!--/.well -->
        </div><!--/span-->
        <div class="span10">
        	<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
       			<div style="float:left;">
       				<div id="${item.title}" class="metricGraph"></div>
       			</div>
			</c:forEach>
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
