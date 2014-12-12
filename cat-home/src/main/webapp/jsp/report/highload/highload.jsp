<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.highload.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.highload.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.highload.Model" scope="request" />

<a:offline>
	<link rel="stylesheet" href="${model.webapp}/assets/css/bootstrap-datetimepicker.css">
	<script src="${model.webapp}/assets/js/bootstrap.datetimepicker.min.js" type="text/javascript"></script>
		<div id="queryBar">
			<div id="datePicker" class="input-append date" style="margin-bottom: 0px;float:left;">
	           日期
	           <span>&nbsp;&nbsp;&nbsp;&nbsp;<input id="time" name="time"  size="16" 
	              data-format="yyyy-MM-dd hh:mm" value="<fmt:formatDate value='${payload.date}' pattern='yyyy-MM-dd'/>"  type="text"/>
	            <span class="add-on"><i class="ace-icon fa fa-calendar"></i></span></span>
	        </div>
			&nbsp;&nbsp;&nbsp;&nbsp;<input class="btn btn-primary  btn-sm"  value="查询" onclick="queryNew()" type="submit">
		</div>
		<div class="report">
			<%@ include file="detail.jsp"%>
		</div>
		<script type="text/javascript">
		  $(document).ready(function(){
			  $('#highload_report').addClass("active");
	          $('#datePicker').datetimepicker({format: 'yyyy-MM-dd'});
	      });
	      
	      function queryNew(){
	        var time=$("#time").val();
	        window.location.href="?op=view&date="+time;
	      }
		</script>
</a:offline>