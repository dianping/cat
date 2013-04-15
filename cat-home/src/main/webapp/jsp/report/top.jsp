<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.top.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.top.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.top.Model" scope="request"/>

<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.0/themes/base/jquery-ui.css" />
<script src="http://code.jquery.com/jquery-1.8.3.js"></script>
<script src="http://code.jquery.com/ui/1.10.0/jquery-ui.js"></script>
<script type="text/javascript">
	/* if(${model.refresh}){
		setTimeout(function refresh(){
			window.location.href="?count=10";
		},10000);		
	} */
	$(document).ready(function() {
		$('#topMetric a').mouseenter(function (e) {
		  e.preventDefault();
		  $(this).tab('show');
		});	
	});
</script>
<style>
.tab-content	table {
  max-width: 100%;
  background-color: transparent;
  border-collapse: collapse;
  border-spacing: 0; 
}
	
</style>
<a:body>
<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
<res:useCss value='${res.css.local.report_css}' target="head-css" />
<res:useCss value='${res.css.local.table_css}' target="head-css" />
<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js"/>

<div class="report">
	<table class="header">
		<tr>
			<td class="title">&nbsp;&nbsp;From ${w:format(model.topReport.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.topReport.endTime,'yyyy-MM-dd HH:mm:ss')}</td>
		<td class="nav" >
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?date=${model.date}&step=${nav.hours}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}">now</a> ]&nbsp;
			</td>
		</tr>
	</table>
	
	<div class="tabbable tabs-left alert-info" id="topMetric"> <!-- Only required for left/right tabs -->
  <ul class="nav nav-tabs">
    <li class="text-right active"><a href="#tab1" data-toggle="tab">异常最多Top10</a></li>
    <li class='text-right'><a href="#tab2" data-toggle="tab">URL最慢Top10</a></li>
    <li class='text-right'><a href="#tab3" data-toggle="tab">Service最慢Top10</a></li>
    <li class='text-right'><a href="#tab4" data-toggle="tab">SQL最慢Top10</a></li>
    <li class='text-right'><a href="#tab5" data-toggle="tab">Call最慢Top10</a></li>
    <li class='text-right'><a href="#tab6" data-toggle="tab">Cache最慢Top10</a></li>
  </ul>
  <div class="tab-content">
    <div class="tab-pane active" id="tab1">
      <c:forEach var="item" items="${model.metrix.error.result}" varStatus="status">
		<table width="20%" style="float:left" border=1>  
				<tr><th colspan="2">${item.key}</th></tr>
				<tr><th width="80%">系统</th>		<th>个</th></tr>
				<c:forEach var="detail" items="${item.value}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td><a href="/cat/r/p?domain=${detail.domain}&date=${w:format(model.topReport.startTime,'yyyyMMddHH')}" target="_blank">${detail.domain}</a></td><td>${w:format(detail.value,'0.0')}</td>
					</tr>
				</c:forEach>
		</table>
		</c:forEach>
    </div>
    <div class="tab-pane" id="tab2">
    	<c:forEach var="item" items="${model.metrix.url.result}" varStatus="status">
		<table width="20%" style="float:left" border=1>  
				<tr><th colspan="2">${item.key}</th></tr>
				<tr><th width="80%">系统</th>		<th>ms</th></tr>
				<c:forEach var="detail" items="${item.value}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td><a href="/cat/r/t?domain=${detail.domain}&date=${w:format(model.topReport.startTime,'yyyyMMddHH')}" target="_blank">${detail.domain}</a></td><td>${w:format(detail.value,'0.0')}</td>
					</tr>
				</c:forEach>
		</table>
	</c:forEach>
    </div>
    <div class="tab-pane" id="tab3">
    	<c:forEach var="item" items="${model.metrix.service.result}" varStatus="status">
		<table width="20%" style="float:left" border=1>  
				<tr><th colspan="2">${item.key}</th></tr>
				<tr><th width="80%">系统</th>		<th>ms</th></tr>
				<c:forEach var="detail" items="${item.value}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td><a href="/cat/r/t?domain=${detail.domain}&date=${w:format(model.topReport.startTime,'yyyyMMddHH')}" target="_blank">${detail.domain}</a></td><td>${w:format(detail.value,'0.0')}</td>
					</tr>
				</c:forEach>
		</table>
	</c:forEach>
    </div>
    <div class="tab-pane" id="tab4">
    	<c:forEach var="item" items="${model.metrix.sql.result}" varStatus="status">
		<table width="20%" style="float:left" border=1>  
				<tr><th colspan="2">${item.key}</th></tr>
				<tr><th width="80%">系统</th>		<th>ms</th></tr>
				<c:forEach var="detail" items="${item.value}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td><a href="/cat/r/t?domain=${detail.domain}&date=${w:format(model.topReport.startTime,'yyyyMMddHH')}" target="_blank">${detail.domain}</a></td><td>${w:format(detail.value,'0.0')}</td>
					</tr>
				</c:forEach>
		</table>
	</c:forEach>
    </div>
    <div class="tab-pane" id="tab5">
    	<c:forEach var="item" items="${model.metrix.call.result}" varStatus="status">
		<table width="20%" style="float:left" border=1>  
				<tr><th colspan="2">${item.key}</th></tr>
				<tr><th width="80%">系统</th>		<th>ms</th></tr>
				<c:forEach var="detail" items="${item.value}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td><a href="/cat/r/t?domain=${detail.domain}&date=${w:format(model.topReport.startTime,'yyyyMMddHH')}" target="_blank">${detail.domain}</a></td><td>${w:format(detail.value,'0.0')}</td>
					</tr>
				</c:forEach>
		</table>
	</c:forEach>
    </div>
    <div class="tab-pane" id="tab6">
    	<c:forEach var="item" items="${model.metrix.cache.result}" varStatus="status">
		<table width="20%" style="float:left" border=1>  
				<tr><th colspan="2">${item.key}</th></tr>
				<tr><th width="80%">系统</th>		<th>ms</th></tr>
				<c:forEach var="detail" items="${item.value}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td><a href="/cat/r/t?domain=${detail.domain}&date=${w:format(model.topReport.startTime,'yyyyMMddHH')}" target="_blank">${detail.domain}</a></td><td>${w:format(detail.value,'0.0')}</td>
					</tr>
				</c:forEach>
		</table>
	</c:forEach>
    </div>
  </div>
	<table  class="footer">
		<tr>
			<td>[ end ]</td>
		</tr>
	</table>
</div>
</a:body>