<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.dependency.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.dependency.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.dependency.Model" scope="request"/>

<a:report title="Dependency Report"
	navUrlPrefix="domain=${model.domain}&op=dependencyGraph">
	<jsp:attribute name="subtitle">From ${w:format(model.reportStart,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.reportEnd,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
	
	<res:useCss value='${res.css.local.table_css}' target="head-css" />
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['startopo.js']}" target="head-js" />
	<res:useJs value="${res.js.local['raphael-min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig.js']}" target="head-js" />
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	
<div class="report">
	<div class="row-fluid">
 		<div class="span12 text-center">
		<%@ include file="dependencyOpNav.jsp"%>
	    <%@ include file="dependencyTimeNav.jsp"%>
	    </div></div>
  		<div class="tabbable tabs-left "  > <!-- Only required for left/right tabs -->
  			<ul class="nav nav-tabs alert-info">
   			 	<li style="margin-left:20px;" class="text-right active"><a href="#tab1" data-toggle="tab"><strong>依赖拓扑</strong></a></li>
   			 	<li class="text-right"><a href="#tab2" data-toggle="tab"><strong>运维告警</strong></a></li>
   			 	<li class="text-right"><a href="#tab3" data-toggle="tab"><strong>数据配置</strong></a></li>
  			</ul>
  			<div class="tab-content">
	    		<div class="tab-pane active" id="tab1">
	    			<div class="text-center" id="fullScreenData">
						<div class="text-center" id="container" style="margin-left:75px;width:1000px;height:800px;border:solid 1px #ccc;"></div>
					  </div>
	    		</div>
	    		<div class="tab-pane" id="tab2">
	    			<div>
		  				<%@ include file="dependencyEvent.jsp"%>
	    			</div>
	    		</div>
	    		<div class="tab-pane" id="tab3">
	  				<%@ include file="dependencyDetailData.jsp"%>
	    		</div>
  			</div>
  	</div>
  		
  </div>
</jsp:body>
</a:report>
<script type="text/javascript">
	$(document).ready(function() {
		/* $('#content .nav-tabs a').mouseenter(function (e) {
			  e.preventDefault();
			  $(this).tab('show');
		}); */
	
		$('#minute'+${model.minute}).addClass('disabled');
		$('#minute'+${model.minute}).addClass('text-error');
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
		console.log(nodeSize+" "+defaultWeight);
		new  StarTopo('container',convertData,{
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
				return weight/5+defaultWeight;
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
