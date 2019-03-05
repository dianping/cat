<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.system.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.system.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.system.Model" scope="request" />

<a:application>
	<script type="text/javascript">
		function query() {
			var domain = $("#search").val();
			var type = $("#type").val();
			var start = $("#startTime").val();
			var end = $("#endTime").val();
			var ipAddrs = '';
			
			if("${model.ipAddrs}" != "[]") {
				ipAddrs = "${model.ipAddrs}".replace(/[\[\]]/g,'').split(', ');
			}
			
			var curIpAddrs = '';
			var num = 0;
			if(document.getElementById("ipAll").checked == false && ipAddrs.length > 0) {
				for( var i=0; i<ipAddrs.length; i++){
				 	var ip = "ip_" + ipAddrs[i];
					if(document.getElementById(ip).checked){
						curIpAddrs += ipAddrs[i] + "_";
					} 
				}
				curIpAddrs = curIpAddrs.substring(0, curIpAddrs.length-1);
			}else{
				curIpAddrs = "All";
			}
			
			window.location.href = "?domain=" + domain + "&type=" + type 
					+ "&ipAddrs=" + curIpAddrs + "&startDate=" + start + "&endDate="
					+ end; 
		}
		
		function clickAll() {
			var ipAddrs = '';
			
			if("${model.ipAddrs}" != "[]"){
				var ipAddrs = "${model.ipAddrs}".replace(/[\[\]]/g,'').split(', ');
			}
			
			for( var i=0; i<ipAddrs.length; i++){
			 	var ip = "ip_" + ipAddrs[i];
				document.getElementById(ip).checked = document.getElementById("ipAll").checked;
			}
		}
		
		function clickIp() {
			var ipAddrs = '';
			if("${model.ipAddrs}" != "[]") {
				ipAddrs = "${model.ipAddrs}".replace(/[\[\]]/g,'').split(', ');
			}
			var num = 0;
			for( var i=0; i<ipAddrs.length; i++){
			 	var ip = "ip_" + ipAddrs[i];
				if(document.getElementById(ip).checked){
					num ++;
				}else{
					document.getElementById("ipAll").checked = false;
					break;
				} 
			}
			if(num == ipAddrs.length) {
				document.getElementById("ipAll").checked = true;
			}
		}

		$(document).ready(
				function() {
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
					
					$('#startTime').val("${w:format(model.startTime,'yyyy-MM-dd HH:mm')}");
					$('#endTime').val("${w:format(model.endTime,'yyyy-MM-dd HH:mm')}");
					$('#type').val('${payload.type}');
					$('#domain').val('${payload.domain}');
					$('#System_report').addClass('active open');
					$('#system_paas').addClass('active');
					
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
					
					$.widget( "custom.catcomplete", $.ui.autocomplete, {
						_renderMenu: function( ul, items ) {
							var that = this,
							currentCategory = "";
							$.each( items, function( index, item ) {
								if ( item.category != currentCategory ) {
									ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
									currentCategory = item.category;
								}
								that._renderItemData( ul, item );
							});
						}
					});
					
					var data = [];
					<c:forEach var="item" items="${model.productLines}">
								var item = {};
								item['label'] = '${item}';
								item['category'] = '产品线';
								data.push(item);
					</c:forEach>
							
					$( "#search" ).catcomplete({
						delay: 0,
						source: data
					});
					
					$("#search_go").bind("click",function(e){
						query();
					});
					$('#wrap_search').submit(
						function(){
							query();
							return false;
						}		
					);
					
					
					var domain = '${payload.domain}';
					if(domain != ''){
						$('#search').val(domain);
					}
					
					var type = '${payload.type}';
					if(type != ''){
						$('#type').val(type);
					}
					
					<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
						var data = ${item.jsonString};
						graphMetricChart(document.getElementById('${item.id}'), data);
					</c:forEach>
				
			});
	</script>
		<table>
			<tr>
				<th class="left">
				<div style="float:left;">
						&nbsp;开始
					<input type="text" id="startTime" style="width:150px;"/>
						结束
						<input type="text" id="endTime" style="width:150px;"/></div>
		        &nbsp;查询类型<select style="width: 100px;" name="type" id="type" >
							<option value="paasSystem">Paas系统</option>
							<!-- option value="system">系统</option>
							<option value="jvm">JVM</option>
							<option value="nginx">Nginx</option> -->
					</select>
					</th>
				<th>&nbsp;&nbsp;</th>
		        <th>
					<div class="navbar-header pull-left position" style="width:350px;">
						<form id="wrap_search" style="margin-bottom:0px;">
						<div class="input-group">
						<input id="search" type="text" class="search-input form-control ui-autocomplete-input" placeholder="input domain for search" autocomplete="off"/>
						<span class="input-group-btn">
							<button class="btn btn-sm btn-primary" type="button" id="search_go">
								Go
							</button> 
						</span>
						</div>
						</form>
					</div>
					</th>
			</tr>
		</table>
		<div>
	    	<label class="btn btn-sm btn-info"><input type="checkbox" id="ipAll" onclick="clickAll()" unchecked>All</label><c:forEach var="item" items="${model.ipAddrs}" varStatus="status"><label class="btn btn-sm btn-info"><input type="checkbox" id="ip_${item}" value="${item}" onclick="clickIp()" unchecked>${item}</label></c:forEach>
		</div>
		
	 	<div>
        	<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
       			<div style="float:left;">
       				<div id="${item.id}" class="metricGraph"></div>
       			</div>
			</c:forEach>
		</div>
</a:application>
