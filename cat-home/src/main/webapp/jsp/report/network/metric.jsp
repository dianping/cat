<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.network.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.network.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.network.Model" scope="request"/>

<c:choose>
	<c:when test="${payload.fullScreen}">
		<res:bean id="res" />
		<res:useCss value='${res.css.local.body_css}' target="head-css" />
		<script src='${model.webapp}/assets/js/jquery.min.js'> </script>
		<res:useJs value="${res.js.local['highcharts.js']}" target="head-js"/>
		<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>
		<a href="javascript:showOpNav()" id="switch" class="btn btn-sm btn-success">隐藏</a>
		<div class="opNav">
		<%@ include file="metricOpNav.jsp" %>
		<table>
			<tr style="text-align: left">
				<th>&nbsp;&nbsp;时间段 
					<c:forEach var="range" items="${model.allRange}">
						<c:choose>
							<c:when test="${payload.timeRange eq range.duration}">
								&nbsp;&nbsp;&nbsp;[ <a href="?op=metric&${navUrlPrefix}&fullScreen=${payload.fullScreen}&date=${model.date}&domain=${model.domain}&product=${payload.product}&timeRange=${range.duration}" class="current">${range.title}</a> ]
							</c:when>
							<c:otherwise>
								&nbsp;&nbsp;&nbsp;[ <a href="?op=metric&${navUrlPrefix}&fullScreen=${payload.fullScreen}&date=${model.date}&domain=${model.domain}&product=${payload.product}&timeRange=${range.duration}">${range.title}</a> ]
							</c:otherwise>
							</c:choose>
					</c:forEach>
				</th>
			</tr>
		</table></div>
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
			<div class="nav-search nav" id="nav-search">
				<c:forEach var="nav" items="${model.navs}">
							&nbsp;[ <a href="${model.baseUri}?op=metric&date=${model.date}&domain=${model.domain}&step=${nav.hours}&product=${payload.product}&timeRange=${payload.timeRange}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
						</c:forEach>
						&nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}&op=metric&product=${payload.product}&timeRange=${payload.timeRange}">now</a> ]&nbsp;
			</div></div>
			<table>
				<tr style="text-align: left">
					<th>
						<div class="navbar-header pull-left position" style="width:350px;">
						<form id="wrap_search" style="margin-bottom:0px;">
						<div class="input-group">
							<span class="input-icon" style="width:300px;">
								<input type="text" placeholder="input device for search" value="${payload.product}" class="search-input search-input form-control ui-autocomplete-input" id="search" autocomplete="off" />
								<i class="ace-icon fa fa-search nav-search-icon"></i>
								</span>
								<span class="input-group-btn" style="width:50px">
								<button class="btn btn-sm btn-primary" type="button" id="search_go">
								Go
								</button>
								</span>
							</div>
						</form>
						</div>
						</th><th>	
						&nbsp;&nbsp;时间段 
						<c:forEach var="range" items="${model.allRange}">
							<c:choose>
								<c:when test="${payload.timeRange eq range.duration}">
									&nbsp;&nbsp;&nbsp;[ <a href="?op=metric&date=${model.date}&domain=${model.domain}&product=${payload.product}&timeRange=${range.duration}" class="current">${range.title}</a> ]
								</c:when>
								<c:otherwise>
									&nbsp;&nbsp;&nbsp;[ <a href="?op=metric&date=${model.date}&domain=${model.domain}&product=${payload.product}&timeRange=${range.duration}">${range.title}</a> ]
								</c:otherwise>
								</c:choose>
						</c:forEach>
					</th>
				</tr>
			</table>
		      <div class="col-xs-12">
		        	<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
		       			<div style="float:left;">
		       				<div id="${item.id}" class="metricGraph"></div>
		       			</div>
					</c:forEach>
			</div>
		</div>
		</a:body>
	</c:otherwise></c:choose>
	
	<script type="text/javascript">
		function networkChange(){
			var date='${model.date}';
			var domain='${model.domain}';
			var network=$("#search").val();
			console.log(network);
			var timeRange=${payload.timeRange};
			var href = "?op=metric&date="+date+"&domain="+domain+"&product="+network+"&timeRange="+timeRange;
			window.location.href=href;
		}
	
		$(document).ready(function() {
			var product = '${payload.product}';
			$('#network').val(product);
			$('i[tips]').popover();
			$('#System_report').addClass("open active");
			$('#system_network').addClass("active");
			
			$.widget( "custom.catcomplete", $.ui.autocomplete, {
				_renderMenu: function( ul, items ) {
					var that = this,
					currentCategory = "";
					$.each( items, function( index, item ) {
						if ( item.category != currentCategory ) {
							ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
							currentCategory = item.category;
						}
						that._renderItemData( ul, item );
					});
				}
			});
			
			var data = [];
			<c:forEach var="item" items="${model.productLines}">
				var item = {};
				item['label'] = '${item.id}';
				item['category'] = '网络监控设备';
				data.push(item);
			</c:forEach>
					
			$( "#search" ).catcomplete({
				delay: 0,
				source: data
			});
			
			$("#search_go").bind("click",function(e){
				networkChange();
			});
			$('#wrap_search').submit(
				function(){
					networkChange();
					return false;
				}		
			);
			
			<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
				var data = ${item.jsonString};
				graphMetricChart(document.getElementById('${item.id}'), data);
			</c:forEach>
		});
	</script>
<style type="text/css">
.input-group .form-control {
	position: static;
}
.input-icon>.ace-icon {
	z-index: 0;
}
</style>
