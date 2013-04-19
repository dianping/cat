<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.metric.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.metric.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.metric.Model" scope="request"/>

<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.0/themes/base/jquery-ui.css" />
<script src="http://code.jquery.com/jquery-1.8.3.js"></script>
<script src="http://code.jquery.com/ui/1.10.0/jquery-ui.js"></script>
<a:body>
<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
<res:useCss value='${res.css.local.report_css}' target="head-css" />
<res:useCss value='${res.css.local.table_css}' target="head-css" />
<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['flotr2_js']}" target="head-js"/>
<res:useJs value="${res.js.local['metric.js']}" target="head-js"/>
<style type="text/css">
.graph {
	width: 380px;
	height: 200px;
	margin: 4px auto;
}
.row-fluid .span2{
	width:9%;
}
</style>
<script type="text/javascript">
	$(document).ready(function() {
		<c:forEach var="item" items="${model.display.groups}" varStatus="status">
			var data = ${item.jsonString};
			graph(document.getElementById('${item.title}'), data);
		</c:forEach>
		

		/* <c:forEach var="item" items="${model.display.conversionRates}" varStatus="status">
			var data = ${item.jsonString};
			graph(document.getElementById('${item.title}'), data);
		</c:forEach> */
		
	});
	
</script>
<div class="report">
	<table class="header">
		<tr>
			<td class="title">&nbsp;&nbsp;From ${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</td>
		<td class="nav">
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?date=${model.date}&step=${nav.hours}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}">now</a> ]&nbsp;
			</td>
		</tr>
	</table>
	</br>
	<div class="container-fluid">
      <div class="row-fluid">
        <div class="span2">
          <div class="well sidebar-nav">
            <ul class="nav nav-list">
              <li class="nav-header active"><a href="#">团购</a></li>
              <li><a href="?date=${model.date}&group=${model.group}&channel=1">团800</a></li>
              <li><a href="#">搜索引擎</a></li>
            </ul>
          </div><!--/.well -->
        </div><!--/span-->
        <div class="span10">
        	<c:forEach var="item" items="${model.display.groups}" varStatus="status">
       			<div style="float:left;" id="${item.title}" class="graph"></div>
			</c:forEach>
        </div>
        <%-- <div class="span10 offset1">
       		<h3 class='text-center'>页面实时转化率</h3>
        	<c:forEach var="item" items="${model.display.conversionRates}" varStatus="status">
        			<div style="float:left;" id="${item.title}" class="graph"></div>
			</c:forEach>
		</div> --%>
	<table  class="footer">
		<tr>
			<td>[ end ]</td>
		</tr>
	</table>
</div>
</a:body>