<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.crash.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.crash.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.crash.Model" scope="request" />
<a:mobile>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<res:useCss value='${res.css.local.table_css}' target="head-css" />
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>
		<div id="queryBar">
		<table>
		<tr>
	      <td>
	        <div style="float:left;">
	         	 &nbsp;&nbsp;日期
				<input type="text" id="time" style="width:100px;" value="<fmt:formatDate value='${payload.dayDate}' pattern='yyyy-MM-dd'/>"/>
	       		 &nbsp;&nbsp;App&nbsp;&nbsp;
	        	<select id="app">
	      	 		<c:forEach var="item" items="${model.appNames}">
	        			<option value="${item.id}">${item.value}</option>
	        		</c:forEach>
	        	</select>
	      		&nbsp;&nbsp;平台&nbsp;&nbsp;
	      		<select id="platform" style="width:100px;">
	  	       		<option value="1">Android</option>
	  	        	<option value="2">Ios</option>
	      		</select>
			</div>
		 </td>
         <td>&nbsp;&nbsp;<input class="btn btn-primary  btn-sm"  value="查询" onclick="query()" type="submit">
		 </td>
	    </tr></table>
		</div>
		<br/>
		<table class="table table-striped table-condensed table-hover table-bordered" style="width:100%">
			<thead>
				<tr align="center">
					<td width="5%"></td>
					<td width="10%"><a href="/cat/r/crash?op=crashStatistics&platform=${payload.platform}&day=${payload.day}&appId=${payload.appId}&sort=version">版本号</a></td>
					<td><a href="/cat/r/crash?op=crashStatistics&platform=${payload.platform}&day=${payload.day}&appId=${payload.appId}&sort=count">crash次数</a></td>
					<td><a href="/cat/r/crash?op=crashStatistics&platform=${payload.platform}&day=${payload.day}&appId=${payload.appId}&sort=countMoM">次数环比(%)</a>			
						<i data-rel="tooltip" data-placement="left" title="当天crash次数/前一天crash次数" class="glyphicon glyphicon-question-sign" ></i>&nbsp;&nbsp;
					</td>
					<td><a href="/cat/r/crash?op=crashStatistics&platform=${payload.platform}&day=${payload.day}&appId=${payload.appId}&sort=countYoY">次数同比(%)</a>
						<i data-rel="tooltip" data-placement="left" title="当天crash次数/上周同一天crash次数" class="glyphicon glyphicon-question-sign" ></i>&nbsp;&nbsp;
					</td>
					<td><a href="/cat/r/crash?op=crashStatistics&platform=${payload.platform}&day=${payload.day}&appId=${payload.appId}&sort=dau">启动用户数</a></td>
					<td><a href="/cat/r/crash?op=crashStatistics&platform=${payload.platform}&day=${payload.day}&appId=${payload.appId}&sort=percent">crash率(%)</a></td>
					<td><a href="/cat/r/crash?op=crashStatistics&platform=${payload.platform}&day=${payload.day}&appId=${payload.appId}&sort=percentMoM">crash率环比(%)</a>
						<i data-rel="tooltip" data-placement="left" title="当天crash率/前一天crash率" class="glyphicon glyphicon-question-sign" ></i>&nbsp;&nbsp;
					</td>
					<td><a href="/cat/r/crash?op=crashStatistics&platform=${payload.platform}&day=${payload.day}&appId=${payload.appId}&sort=percentYoY">crash率同比(%)</a>
						<i data-rel="tooltip" data-placement="left" title="当天crash率/上周同一天crash率" class="glyphicon glyphicon-question-sign" ></i>&nbsp;&nbsp;
					</td>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="entry" items="${model.versions}" varStatus="status">
					<tr>
						<td><span id="${entry.id}" class="ui-icon ace-icon fa fa-plus center bigger-110 blue showdetail"></span></td>
						<td align="center"><a href="/cat/r/crash?op=appCrashLog&crashLogQuery.day=${payload.day}&crashLogQuery.startTime=00:00&crashLogQuery.endTime=23:59&crashLogQuery.appName=${payload.appId}&crashLogQuery.platform=${payload.platform}&crashLogQuery.dpid=&crashLogQuery.query=${entry.id};;;;" target="_blank">${entry.id}</a></td>
						<td align="right">${entry.crashCount}</td>
						
						<td align="right">${w:format(entry.crashCountMoM,'#0.0000')}%</td>
						<td align="right">${w:format(entry.crashCountYoY,'#0.0000')}%</td>
						<td align="right">${entry.dau}</td>
						<td align="right">${w:format(entry.percent,'#0.0000')}%</td>
						<td align="right">${w:format(entry.percentMoM,'#0.0000')}%</td>
						<td align="right">${w:format(entry.percentYoY,'#0.0000')}%</td>
					</tr>
					<tr id="detail-${entry.id}" style="display:none">
						<td></td>
						<td colspan="4">
							<table class="table table-striped table-condensed table-hover table-bordered">
							<thead>
								<tr>
									<th>模块</th>
									<th>crash次数</th>
								</tr>
							</thead>
							<c:forEach var="module" items="${entry.modules}">
								<tr>
									<td>${module.id}</td>
									<td>${module.crashCount}</td>
								</tr>
							</c:forEach>
							</table>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
		
		<script type="text/javascript">
		  $(document).ready(function(){
			  $('[data-rel=tooltip]').tooltip();
			  $('#App_report').addClass("active open");
			  $('#crashStatistics').addClass("active");
			  $('#time').datetimepicker({
					format:'Y-m-d',
					timepicker:false,
					maxDate:0
				});
			
			  $('#app').val("${payload.appId}");
			  $('#platform').val("${payload.platform}");

	      });
		  
		  $( document ).on("click", ".showdetail", function(){ 
			  var item = document.getElementById("detail-"+this.id);
			  if (item.style.display == 'none') {
			  	item.style.display = '';
			 	this.className = "ui-icon ace-icon fa fa-minus center bigger-110 blue showdetail";
			  } else {
				 item.style.display = 'none';
				 this.className = "ui-icon ace-icon fa fa-plus center bigger-110 blue showdetail";
			  }
			}); 
	      
	      function query(){
	      	var app = $('#app').val();
	        var time = $("#time").val();
	        var platform = $("#platform").val();
	        
	        window.location.href = "?op=crashStatistics&appId=" + app + "&day=" + time + "&platform=" + platform;
	     }
	      
	      
		</script>
</a:mobile>