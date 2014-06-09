<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"
	type="com.dianping.cat.report.page.userMonitor.Context" scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.report.page.userMonitor.Payload" scope="request" />
<jsp:useBean id="model"
	type="com.dianping.cat.report.page.userMonitor.Model" scope="request" />

<a:body>
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useCss value="${res.css.local['bootstrap-datetimepicker.min.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
	<script type="text/javascript">
		function query(){
			var group = $("#group").val();
			var url = $("#url").val();
			var city = $("#city").val();
			var type = $("#type").val();
			var channel = $("#channel").val();
			var start = $("#startTime").val();
			var end = $("#endTime").val();
			
			window.location.href="?url="+url +"&group="+group+ "&city="+city+"&type="+type+"&channel="+channel+"&startDate="+start+"&endDate="+end;
		}
		
		$(document).ready(function() {
			$('#datetimepicker1').datetimepicker();
			$('#datetimepicker2').datetimepicker();
			$('#startTime').val("${w:format(model.start,'yyyy-MM-dd HH:mm')}");
			$('#endTime').val("${w:format(model.end,'yyyy-MM-dd HH:mm')}");
			$('#type').val('${payload.type}');
			$('#channel').val('${payload.channel}');
			$('#url').val('${payload.url}');
			
			var cityData = ${model.cityInfo};
			var select = $('#province');
			
			var urlData = ${model.items};
			var group = $('#group');
			
			function groupChange(){
				var key = $("#group").val();
				var value = urlData[key];
				var url = document.getElementById("url");
				url.length=0;
				for (var prop in value) {
				    var opt = $('<option />');
			  		
			  		opt.html(value[prop].pattern);
				  	opt.val(value[prop].name);
			  		opt.appendTo(url);
				}
			}
			group.on('change',groupChange);
			
			function provinceChange(){
				var key = $("#province").val();
				var value = cityData[key];
				
				select = document.getElementById("city");
				select.length=0;
				for (var prop in value) {
				    var opt = $('<option />');
			  		var city = value[prop].city;
			  		
			  		if(city==''){
				  		opt.html('ALL');
			  		}else{
				  		opt.html(value[prop].city);
			  		}
				  	if(value[prop].province==''){
				  		opt.val('');
				  	}else{
					  	opt.val(value[prop].province+'-'+value[prop].city);
				  	}
			  		opt.appendTo(select);
				}
			}
			select.on('change',provinceChange);
			
			for (var prop in cityData) {
			  	if (cityData.hasOwnProperty(prop)) { 
			  		var opt = $('<option />');
			  		
			  		if(prop==''){
				  		opt.html('ALL');
			  		}else{
				  		opt.html(prop);
			  		}
			  		opt.val(prop);
			  		opt.appendTo(select);
			  }
			}
			
			for (var prop in urlData) {
			  	if (urlData.hasOwnProperty(prop)) { 
			  		var opt = $('<option />');
			  		
			  		opt.val(prop);
			  		opt.html(prop);
			  		opt.appendTo(group);
			  }
			}
			
			var city = '${payload.city}';
			var array = city.split('-');
			
			$('#province').val(array[0]);
			provinceChange();
			
			if(array.length==2){
				$("#city").val(city);
			}
			
			$('#group').val('${payload.group}');
			groupChange();
			$("#url").val(url);
			
			<c:choose>
			<c:when test="${payload.type eq 'info'}">
				<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
					var data = ${item.jsonString};
					graphMetricChart(document.getElementById('${item.id}'), data);
				</c:forEach>
				<c:forEach var="item" items="${model.pieCharts}" varStatus="status">
					var data = ${item.jsonString};
					graphPieChart(document.getElementById('${item.title}'),data);
				</c:forEach>
			</c:when>
			<c:otherwise>
					graphMetricChart(document.getElementById('lineChart'), ${model.lineChart.jsonString});
					graphPieChart(document.getElementById('pieChart'), ${model.pieChart.jsonString});
			</c:otherwise>
			</c:choose>
		});
		</script>
	<div class="report">
		<table>
			<tr>
				<th class="left">
				组<select style="width: 100px;" name="group" id="group">
				</select> URL <select style="width: 400px;" name="url" id="url"></select>
				省份 <select style="width: 100px;" name="province" id="province">
				</select> 城市 <select style="width: 100px;" name="city" id="city">
				</select> 运营商 <select style="width: 120px;" name="channel" id="channel">
						<option value="">ALL</option>
						<option value="中国电信">中国电信</option>
						<option value="中国移动">中国移动</option>
						<option value="中国联通">中国联通</option>
						<option value="中国铁通">中国铁通</option>
						<option value="其他">其他</option>
				</select> 查询类型 <select style="width: 120px;" name="type" id="type">
						<option value="info">访问情况</option>
						<option value="httpStatus">HttpStatus</option>
						<option value="errorCode">Code</option>
				</select>
				</th>
			</tr>
			<tr>
				<th class="right">开始时间
					<div id="datetimepicker1" class="input-append date"
						style="margin-bottom: 0px;">
						<input id="startTime" name="startTime"
							style="height: 30px; width: 150px;"
							data-format="yyyy-MM-dd hh:mm" type="text"></input> <span
							class="add-on"> <i data-time-icon="icon-time"
							data-date-icon="icon-calendar"> </i>
						</span>
					</div> 结束时间
					<div id="datetimepicker2" class="input-append date"
						style="margin-bottom: 0px;">
						<input id="endTime" name="endTime"
							style="height: 30px; width: 150px;"
							data-format="yyyy-MM-dd hh:mm" type="text"></input> <span
							class="add-on"> <i data-time-icon="icon-time"
							data-date-icon="icon-calendar"> </i>
						</span>
					</div> <input class="btn btn-primary "
					value="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
					onclick="query()" type="submit">
				</div>
				</th>
			</tr>
		</table>
		<c:choose>
			<c:when test="${payload.type eq 'info'}">
				<div>
				<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
					<div style="float: left;">
						<div id="${item.id}" style="width:450px; height:380px;"></div>
					</div>
				</c:forEach></div>
				<div>
				<c:forEach var="item" items="${model.pieCharts}" varStatus="status">
					<div style="float: left;">
						<h5 class="text-center">${item.title}</h5>
						<div id="${item.title}" style="width:600px; height:450px;"></div>
					</div>
				</c:forEach></div>
			</c:when>
			<c:otherwise>
				<div class="row-fluid">
					<div class="span6">
						<div id="lineChart" style="width:550px; height:400px;"></div>
					</div>
					<div class="span6">
						<div id="pieChart" style="width:550px; height:400px;"></div>
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
