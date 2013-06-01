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
	<div class='report'>
		<div class="row-fluid">
			<div class="span12 text-center">
				<a style="margin-top:18px;" class="btn btn-danger  btn-primary" href="?op=graph&minute=${model.minute}&domain=${model.domain}&date=${model.date}&all=true">切换到实时拓扑图</a>
			</div>
		</div>
	<%@ include file="dependencyLineGraph.jsp"%>
	</br>
  	<div class="row-fluid">
	  <div class="span12">
	  		<%@ include file="dependencyEvent.jsp"%>
	  </div>
	</div>
	  <div class="row-fluid">
	  	    <div class="span2">
	  	    	<a class="btn btn-primary" href="?domain=${model.domain}&date=${model.date}&all=true">当前小时数据汇总</a>
	  	   		<h4 class="text-success">当前数据:<c:if test="${payload.all}">0~60</c:if>
	  			<c:if test="${payload.all == false}">${model.minute}</c:if>分钟</h4>
	  	    </div>
	  	    <div class="span10">
	  	    	<%@ include file="dependencyHeader.jsp" %>
	  	    </div>
	  </div>
	  <div class="row-fluid">
	  		<div class="span6">
	  			<h5 class="text-error text-center">项目本身详细数据</h5>
	  			<table	class="contents table table-striped table-bordered table-condensed">
					<thead>	<tr>
						<th>Name</th>
						<th>Total Count</th>
						<th>Failure Count</th>
						<th>Failure%</th>
						<th>Avg(ms)</th>
					</tr></thead><tbody>
					<c:forEach var="item" items="${model.segment.indexs}"
											varStatus="status">
						 <c:set var="itemKey" value="${item.key}" />
						 <c:set var="itemValue" value="${item.value}" />
						<tr>
							<td>${itemValue.name}</td>
							<td style="text-align:right;">${itemValue.totalCount}</td>
							<td style="text-align:right;">${itemValue.errorCount}</td>
							<td style="text-align:right;">${w:format(itemValue.errorCount/itemValue.totalCount,'0.0000')}</td>
							<td style="text-align:right;">${w:format(itemValue.avg,'0.0')}</td>
						</tr>		
					</c:forEach></tbody>
				</table>
	  		</div>
	  		<div class="span6">
	  			<h5 class="text-error text-center">依赖项目详细数据</h5>
				<table class="contentsDependency table table-striped table-bordered table-condensed">
					<thead>	<tr>
						<th>Type</th>
						<th>Target</th>
						<th>Total Count</th>
						<th>Failure Count</th>
						<th>Failure%</th>
						<th>Avg(ms)</th>
					</tr></thead><tbody>
					<c:forEach var="item" items="${model.segment.dependencies}"
											varStatus="status">
						 <c:set var="itemKey" value="${item.key}" />
						 <c:set var="itemValue" value="${item.value}" />
						<tr>
							<td>${itemValue.type}</td>
							<td>${itemValue.target}</td>
							<td style="text-align:right;">${itemValue.totalCount}</td>
							<td style="text-align:right;">${itemValue.errorCount}</td>
							<td style="text-align:right;">${w:format(itemValue.errorCount/itemValue.totalCount,'0.0000')}</td>
							<td style="text-align:right;">${w:format(itemValue.avg,'0.0')}</td>
						</tr>		
					</c:forEach></tbody>
				</table>	  			
	  		</div>
	  </div>
	  </div>
</jsp:body>
</a:report>
<script type="text/javascript">
	$(document).ready(function() {
		$('#minute'+${model.minute}).addClass('disabled');
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
		$('#tab0').addClass('active');
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
