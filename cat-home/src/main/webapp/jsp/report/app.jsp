<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.app.Context"
	scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.report.page.app.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.app.Model"
	scope="request" />

<a:body>
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useCss value="${res.css.local['bootstrap-datetimepicker.min.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
	<script type="text/javascript">
		var commandInfo = ${model.command};
		function check() {
			var value = document.getElementById("checkbox").checked;

			if (value == true) {
				$('#history').slideDown();
				command2Change();
			} else {
				$('#history').slideUp();
			}
		}
		var command1Change = function command1Change() {
			var key = $("#command").val();
			var value = commandInfo[key];
			var code = document.getElementById("code");
			code.length = 0;
			for ( var prop in value) {
				var opt = $('<option />');

				opt.html(value[prop].name);
				opt.val(value[prop].id);
				opt.appendTo(code);
			}
		}
		var command2Change = function command2Change() {
			var key = $("#command2").val();
			var value = commandInfo[key];
			var code = document.getElementById("code2");
			code.length = 0;
			for ( var prop in value) {
				var opt = $('<option />');

				opt.html(value[prop].name);
				opt.val(value[prop].id);
				opt.appendTo(code);
			}
		}

		function getDate() {
			var myDate = new Date();

			return myDate.getFullYear() + "-" + myDate.getMonth() + "-"
					+ myDate.getDate();
		}
		
		function query() {
			var time = $("#time").val();
			var command = $("#command").val();
			var code = $("#code").val();
			var network = $("#network").val();
			var version = $("#version").val();
			var channel = $("#channel").val();
			var palteform = $("#platform").val();
			var city = $("#city").val();
			var operator = $("#operator").val();
			var split = ";";
			var query1 = time + split + command + split + code + split
					+ network + split + version + split + channel + split
					+ palteform + split + city + split + operator;
			var query2 = "";
			var value = document.getElementById("checkbox").checked;

			if (value == true) {
				var time2 = $("#time2").val();
				var command2 = $("#command2").val();
				var code2 = $("#code2").val();
				var network2 = $("#network2").val();
				var version2 = $("#version2").val();
				var channel2 = $("#channel2").val();
				var palteform2 = $("#platform2").val();
				var city2 = $("#city2").val();
				var operator2 = $("#operator2").val();
				query2 = time2 + split + command2 + split + code2 + split
						+ network2 + split + version2 + split + channel2
						+ split + palteform2 + split + city2 + split
						+ operator2;
			}

			var href = "?query1=" + query1 + "&query2=" + query2;
			window.location.href = href;
		}

		$(document).ready(function() {
			$('#datetimepicker1').datetimepicker();
			$('#datetimepicker2').datetimepicker();

			var query1 = '${payload.query1}';
			var query2 = '${payload.query2}';
			var command1 = $('#command');
			var command2 = $('#command2');
			var words = query1.split(";");

			command1.on('change', command1Change);
			command2.on('change', command2Change);

			$("#command").val(words[1]);
			
			if(words[0]==null||words[0].length==0){
				$("#time").val(getDate());
			}else{
				$("#time").val(words[0]);
			}
			
			command1Change();
			$("#code").val(words[2]);
			$("#network").val(words[3]);
			$("#version").val(words[4]);
			$("#channel").val(words[5]);
			$("#platform").val(words[6]);
			$("#city").val(words[7]);
			$("#operator").val(words[8]);

			if (query2 != null && query2 != '') {
				$('#history').slideDown();
				document.getElementById("checkbox").checked = true;
				var words = query2.split(";");

				if(words[0]==null||words[0].length==0){
					$("#time2").val(getDate());
				}else{
					$("#time2").val(words[0]);
				}

				$("#command2").val(words[1]);
				command2Change();
				$("#code2").val(words[2]);
				$("#network2").val(words[3]);
				$("#version2").val(words[4]);
				$("#channel2").val(words[5]);
				$("#platform2").val(words[6]);
				$("#city2").val(words[7]);
				$("#operator2").val(words[8]);
			}else{
				$("#time2").val(getDate());
			}
		});
	</script>
	<div class="report">
		<table>
			<tr>
				<th align=left>时间
					<div id="datetimepicker1" class="input-append date"
						style="margin-bottom: 0px;">
						<input id="time" name="time" style="height: 30px; width: 150px;"
							data-format="yyyy-MM-dd" type="text"></input> <span
							class="add-on"> <i data-time-icon="icon-time"
							data-date-icon="icon-calendar"> </i>
						</span>
					</div> 命令字 <select id="command" style="width: 350px;">
						<c:forEach var="item" items="${model.commands}" varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 返回码 <select id="code" style="width: 120px;">
				</select> 网络类型 <select id="network" style="width: 80px;">
						<c:forEach var="item" items="${model.networks}" varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select>
				</th>
			</tr>
			<tr>
				<th align=left>版本 <select id="version" style="width: 100px;">
						<c:forEach var="item" items="${model.versions}" varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 渠道 <select id="channel" style="width: 100px;">
						<c:forEach var="item" items="${model.channels}" varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 平台 <select id="platform" style="width: 100px;">
						<c:forEach var="item" items="${model.platforms}"
							varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 地区 <select id="city" style="width: 100px;">
						<c:forEach var="item" items="${model.cities}" varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 运营商 <select id="operator" style="width: 100px;">
						<c:forEach var="item" items="${model.operators}"
							varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> <input class="btn btn-primary "
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" /> <input class="btn btn-primary" id="checkbox"
					onclick="check()" type="checkbox" /> <label for="checkbox"
					style="display: -webkit-inline-box">选择历史对比</label>
				</th>
			</tr>
		</table>
		<table id="history" style="display: none">
			<tr>
				<th align=left>时间
					<div id="datetimepicker2" class="input-append date"
						style="margin-bottom: 0px;">
						<input id="time2" name="time2" style="height: 30px; width: 150px;"
							data-format="yyyy-MM-dd" type="text"></input> <span
							class="add-on"> <i data-time-icon="icon-time"
							data-date-icon="icon-calendar"> </i>
						</span>
					</div> 命令字 <select id="command2" style="width: 350px;">
						<c:forEach var="item" items="${model.commands}" varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 返回码 <select id="code2" style="width: 120px;">
				</select> 网络类型 <select id="network2" style="width: 80px;">
						<c:forEach var="item" items="${model.networks}" varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select>
				</th>
			</tr>
			<tr>
				<th align=left>版本 <select id="version2" style="width: 100px;">
						<c:forEach var="item" items="${model.versions}" varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 渠道 <select id="channel2" style="width: 100px;">
						<c:forEach var="item" items="${model.channels}" varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 平台 <select id="platform2" style="width: 100px;">
						<c:forEach var="item" items="${model.platforms}"
							varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 地区 <select id="city2" style="width: 100px;">
						<c:forEach var="item" items="${model.cities}" varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 运营商 <select id="operator2" style="width: 100px;">
						<c:forEach var="item" items="${model.operators}"
							varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select>
				</th>
			</tr>
		</table>

		<table class="footer">
			<tr>
				<td>[ end ]</td>
			</tr>
		</table>
	</div>
</a:body>