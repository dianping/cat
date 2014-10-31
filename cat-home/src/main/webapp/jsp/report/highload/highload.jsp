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
			<input class="btn btn-primary  btn-small"  value="查询" onclick="queryNew()" type="submit">
			<br><br>
		</div>
		<div id="SqlReport">
			<table	class="problem table table-striped table-bordered table-condensed table-hover" id="contents">
				<thead>
				<tr class="text-success">
					<th width="8%">domain</th>
					<th width="7%">weight</th>
					<th width="20%">SQL</th>
					<th width="5%">总数</th>
					<th width="5%">错误数</th>
					<th width="7%">失败率</th>
					<th width="4%">Min</th>
					<th width="5%">Max</th>
					<th width="5%">Avg</th>
					<th width="5%">95Line</th>
					<th width="6%">99.9Line</th>
					<th width="5%">Std</th>
					<th width="5%">QPS</th>
				</tr>
				</thead>
				<tbody>
				<c:forEach var="report" items="${model.sqlReports}" varStatus="status">
					<c:set var="e" value="${report.name}"/>
					<tr>
						<td>${report.domain}</td>
						<td>${report.weight}</td>
						<td>${e.id}</td>
						<td>${w:format(e.totalCount,'#,###,###,###,##0')}</td>
						<td>${w:format(e.failCount,'#,###,###,###,##0')}</td>
						<td>&nbsp;${w:format(e.failPercent/100,'0.0000%')}</td>
						<td>${w:format(e.min,'###,##0.#')}</td>
						<td>${w:format(e.max,'###,##0.#')}</td>
						<td>${w:format(e.avg,'###,##0.0')}</td>
						<td>${w:format(e.line95Value,'###,##0.0')}</td>
						<td>${w:format(e.line99Value,'###,##0.0')}</td>
						<td>${w:format(e.std,'###,##0.0')}</td>
						<td>${w:format(e.tps,'###,##0.0')}</td>
					</tr>
				</c:forEach>
				</tbody>
			</table>
		</div></div></div>
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