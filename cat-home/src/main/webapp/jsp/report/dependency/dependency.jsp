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
	<res:useJs value="${res.js.local['svgchart.latest.min.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig.js']}" target="head-js" />
	
	<div class='report'>
		<%@ include file="dependencyOpNav.jsp" %>
		<div class="tabbable text-error" id="content"> <!-- Only required for left/right tabs -->
  			<ul class="nav nav-tabs">
   			 	<li style="margin-left:20px;" class="text-right active"><a id="tab1Href" href="#tab1" data-toggle="tab"><strong>项目指标以及依赖项目数据趋势</strong></a></li>
   			 	<li class="text-right"><a href="#tab2" id="tab2Href" data-toggle="tab"><strong>运维Zabbix告警信息</strong></a></li>
   			 	<li class="text-right"><a href="#tab3" id="tab3Href" data-toggle="tab"><strong>详细数据以及配置</strong></a></li>
  			</ul>
  			<div class="tab-content">
	    		<div class="tab-pane active" id="tab1">
	    			<%@ include file="dependencyLineGraph.jsp"%>
	    		</div>
	    		<div class="tab-pane" id="tab2">
	    			<div class="text-center">
		    			<%@ include file="dependencyTimeNavTab2.jsp" %>
	    			</div>
	  				<%@ include file="dependencyEvent.jsp"%>
	    		</div>
	    		<div class="tab-pane" id="tab3">
	    			 <div class="row-fluid">
				  	    <div class="span2">
				  	    	<a class="btn btn-primary" href="?domain=${model.domain}&date=${model.date}&all=true">当前小时数据汇总</a>
				  	   		<h4 class="text-success">当前数据:<c:if test="${payload.all}">0~60</c:if>
				  			<c:if test="${payload.all == false}">${model.minute}</c:if>分钟</h4>
				  	    </div>
				  	    <div class="span10">
				  	    	<%@ include file="dependencyTimeNavTab3.jsp" %>
				  	    </div>
				  </div>
				  <%@ include file="dependencyDetailData.jsp"%>
	    		</div>
	    	</div></div>
	 
	  </div>
</jsp:body>
</a:report>
<script type="text/javascript">
	$(document).ready(function() {
		var tab = '${payload.tab}';
		if(tab=='tab3'){
			$('#tab3Href').trigger('click');
		}else if(tab=='tab2'){
			$('#tab2Href').trigger('click');
		}else if(tab=='tab1'){
			$('#tab1Href').trigger('click');
		}
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
		$('#otherDependency .nav-tabs a').mouseenter(function (e) {
		  e.preventDefault();
		  $(this).tab('show');
		});	
		$('#zabbixTab0').addClass('active');
		$('#leftTab0').addClass('active');
		$('.switch').css('display','none');
		$('.dataTables_info').css('display','none');
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
	.graph {
		width: 450px;
		height: 200px;
		margin: 4px auto;
	}
</style>
