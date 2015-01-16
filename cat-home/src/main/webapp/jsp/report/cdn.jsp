<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>

<jsp:useBean id="ctx" type="com.dianping.cat.report.page.cdn.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.cdn.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.cdn.Model" scope="request"/>

<a:body>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
	
	<script type="text/javascript">
		function query(){
			var province = $("#province").val();
			var city = $("#city").val();
			var cdn = $("#cdn").val();
			var start = $("#startTime").val();
			var end = $("#endTime").val();
			window.location.href="?province="+province+"&city="+city+"&cdn="+cdn+"&startDate="+start+"&endDate="+end;
		}
		
		function proceed(id) {
			var start = "${w:format(model.start,'yyyy-MM-dd HH:mm')}";
			var end = "${w:format(model.end,'yyyy-MM-dd HH:mm')}";
			if ('${payload.cdn}' == 'ALL') {
				window.location.href="?province=${payload.province}&city=${payload.city}&cdn="+id+"&startDate="+start+"&endDate="+end;
			} else if ('${payload.province}' == 'ALL') {
				window.location.href="?province="+id+"&city=${payload.city}&cdn=${payload.cdn}&startDate="+start+"&endDate="+end;
			} else if ('${payload.city}' == 'ALL') {
				window.location.href="?province=${payload.province}&city="+id+"&cdn=${payload.cdn}&startDate="+start+"&endDate="+end;
			}
		}
		
		var cityData = ${model.cityInfo};
		
		function provinceChange(){
			var key = $("#province").val();
			if(key == 'ALL'){
				key = '';
			}
			var value = cityData[key];
			
			select = document.getElementById("city");
			select.length=0;
			
			for (var prop in value) {
			    var opt = $('<option />');
		  		var city = value[prop].city;
			  	
		  		if(city == ''){
		  			opt.val('ALL').html('ALL');
		  		}else{
		  			opt.val(city).html(city);
		  		}
		  		opt.appendTo(select);
			}
		}
	
		function init(){
			for ( var prop in cityData) {
				if (prop == '') {
					$('#province').append("<option value='ALL'>ALL</option>");
				}else{
					$('#province').append("<option value='"+prop+"'>"+prop+"</option>");
				}
			}
		}
		
		$(document).ready(function() {
			$('#startTime').datetimepicker({
				format:'Y-m-d H:i',
				step:30,
				maxDate:0
			});
			$('#endTime').datetimepicker({
				format:'Y-m-d H:i',
				step:30,
				maxDate:0
			});			
			$('#startTime').val("${w:format(model.start,'yyyy-MM-dd HH:mm')}");
			$('#endTime').val("${w:format(model.end,'yyyy-MM-dd HH:mm')}");
			init();
			$('#System_report').addClass('active open');
			$('#system_cdn').addClass('active');
			$('#province').on('change',provinceChange);
			
			var province = '${payload.province}';
			var city = '${payload.city}';
			var cdn = '${payload.cdn}';
			if(cdn == '') {
				cdn = 'ALL';
			}
			if(province == '') {
				province = 'ALL';
			}
			if(city == '') {
				city = 'ALL';
			}
			$('#cdn').val(cdn);
			$('#province').val(province);
			provinceChange();
			
			if(city != ''){
				$("#city").val(city);
			}
			
			<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
			var data = ${item.jsonString};
			graphMetricChart(document.getElementById('${item.id}'), data);
			</c:forEach>
		});
	</script>
	<div class="report">
		<table>
			<tr>
				<th class="left">
				<div style="float:left;">
					&nbsp;开始
					<input type="text" id="startTime" style="width:150px;"/>
					结束
					<input type="text" id="endTime" style="width:150px;"/></div>
				&nbsp;cdn服务商 <select style="width: 120px;" name="cdn" id="cdn">
						<option value="ALL">ALL</option>
						<option value="WangSu">网宿</option>
						<option value="DiLian">帝联</option>
						<option value="TengXun">腾讯</option>
						</select>
				省份 <select style="width: 100px;" name="province" id="province">
				</select> 城市 <select style="width: 100px;" name="city" id="city">
				</select> 
				</th>
				<th class="right">
				 &nbsp;&nbsp;
				 <input class="btn btn-primary btn-sm"
					value="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
					onclick="query()" type="submit">
				</div>
				</th>
			</tr>
		</table>
				<div>
				<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
					<div style="float: left; text-align: center;">
						<div style="margin: 20px 0 0 0; cursor: pointer;"><a onclick="proceed('${item.id}');">${item.id}</a></div>
						<div id="${item.id}" style="width:450px; height:380px;"></div>
					</div>
				</c:forEach></div>
	</div>
</a:body>