<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.app.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.app.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.app.Model" scope="request" />

<a:body>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<res:useCss value='${res.css.local.table_css}' target="head-css" />
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>
		<div id="queryBar">
	        <div style="float:left;">
				&nbsp;日期
			<input type="text" id="time" style="width:100px;" value="<fmt:formatDate value='${payload.dayDate}' pattern='yyyy-MM-dd'/>"/>
			</div>
			&nbsp;&nbsp;&nbsp;&nbsp;<input class="btn btn-primary  btn-sm"  value="查询" onclick="queryNew()" type="submit">
			&nbsp;&nbsp;&nbsp;&nbsp;
		</div>
		<br>
		<%@ include file="statisticsDetail.jsp"%>
		<script type="text/javascript">
		  $(document).ready(function(){
			  $('[data-rel=tooltip]').tooltip();
			  $('#statistics').addClass("active");
			  $('#time').datetimepicker({
					format:'Y-m-d',
					timepicker:false,
					maxDate:0
				});
			  
			  var type='${payload.type}';
			  if(type == 'request' || type.length==0){
				  type='all';
			  }
			  $("#li-"+type).addClass("active");
			  $("#tabContent-"+type).addClass("active");
	      });
	      
	      function queryNew(){
	        var time=$("#time").val();
	        window.location.href="?op=statistics&day="+time+"&domain=${model.domain}&type=${payload.type}";
	      }
		</script>
</a:body>