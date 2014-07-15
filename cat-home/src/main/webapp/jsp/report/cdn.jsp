<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>

<jsp:useBean id="ctx" type="com.dianping.cat.report.page.cdn.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.cdn.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.cdn.Model" scope="request"/>

<a:body>
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useCss value="${res.css.local['bootstrap-datetimepicker.min.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
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
				window.location.href="?province=ALL&cdn="+id+"&startDate="+start+"&endDate="+end;
			} else if ('${payload.province}' == 'ALL') {
				window.location.href="?province="+id+"&city=ALL&cdn=${payload.cdn}&startDate="+start+"&endDate="+end;
			} else if ('${payload.city}' == 'ALL') {
				window.location.href="?province=${payload.province}&city="+id+"&cdn=${payload.cdn}&startDate="+start+"&endDate="+end;
			}
		}
		
		var cityData = ${model.cityInfo};
		
		function provinceChange(){
			var key = $("#province").val();
			var value = cityData[key];
			
			select = document.getElementById("city");
			select.length=0;
			for (var prop in value) {
			    var opt = $('<option />');
		  		var city = value[prop].city;
		  		
		  		if(city==''){
			  		city = 'ALL';
		  		}
			  	
		  		opt.val(city).html(city);
		  		opt.appendTo(select);
			}
		}
		
		function cdnChange(){
			var key = $("#cdn").val();
			
			if (key == "ALL") {
				document.getElementById("province").length = 0;
				document.getElementById("city").length = 0;
			} else if (document.getElementById("province").length == 0) {
				for ( var prop in cityData) {
					if (prop == '') prop = 'ALL';
					$('#province').append("<option value='"+prop+"'>"+prop+"</option>");
				}
			}
		}

		$(document).ready(function() {
			$('.date').datetimepicker();
			$('#startTime').val("${w:format(model.start,'yyyy-MM-dd HH:mm')}");
			$('#endTime').val("${w:format(model.end,'yyyy-MM-dd HH:mm')}");
			
			$('#cdn').on('change', cdnChange).val('${payload.cdn}');
			cdnChange();
			
			$('#province').on('change',provinceChange);
			
			var province = '${payload.province}';
			var city = '${payload.city}';
			
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
					<select style="width: 120px;" name="innerorouter" id=""innerorouter"">
						<option value="inner">内部cdn</option>
						<option value="outer">外部cdn</option>
					</select> 
				</th>
				<th class="left">
				cdn服务商 <select style="width: 120px;" name="cdn" id="cdn">
						<option value="ALL">ALL</option>
						<option value="网宿">网宿</option>
						<option value="帝联">帝联</option>
						</select>
				省份 <select style="width: 100px;" name="province" id="province">
				</select> 城市 <select style="width: 100px;" name="city" id="city">
				</select> 
				</th>
				<th class="right">开始时间
					<div class="input-append date"
						style="margin-bottom: 0px;">
						<input id="startTime" name="startTime"
							style="height: 30px; width: 150px;"
							data-format="yyyy-MM-dd hh:mm" type="text"></input> <span
							class="add-on"> <i data-time-icon="icon-time"
							data-date-icon="icon-calendar"> </i>
						</span>
					</div> 结束时间
					<div class="input-append date"
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
				<div>
				<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
					<div style="float: left; text-align: center;">
						<div style="margin: 20px 0 0 0; cursor: pointer;"><a onclick="proceed('${item.id}');">${item.id}</a></div>
						<div id="${item.id}" style="width:450px; height:380px;"></div>
					</div>
				</c:forEach></div>

		<table class="footer">
			<tr>
				<td>[ end ]</td>
			</tr>
		</table>
	</div>
	
</a:body>