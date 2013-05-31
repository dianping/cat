<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.dependency.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.dependency.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.dependency.Model" scope="request"/>

<a:report title="Dependency Report"
	navUrlPrefix="domain=${model.domain}">
	<jsp:attribute name="subtitle">From ${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
	
	<res:useCss value='${res.css.local.table_css}' target="head-css" />
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['startopo.js']}" target="head-js" />
	<res:useJs value="${res.js.local['raphael-min.js']}" target="head-js" />
<div class="report">
  <div class='text-center'>
	  <a style="margin-top:18px;" class="btn btn-danger  btn-primary" href="?minute=${model.minute}&domain=${model.domain}&date=${model.date}&all=true">切换到实时趋势图</a>
  </div>
  <div class="row-fluid">
  	<div class="span12  text-center">
		<c:forEach var="item" items="${model.minutes}" varStatus="status">
		<c:if test="${status.index % 30 ==0}">
			<div class="pagination">
			<ul>
		</c:if>
			<c:if test="${item > model.maxMinute }"><li class="disabled" id="minute${item}"><a
			href="?op=graph&domain=${model.domain}&date=${model.date}&minute=${item}">
				<c:if test="${item < 10}">0${item}</c:if>
				<c:if test="${item >= 10}">${item}</c:if></a></li>
			</c:if>
			<c:if test="${item <= model.maxMinute }"><li id="minute${item}"><a
			href="?op=graph&domain=${model.domain}&date=${model.date}&minute=${item}">
				<c:if test="${item < 10}">0${item}</c:if>
				<c:if test="${item >= 10}">${item}</c:if></a></li>
			</c:if>
		<c:if test="${status.index % 30 ==29 || status.last}">
			</ul>
			</div>
		</c:if>
	</c:forEach>
  	</div>
  </div>
  <div class="text-center">
	<div class="text-center" id="container" style="margin-left:200px;width:1000px;height:800px;border:solid 1px #ccc;"></div>
  </div>
  <div class="row-fluid">
  	<div class="span12">
  		<%@ include file="dependencyEvent.jsp"%>
  	</div>
  </div>
</jsp:body>
</a:report>
<script type="text/javascript">
	$(document).ready(function() {
		$('#minute'+${model.minute}).addClass('disabled');
		$('#tab0').addClass('active');
		$('#leftTab0').addClass('active');
		
		var data = ${model.topologyGraph};
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

		new  StarTopo('container',parse(data)
					,{
							typeMap:{
								database:'rect',
								project:'circle',
								service:'lozenge'
							},
							colorMap:{
										 "1":'#7cfc00',
										 "2":'#FFD700',
										 "3":'#f08080'
							},
			radius:300,
			sideWeight:function(weight){
				return weight+1
			},
			nodeWeight:function(weight){
				return weight/5+1;
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
