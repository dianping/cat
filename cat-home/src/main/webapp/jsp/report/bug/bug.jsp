<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.statistics.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.statistics.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.statistics.Model" scope="request"/>

<a:offline>
<res:useCss value='${res.css.local.table_css}' target="head-css" />

<script type="text/javascript">
	$(document).ready(function() {
		$('#bug_report').addClass('active');
	});
</script>
<div class="breadcrumbs" id="breadcrumbs">
	<script type="text/javascript">
		try{ace.settings.check('breadcrumbs' , 'fixed')}catch(e){}
	</script>
	<span class="text-danger title">【报表时间】</span><span class="text-success">&nbsp;&nbsp;${w:format(model.bugReport.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.bugReport.endTime,'yyyy-MM-dd HH:mm:ss')}</span>
	<div class="nav-search nav" id="nav-search">
		<a href="?op=historyBug&domain=${model.domain}" class="switch"><span class="text-danger">【切到历史模式】</span></a>
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?date=${model.date}&step=${nav.hours}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}">now</a> ]&nbsp;
	</div>
</div>
<div class="row-fluid">
		<div class="report">
			<%@ include file="detail.jsp"%>
		</div>
</div>
</a:offline>
