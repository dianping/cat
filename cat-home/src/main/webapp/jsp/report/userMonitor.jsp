<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.userMonitor.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.userMonitor.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.userMonitor.Model" scope="request"/>

<a:body>
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#url").select2();
			$("#city").select2();

			$('#datetimepicker1').datetimepicker();
			$('#datetimepicker2').datetimepicker();
			
			
			<c:choose>
			<c:when test="${payload.type eq 'info'}">
				<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
					var data = ${item.jsonString};
					graphMetricChart(document.getElementById('${item.id}'), data);
				</c:forEach>
			</c:when>
			<c:otherwise>
					graphMetricChart(document.getElementById('lineChart'), ${model.lineChart.jsonString});
					graphPieChart(document.getElementById('pieChart'), ${model.pieChart.jsonString});
			</c:otherwise>
			</c:choose>
		});
		
		function query(){
			var url = $("#url").val();
			var city = $("#city").val();
			var type = $("#type").val();
			var start = $("#startTime").val();
			var end = $("#endTime").val();
			
			window.location.href="?url="+url +"&city="+city+"&type="+type+"&startDate="+start+"&endDate="+end;
		}
		</script>
		
	<div class="report">
		<table>
			<tr>
				<th class="left">
					URL:
					<select name="url" id="url">
	                     <c:forEach var="item" items="${model.pattermItems}">
	                           <option value="${item.pattern}">${item.pattern}</option>
	                     </c:forEach>
                 	 </select>
                 	 城市
                 	 <select style="width:200px;" name="city" id="city">
	                     <c:forEach var="item" items="${model.cities}">
	                           <option value="${item}">${item}</option>
	                     </c:forEach>
                 	 </select>
                 	 运营商
                 	 <select style="width:120px;" name="type" id="type">
	                           <option value="">ALL</option>
	                           <option value="中国电信">中国电信</option>
	                           <option value="中国移动">中国移动</option>
	                           <option value="中国联通">中国联通</option>
	                           <option value="中国铁通">中国铁通</option>
                 	 </select>
                 	 类型
                 	 <select style="width:120px;" name="type" id="type">
	                           <option value="hit">访问量</option>
	                           <option value="HttpCode">HttpStatus</option>
	                           <option value="ErrorCode">ErrorCode</option>
                 	 </select>
                 	 </th>
                 	 </tr>
                 	 <tr><th  class="left">
                 	 开始时间
                 	 <div id="datetimepicker1" class="input-append date" style="margin-bottom:0px;">
			           <input id="startTime" name="startTime" style="height:30px;width:150px;" placeholder="开始时间"  
			              data-format="yyyy-MM-dd HH:mm" type="text"></input> <span class="add-on"> <i
			              data-time-icon="icon-time" data-date-icon="icon-calendar"> </i>
			           </span>
			       	 </div>
                 	 结束时间
                 	 <div id="datetimepicker2" class="input-append date"  style="margin-bottom:0px;">
			           <input id="endTime"  name="endTime" style="height:30px;width:150px;"  placeholder="结束时间"  
			              data-format="yyyy-MM-dd HH:mm" type="text"></input> <span class="add-on"> <i
			              data-time-icon="icon-time" data-date-icon="icon-calendar"> </i>
			           </span>
			        </div>
			        
			        <input class="btn btn-primary  btn-small"  value="查询"
						onclick="query()"
						type="submit"></div>
				</th>
			</tr>
		</table>
		<c:choose>
			<c:when test="${payload.type eq 'info'}">
				<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
		   			<div style="float:left;">
		   				<div id="${item.id}" style="width:450px;height:380px;"></div>
		   			</div>
				</c:forEach>
			</c:when>
			<c:otherwise>
			<div class="row-fluid">
				<div class="span6">
					<div id="lineChart" style="width:550px;height:400px;"></div>
				</div>
				<div class="span6">
					<div id="pieChart" style="width:550px;height:400px;"></div>
				</div>
			</div>
			</c:otherwise>
		</c:choose>
		
		<table class="footer">
			<tr>
				<td>[ end ]</td>
			</tr>
		</table>
	</div>
</a:body>
