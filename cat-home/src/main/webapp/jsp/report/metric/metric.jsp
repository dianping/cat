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
		<script src='${model.webapp}/assets/js/jquery.min.js'> </script>
		<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js" />
		<res:useJs value="${res.js.local['highcharts.js']}" target="head-js"/>
		<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>
      	<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
   			<div style="float:left;">
   				<div id="${item.id}" class="metricGraph"></div>
   			</div>
		</c:forEach>
	</c:when>
	<c:otherwise>
	<a:body>
		<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>
		<div class="report">
			<div class="breadcrumbs" id="breadcrumbs">
			<span class="text-danger title">【报表时间】</span><span class="text-success">&nbsp;&nbsp;${w:format(model.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.endTime,'yyyy-MM-dd HH:mm:ss')}</span>
			<!-- #section:basics/content.searchbox -->
			<div class="nav-search nav" id="nav-search">
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?op=view&tag=${payload.tag}&date=${model.date}&domain=${model.domain}&step=${nav.hours}&timeRange=${payload.timeRange}&product=${payload.product}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?op=view&tag=${payload.tag}&${navUrlPrefix}&product=${payload.product}&timeRange=${payload.timeRange}">now</a> ]&nbsp;
			</div></div>
			<table>
			<tr style="text-align: left">
				<th>&nbsp;&nbsp;业务
				<select id="productId" onchange="productChange()">
					<c:forEach var="item" items="${model.tags}" varStatus="status">
					  <option value="t_${item}">标签_${item}</option>
					</c:forEach>
					<c:forEach var="item" items="${model.productLines}" varStatus="status">
					  <option value="p_${item.id}">产品线_${item.title}</option>
					</c:forEach>
				</select>
				&nbsp;&nbsp;时间段 
					<c:forEach var="range" items="${model.allRange}">
						<c:choose>
							<c:when test="${payload.timeRange eq range.duration}">
								&nbsp;&nbsp;&nbsp;[ <a href="?op=view&tag=${payload.tag}&date=${model.date}&domain=${model.domain}&product=${payload.product}&timeRange=${range.duration}" class="current">${range.title}</a> ]
							</c:when>
							<c:otherwise>
								&nbsp;&nbsp;&nbsp;[ <a href="?op=view&tag=${payload.tag}&date=${model.date}&domain=${model.domain}&product=${payload.product}&timeRange=${range.duration}">${range.title}</a> ]
							</c:otherwise>
							</c:choose>
					</c:forEach>
				</th>
				<th class="text-right">
				</th>
			</tr>
		</table>
		 <div class="col-xs-12">
			<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
	   			<div style="float:left;">
	   				<div id="${item.id}" class="metricGraph" style="width:450px;height:350px;"></div>
	   			</div>
			</c:forEach></div>
		
		</div>
		</a:body>
	</c:otherwise>
</c:choose>
<%@ include file="metricOpNav.jsp" %>
<script type="text/javascript">
	function productChange(){
		var date='${model.date}';
		var domain='${model.domain}';
		var product=$('#productId').val();
		var timeRange=${payload.timeRange};
		var newProduct = product.substring(2,product.length);
		
		if(product.charAt(0)=='t'){
			 href = "?date="+date+"&domain="+domain+"&timeRange="+timeRange+"&tag="+newProduct;
		}else{
			 href = "?date="+date+"&domain="+domain+"&product="+newProduct+"&timeRange="+timeRange;
		}
		window.location.href=href;
	}

	$(document).ready(function() {
		<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
			var data = ${item.jsonString};
			graphMetricChart(document.getElementById('${item.id}'), data);
		</c:forEach>
	
		var tag= '${payload.tag}';
		
		if(tag!=null&&tag!=''){
			$('#productId').val('t_'+tag);
		}else{
			var product = '${payload.product}';
			$('#productId').val('p_'+product);
		}
		var hide =${payload.hideNav};
		
		if(hide){
			$('.opNav').slideUp();
			$('#switch').html("显示");
		}
		$('#Dashboard_report').addClass("open active");
		$('#dashbord_metric').addClass("active");
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
</style>
