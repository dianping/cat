<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.dependency.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.dependency.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.dependency.Model" scope="request"/>

<a:hourly_report title="Dependency Report"
	navUrlPrefix="op=lineChart&domain=${model.domain}">
	<jsp:attribute name="subtitle">${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
	
	<div class='report'>
		<div class="tabbable text-danger" id="content"> <!-- Only required for left/right tabs -->
			<%@ include file="dependencyLineGraph.jsp"%>
	  </div>
</jsp:body>
</a:hourly_report>
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
