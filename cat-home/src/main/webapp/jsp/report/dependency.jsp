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
	<res:useJs value="${res.js.local['flotr2_js']}" target="head-js"/>
	<res:useJs value="${res.js.local['baseTools_js']}" target="head-js"/>
	<res:useJs value="${res.js.local['dependencyTrendGraph_js']}" target="head-js"/>

<div class="report">
	<div class="row-fluid">
	<div class="span2 text-center">
		<a style="margin-top:18px;" class="btn btn-primary" href="?domain=${model.domain}&date=${model.date}&all=true">All</a>
		<a style="margin-top:18px;" class="btn btn-danger  btn-primary" href="?op=graph&minute=${model.minute}&domain=${model.domain}&date=${model.date}&all=true">Graph</a>
	</div>
	<div class="span10">
		<c:forEach var="item" items="${model.minutes}" varStatus="status">
		<c:if test="${status.index % 30 ==0}">
			<div class="pagination">
			<ul>
		</c:if>
			<c:if test="${item > model.maxMinute }"><li class="disabled" id="minute${item}"><a
			href="?domain=${model.domain}&date=${model.date}&minute=${item}">
				<c:if test="${item < 10}">0${item}</c:if>
				<c:if test="${item >= 10}">${item}</c:if></a></li>
			</c:if>
			<c:if test="${item <= model.maxMinute }"><li id="minute${item}"><a
			href="?domain=${model.domain}&date=${model.date}&minute=${item}">
				<c:if test="${item < 10}">0${item}</c:if>
				<c:if test="${item >= 10}">${item}</c:if></a></li>
			</c:if>
		<c:if test="${status.index % 30 ==29 || status.last}">
			</ul>
			</div>
		</c:if>
	</c:forEach></div>
	</div>
	</br>
  <div class="row-fluid">
  <div class="span6">
	<table class="table table-striped table-bordered table-condensed">
		<tr>
			<td>Exception</td>
			<td>${model.segment.exceptionCount}</td>
		</tr>
		<tr>
			<td colspan='2'></td>
		</tr>
	</table>
	
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
				<td>${itemValue.totalCount}</td>
				<td>${itemValue.errorCount}</td>
				<td>${w:format(itemValue.errorCount/itemValue.totalCount,'0.0000')}</td>
				<td>${w:format(itemValue.avg,'0.0')}</td>
			</tr>		
		</c:forEach></tbody>
	</table>
	
	</br>
	<table	class="contentsDependency table table-striped table-bordered table-condensed">
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
				<td>${itemValue.totalCount}</td>
				<td>${itemValue.errorCount}</td>
				<td>${w:format(itemValue.errorCount/itemValue.totalCount,'0.0000')}</td>
				<td>${w:format(itemValue.avg,'0.0')}</td>
			</tr>		
		</c:forEach></tbody>
	</table>
	</div>
  <div class="span6">
  			<div class="tabbable"  id="otherDependency">
				  <ul class="nav nav-tabs">
				  	<c:forEach  var="item" items="${model.events}"  varStatus="status" >
						 <li id="leftTab${status.index}" class="text-right"><a href="#tab${status.index}" data-toggle="tab">
						 ${item.key}
						 <c:set var="size" value="${w:size(item.value)}"/>
						 <c:if test="${size > 0 }"><span class='text-error'>(${size})</span></c:if>
					</a></li>
				  	</c:forEach>
				  </ul>
		  	<div class="tab-content">
	    		<c:forEach  var="entry" items="${model.events}"  varStatus="status" >
	    		<c:set var="items" value="${entry.value}"/>
				    <div class="tab-pane" id="tab${status.index}">	
						<table	class="table table-striped table-bordered table-condensed">
				  		<thead>
				  			<tr><th>时间</th>
				  				<th>详情</th>
				  				<th>来源</th>
				  				<th>项目名</th>
				  				<th>IP</th>
				  			</tr>
				  		</thead>
				  		<tbody>
				  			<c:forEach var="item" items="${items}">
				  				<tr><td>${w:format(item.date,'HH:mm')}</td>
				  					<td>
				  						<c:choose>
				  							<c:when test="${not empty item.link}"><a href="${item.link}" target="_blank">${item.subject}</a></c:when>
				  							<c:otherwise>${item.subject}</c:otherwise>
				  						</c:choose>
				  						<i data-content="${item.content}" data-original-title="详情" data-placement="top" data-toggle="popover" class="icon-tags" data-trigger="hover" tips=""></i>
				  					</td>
				  					<td>
				  					<c:choose>
				  						<c:when test="${item.type==1}">运维</c:when>
				  						<c:when test="${item.type==2}">数据库</c:when>
				  						<c:when test="${item.type==3}">CAT</c:when>
				  					</c:choose>
				  					</td>
				  					<td>${item.domain}</td>
				  					<td>${item.ip}</td>
				  				</tr>
				  			</c:forEach>	
					</table></div>
					</c:forEach>
			    </div>
		    </div>
  		</tbody>
  </div>
</div>
</div>
<%@ include file="dependencyLineGraph.jsp"%>
</jsp:body>
</a:report>
<script type="text/javascript">
	$(document).ready(function() {
		$('#minute'+${model.minute}).addClass('disabled');
		$('.contents').dataTable({
			"sPaginationType": "full_numbers",
			'iDisplayLength': 50,
			"bPaginate": false,
			"bFilter": false,
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
		$('i[tips]').popover();
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
