<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.system.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.system.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.system.Model" scope="request" />

<a:body>
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useCss value="${res.css.local['bootstrap-datetimepicker.min.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
	<script type="text/javascript">
		function query() {
			var productLine = $("#productLine").val();
			var domain = $("#domain").val();
			var type = $("#type").val();
			var start = $("#startTime").val();
			var end = $("#endTime").val();
			var ipAddrs = '';
			
			if("${model.ipAddrs}" != "[]") {
				ipAddrs = "${model.ipAddrs}".replace(/[\[\]]/g,'').split(', ');
			}
			for( var i=0; i<ipAddrs.length; i++){
			 	var ip = "ip_" + ipAddrs[i];
				if(document.getElementById(ip).checked == false){
					document.getElementById("ipAll").checked = false;
					break;
				} 
			}
			
			var curIpAddrs = '';
			var num = 0;
			if(document.getElementById("ipAll").checked == false) {
				for( var i=0; i<ipAddrs.length; i++){
				 	var ip = "ip_" + ipAddrs[i];
					if(document.getElementById(ip).checked){
						curIpAddrs += ipAddrs[i] + "_";
						num ++;
					} 
				}
				if(num == ipAddrs.length) {
					curIpAddrs = "All";
					document.getElementById("ipAll").checked = true;
				}else{
					curIpAddrs = curIpAddrs.substring(0, curIpAddrs.length-1);
				}
			}else{
				curIpAddrs = "All";
			}
			
			window.location.href = "?productLine=" + productLine + "&domain=" + domain + "&type=" + type 
					+ "&ipAddrs=" + curIpAddrs + "&startDate=" + start + "&endDate="
					+ end; 
		}
		
		function queryAll() {
			var productLine = $("#productLine").val();
			var domain = $("#domain").val();
			var type = $("#type").val();
			var start = $("#startTime").val();
			var end = $("#endTime").val();
			var ipAddrs = '';
			
			if("${model.ipAddrs}" != "[]"){
				var ipAddrs = "${model.ipAddrs}".replace(/[\[\]]/g,'').split(', ');
			}
			
			for( var i=0; i<ipAddrs.length; i++){
			 	var ip = "ip_" + ipAddrs[i];
				document.getElementById(ip).checked = document.getElementById("ipAll").checked;
			}
			
			var curIpAddrs = '';
			if(document.getElementById("ipAll").checked == true) {
				curIpAddrs = "All";
			}else{
				for( var i=0; i<ipAddrs.length; i++){
				 	var ip = "ip_" + ipAddrs[i];
					if(document.getElementById(ip).checked){
						curIpAddrs += ipAddrs[i] + "_";
					} 
				}
				curIpAddrs = curIpAddrs.substring(0, curIpAddrs.length-1);
			}
			
			window.location.href = "?productLine=" + productLine + "&domain=" + domain + "&type=" + type 
					+ "&ipAddrs=" + curIpAddrs + "&startDate=" + start + "&endDate="
					+ end; 
		}

		$(document).ready(
				function() {
					$('#datetimepicker1').datetimepicker().on('hide', function(ev){
						var timestamp = $("#datetimepicker2").data("datetimepicker").getDate().valueOf();
						if (ev.date.valueOf() > timestamp){
				        	alert("结束时间不能晚于结束时间！");
				        	$("#startTime").val($("#endTime").val());
				        	} 
					});
					$('#datetimepicker2').datetimepicker().on('hide', function(ev){
						var timestamp = $("#datetimepicker1").data("datetimepicker").getDate().valueOf();
						if (ev.date.valueOf() < timestamp){
				        	alert("结束时间不能早于开始时间！");
				        	$("#endTime").val($("#startTime").val());
				        	} 
					});
					
					$('#startTime').val("${w:format(model.startTime,'yyyy-MM-dd HH:mm')}");
					$('#endTime').val("${w:format(model.endTime,'yyyy-MM-dd HH:mm')}");
					$('#type').val('${payload.type}');
					$('#domain').val('${payload.domain}');
					$('#productLine').val('${payload.productLine}');
					
					var curIpAddrs = '';
					if("${payload.ipAddrs}" == ''){
						document.getElementById("ipAll").checked = false;
					}else if("${payload.ipAddrs}" == 'All'){
						if("${model.ipAddrs}" != "[]"){
							curIpAddrs = "${model.ipAddrs}".replace(/[\[\]]/g,'').split(', ');
							document.getElementById("ipAll").checked = true;
						}
					}else{
						var curIpAddrStr = "${payload.ipAddrs}";
						curIpAddrs = curIpAddrStr.split("_");
					}
					for(var i=0; i<curIpAddrs.length; i++) {
						document.getElementById("ip_" + curIpAddrs[i]).checked = true;
					}
					
					var projectsInfo = ${model.projectsInfo};
					var productSelect = $('#productLine');
					function change() {
						var productLine = $("#productLine").val();
						var projects;
						if(productLine == 'All') {
							projects = new Array();
							for(var key in projectsInfo) {
								for(var subKey in projectsInfo[key]) {
									projects.push(projectsInfo[key][subKey]);
								}
							}
						} else {
							projects = projectsInfo[productLine];
						}
						select = document.getElementById("domain");
						select.length = 0;

						for ( var prop in projects) {
							if (projects.hasOwnProperty(prop)) {
								var opt = $('<option />');
								var domain = projects[prop];
								opt.html(domain);
								opt.val(domain);
								opt.appendTo(select);
							}
						}
						$("#domain").select2();
					}
					productSelect.on('change', change);

					for ( var prop in projectsInfo) {
						if (projectsInfo.hasOwnProperty(prop)) {
							var opt = $('<option />');
							
							opt.html(prop);
							opt.val(prop);
							opt.appendTo(productSelect);
						}
					}

					var productLine = '${payload.productLine}';
					if(productLine != ''){
						$('#productLine').val(productLine);
					}
					change();
					
					var domain = '${payload.domain}';
					if(domain != ''){
						$('#domain').val(domain);
					}
					
					var type = '${payload.type}';
					if(type != ''){
						$('#type').val(type);
					}
					
					$("#productLine").select2();
					$("#domain").select2();
					$("#type").select2();
					
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
					业务线
					<select style="width: 200px;" name="productLine" id="productLine" >
					<option value="All">All</option>
					</select>
					项目
					<select style="width: 200px;" name="domain" id="domain" ></select> 
					查询类型
					<select style="width: 100px;" name="type" id="type" >
							<option value="system">系统</option>
							<option value="jvm">JVM</option>
							<option value="nginx">Nginx</option>
					</select>
				</th>

				<th class="right">开始时间
					<div id="datetimepicker1" class="input-append date" style="margin-bottom: 0px;">
						<input id="startTime" name="startTime" style="height: 30px; width: 150px;" data-format="yyyy-MM-dd hh:mm" type="text" >
						</input>
						<span class="add-on">
							<i data-time-icon="icon-time" data-date-icon="icon-calendar"> </i>
						</span>
					</div>
           
					结束时间
					<div id="datetimepicker2" class="input-append date" style="margin-bottom: 0px;">
						<input id="endTime" name="endTime" style="height: 30px; width: 150px;" data-format="yyyy-MM-dd hh:mm" type="text" ></input> 
						<span class="add-on" ondragleave="query()"> 
							<i data-time-icon="icon-time" data-date-icon="icon-calendar"> </i>
						</span>
					</div>
					<input class="btn btn-primary " value="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" onclick="query()" type="submit">
				</th>
			</tr>
		</table>
	
		<div class="btn-group" data-toggle="buttons">
			<label class="btn btn-info">
		    		<input type="checkbox" id="ipAll"  unchecked>All
		  	</label>
	    	
	    	<c:forEach var="item" items="${model.ipAddrs}" varStatus="status">
      			<label class="btn btn-info">
		    		<input type="checkbox" id="ip_${item}" value="${item}" unchecked>${item}
		  		</label>
			</c:forEach>
		</div>
		
	 	<div>
        	<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
       			<div style="float:left;">
       				<div id="${item.id}" class="metricGraph"></div>
       			</div>
			</c:forEach>
		</div>

		<table class="footer">
			<tr>
				<td>[ end ]</td>
			</tr>
		</table>
	</div>
</a:body>
