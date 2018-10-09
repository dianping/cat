<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.dependency.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.dependency.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.dependency.Model" scope="request"/>

<a:hourly_report title="Dependency Report"
	navUrlPrefix="domain=${model.domain}&op=dependencyGraph">
	<jsp:attribute name="subtitle">${w:format(model.reportStart,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.reportEnd,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['startopo.js']}" target="head-js" />
	<res:useJs value="${res.js.local['raphael-min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig.js']}" target="head-js" />
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />

<div class="report">
		<div class="text-center"><%@ include file="dependencyTimeNav.jsp"%> </div>
		<div class="text-center" id="container" style="margin-left:75px;width:1000px;height:800px;border:solid 1px #ccc;"></div>
  	</div>
  		
  </div>
</jsp:body>
</a:hourly_report>
<script type="text/javascript">
	$(document).ready(function() {
		$('.switch').hide();
		$('#dependency_topo').addClass('active');
		$('#zabbixTab0').addClass('active');
		$('#leftTab0').addClass('active');
		$('.contents').dataTable({
			"sPaginationType": "full_numbers",
			'iDisplayLength': 50,
			"bPaginate": false,
			//"bFilter": false,
		});
		$('.contentsDependency').dataTable({
			"sPaginationType": "full_numbers",
			'iDisplayLength': 50,
			"bPaginate": false,
		});
		var data = ${model.topologyGraph};
		var nodeSize = 0;
		function parse(data){
			var nodes = data.nodes;
			var edges = data.edges;
			var points = [];
			var sides = [];

			for(var o in nodes){
				if(nodes.hasOwnProperty(o)){
					points.push(nodes[o]);
					nodeSize++;
				}
			}
			for(var o in edges){
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
		var convertData = parse(data);
		var defaultWeight=0.8;
		if(nodeSize>30){
			defaultWeight = 0.5;
		}else if(nodeSize>20){
			defaultWeight = 0.6;
		}else if(nodeSize>10){
			defaultWeight = 0.8;
		}else if(nodeSize>0){
			defaultWeight = 1.0;
		}
		try{
		new  StarTopo('container',convertData,{
				typeMap:{
					database:'rect',
					project:'circle',
					service:'lozenge',
					cache:'lozenge'
				},
				colorMap:{
					 "1":'#2fbf2f',
					 "2":'#bfa22f',
					 "3":'#b94a48',
					 "4":'#772fbf'
				},
			radius:300,
			sideWeight:function(weight){
				return weight+3
			},
			nodeWeight:function(weight){
				return weight/5+defaultWeight;
			}});
		}catch(e){
			console.log(e);
		}
		var tab = '${payload.tab}';
		if(tab=='tab3'){
			$('#tab3Href').trigger('click');
		}else if(tab=='tab2'){
			$('#tab2Href').trigger('click');
		}else if(tab=='tab1'){
			$('#tab1Href').trigger('click');
		}
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
