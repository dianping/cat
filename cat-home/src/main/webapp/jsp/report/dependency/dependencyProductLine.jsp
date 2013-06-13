<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.dependency.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.dependency.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.dependency.Model" scope="request"/>

<a:report title="Dependency Report"
	navUrlPrefix="domain=${model.domain}&op=productLine&productLine=${payload.productLine}">
	<jsp:attribute name="subtitle">From ${w:format(model.reportStart,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.reportEnd,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
	
	<res:useCss value='${res.css.local.table_css}' target="head-css" />
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['startopo.js']}" target="head-js" />
	<res:useJs value="${res.js.local['raphael-min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig.js']}" target="head-js" />
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	
	<div class="report">
		<div class='text-center'>
		<%@ include file="dependencyOpNav.jsp" %>
 		<%@ include file="dependencyTimeNav.jsp"%>
 		</div>
 		<div class="row-fluid">
 			<div class="span2">
 				<div class="well sidebar-nav">
           			 <ul class="nav nav-list">
		 				<c:forEach var="item" items="${model.productLines}" varStatus="status">
					             <li class="text-left" id="tab${item.id}"><a href="?op=productLine&productLine=${item.id}&minute=${model.minute}&domain=${model.domain}&date=${model.date}">${item.title}</a></li>
			            </c:forEach></ul></div></div>
 			<div class="span10">
 				<h4 class='text-center text-error' id="title"></h4>
 				<div class="text-center" id="container" style="width:1000px;height:600px;border:solid 1px #ccc;"></div>
 			</div>
 			</div>
 		</div>
</jsp:body>
</a:report>
<script type="text/javascript">
	$(document).ready(function() {
		$('#minute'+${model.minute}).addClass('disabled');
		$('#tab'+'${payload.productLine}').addClass('active');
		$('.position').hide();
		$('.switch').hide();
		var data = ${model.productLineGraph};
		console.log(data);
		var title = data.id;
		$('#title').html(title);
		function parse(data){
			var nodes = data.nodes;
			var points = [];
			var sides = [];
			for(var o in nodes){
				if(nodes.hasOwnProperty(o)){
					points.push(nodes[o]);
				}
			}
			for(var o in data.edges){
				if(data.edges.hasOwnProperty(o)){
					sides.push(data.edges[o]);
				}
			}
			data.points = points;
			data.sides = sides;
			delete data.nodes;
			delete data.edges;
			return data;
		}
		new StarTopo('container',data,{
			typeMap:{
				database:'rect',
				project:'circle',
				service:'lozenge'
			},
			colorMap:{
				 "1":'#2fbf2f',
				 "2":'#bfa22f',
				 "3":'#b94a48',
				 "4":'#772fbf'
			},
		radius:300,
		sideWeight:function(weight){
			return weight+1
		},
		nodeWeight:function(weight){
			return weight/5+0.8;
		}});
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
