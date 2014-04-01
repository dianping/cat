<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.metric.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.metric.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.metric.Model" scope="request"/>

<c:choose>
	<c:when test="${payload.fullScreen}">
		<res:bean id="res" />
		<res:useCss value='${res.css.local.body_css}' target="head-css" />
		<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
		<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js" />
		<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js" />
		<res:useJs value="${res.js.local['highcharts.js']}" target="head-js"/>
		<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>
		<a href="javascript:showOpNav()" id="switch" class="btn btn-small btn-success">隐藏</a>
		<div class="opNav">
			<table>
				<tr style="text-align: left">
					<th>&nbsp;&nbsp;时间段选择: 
						<c:forEach var="range" items="${model.allRange}">
							<c:choose>
								<c:when test="${payload.timeRange eq range.duration}">
									&nbsp;&nbsp;&nbsp;[ <a href="?op=dashboard&${navUrlPrefix}&fullScreen=${payload.fullScreen}&date=${model.date}&domain=${model.domain}&product=${payload.product}&timeRange=${range.duration}" class="current">${range.title}</a> ]
								</c:when>
								<c:otherwise>
									&nbsp;&nbsp;&nbsp;[ <a href="?op=dashboard&${navUrlPrefix}&fullScreen=${payload.fullScreen}&date=${model.date}&domain=${model.domain}&product=${payload.product}&timeRange=${range.duration}">${range.title}</a> ]
								</c:otherwise>
								</c:choose>
						</c:forEach>
					</th>
				</tr>
			</table>
			<%@ include file="metricOpNav.jsp" %>
		</div>
      	<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
   			<div style="float:left;">
   				<div id="${item.id}" class="metricGraph"></div>
   			</div>
		</c:forEach>
	</c:when>
	<c:otherwise>
	<a:body>
		<res:useJs value="${res.js.local['highcharts.js']}" target="head-js"/>
		<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>
		<div class="report">
			<table class="header">
				<tr>
					<td class="title">&nbsp;&nbsp;From ${w:format(model.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.endTime,'yyyy-MM-dd HH:mm:ss')}</td>
					<td class="nav">
						<c:forEach var="nav" items="${model.navs}">
							&nbsp;[ <a href="${model.baseUri}?op=dashboard&group=${payload.group}&date=${model.date}&domain=${model.domain}&step=${nav.hours}&timeRange=${payload.timeRange}&product=${payload.product}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
						</c:forEach>
						&nbsp;[ <a href="${model.baseUri}?op=dashboard&group=${payload.group}&${navUrlPrefix}&product=${payload.product}&timeRange=${payload.timeRange}">now</a> ]&nbsp;
					</td>
				</tr>
			</table>
			<table>
		<tr style="text-align: left">
				<th>&nbsp;&nbsp;时间段选择: 
					<c:forEach var="range" items="${model.allRange}">
						<c:choose>
							<c:when test="${payload.timeRange eq range.duration}">
								&nbsp;&nbsp;&nbsp;[ <a href="?op=dashboard&group=${payload.group}&date=${model.date}&domain=${model.domain}&product=${payload.product}&timeRange=${range.duration}" class="current">${range.title}</a> ]
							</c:when>
							<c:otherwise>
								&nbsp;&nbsp;&nbsp;[ <a href="?op=dashboard&group=${payload.group}&date=${model.date}&domain=${model.domain}&product=${payload.product}&timeRange=${range.duration}">${range.title}</a> ]
							</c:otherwise>
							</c:choose>
					</c:forEach>
				</th>
			</tr>
		</table>
			<%@ include file="detail.jsp" %>
			<table  class="footer">
				<tr>
					<td>[ end ]</td>
				</tr>
			</table>
		</div>
		</a:body>
	</c:otherwise>
</c:choose>

<script type="text/javascript">
	$(document).ready(function() {
		<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
			var data = ${item.jsonString};
			graphMetricChart(document.getElementById('${item.id}'), data);
		</c:forEach>
		
		var hide =${payload.hideNav};
		
		if(hide){
			$('.opNav').slideUp();
			$('#switch').html("显示");
		}
	});
	
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
	
</script>
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
