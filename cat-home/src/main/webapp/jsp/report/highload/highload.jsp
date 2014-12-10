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

<a:body>
	<res:useCss value="${res.css.local['bootstrap-datetimepicker.min.css']}" target="head-css" />
	<res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
	<div style="height:24px"></div>
   <div class="row-fluid">
     <div class="span2">
		<%@include file="../reportTree.jsp"%>
	 </div>
	 <div class="span10">
		<div id="queryBar">
			<div class="text-left"></div>
			日期： &nbsp;
			<div id="datePicker" class="input-append date" >
				<input name="time" id="time" style="height:auto; width: 150px;" 
				value="<fmt:formatDate value="${payload.date}" pattern="yyyy-MM-dd"/>" type="text"></input> 
				<span class="add-on"> <i data-time-icon="icon-time" data-date-icon="icon-calendar"></i> </span>
			</div>&nbsp;&nbsp;
			<input class="btn btn-primary  btn-sm"  value="查询" onclick="queryNew()" type="submit">
		</div>
		<div class="report">
			<%@ include file="detail.jsp"%>
		</div>
	</div>
  </div>
		<script type="text/javascript">
		  $(document).ready(function(){
			  $('#highload').addClass("active");
	          $('#datePicker').datetimepicker({format: 'yyyy-MM-dd'});
	      });
	      
	      function queryNew(){
	        var time=$("#time").val();
	        window.location.href="?op=view&date="+time;
	      }
		</script>
</a:body>