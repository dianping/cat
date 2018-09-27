<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.statistics.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.statistics.Model" scope="request" />

<a:application>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<res:useCss value='${res.css.local.table_css}' target="head-css" />
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['tableInit.js']}" target="head-js"/>
		<div id="queryBar">
	        <div style="float:left;">
				&nbsp;日期
			<input type="text" id="time" style="width:100px;" value="<fmt:formatDate value='${payload.day}' pattern='yyyy-MM-dd'/>"/>
			</div>
			&nbsp;&nbsp;&nbsp;&nbsp;<input class="btn btn-primary  btn-sm"  value="查询" onclick="queryNew()" type="submit">
		</div>
		<br>
		<%@ include file="clientDetail.jsp"%>
		<script type="text/javascript">
		  $(document).ready(function(){
			  init();
			  $('#Offline_report').addClass('active open');
			  $('#client_report').addClass("active");
			  $('#time').datetimepicker({
					format:'Y-m-d',
					timepicker:false,
					maxDate:0
				});
	      });
	      
	      function queryNew(){
	        var time=$("#time").val();
	        window.location.href="?op=client&day="+time;
	      }
		</script>
</a:application>