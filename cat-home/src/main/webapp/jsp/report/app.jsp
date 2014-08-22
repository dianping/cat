<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.app.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.app.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.app.Model" scope="request" />

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
			for ( var prop in value) {
				var opt = $('<option />');

				opt.html(value[prop].name);
				opt.val(value[prop].id);
				opt.appendTo(code);
			}
		}

		function getDate() {
			var myDate = new Date();
			var myMonth = new Number(myDate.getMonth());
			var month = myMonth + 1;
			var day = myDate.getDate();
			
			if(month<10){
				month = '0' + month;
			}
			if(day<10){
				day = '0' + day;
			}

			return myDate.getFullYear() + "-" + month + "-"
					+ myDate.getDate();
		}

		function query(field) {
			var time = $("#time").val();
			var command = $("#command").val();
			var code = $("#code").val();
			var network = $("#network").val();
			var version = $("#version").val();
			var connectionType = $("#connectionType").val();
			var palteform = $("#platform").val();
			var city = $("#city").val();
			var operator = $("#operator").val();
			var split = ";";
			var query1 = time + split + command + split + code + split
					+ network + split + version + split + connectionType
					+ split + palteform + split + city + split + operator;
			var query2 = "";
			var value = document.getElementById("checkbox").checked;

			if (value == true) {
				var time2 = $("#time2").val();
				var command2 = $("#command2").val();
				var code2 = $("#code2").val();
				var network2 = $("#network2").val();
				var version2 = $("#version2").val();
				var connectionType2 = $("#connectionType2").val();
				var palteform2 = $("#platform2").val();
				var city2 = $("#city2").val();
				var operator2 = $("#operator2").val();
				query2 = time2 + split + command2 + split + code2 + split
						+ network2 + split + version2 + split + connectionType2
						+ split + palteform2 + split + city2 + split
						+ operator2;
			}

			var checkboxs = document.getElementsByName("typeCheckbox");
			var type = "";

			for (var i = 0; i < checkboxs.length; i++) {
				if (checkboxs[i].checked) {
					type = checkboxs[i].value;
					break;
				}
			}
			
			if(typeof(field) == "undefined"){
				field = "";
			}
			var href = "?query1=" + query1 + "&query2=" + query2 + "&type="
					+ type + "&groupByField=" + field;
			window.location.href = href;
		}

		$(document).ready(
				function() {
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

					if (words[0] == null || words.length == 1) {
						$("#time").val(getDate());
					} else {
						$("#time").val(words[0]);
					}

					command1Change();
					$("#code").val(words[2]);
					$("#network").val(words[3]);
					$("#version").val(words[4]);
					$("#connectionType").val(words[5]);
					$("#platform").val(words[6]);
					$("#city").val(words[7]);
					$("#operator").val(words[8]);
					
					var datePair = {};
					datePair["查询1"]=$("#time").val();

					if (query2 != null && query2 != '') {
						$('#history').slideDown();
						document.getElementById("checkbox").checked = true;
						var words = query2.split(";");

						if (words[0] == null || words[0].length == 0) {
							$("#time2").val(getDate());
						} else {
							$("#time2").val(words[0]);
						}
						
						datePair["查询2"]=$("#time2").val();

						$("#command2").val(words[1]);
						command2Change();
						$("#code2").val(words[2]);
						$("#network2").val(words[3]);
						$("#version2").val(words[4]);
						$("#connectionType2").val(words[5]);
						$("#platform2").val(words[6]);
						$("#city2").val(words[7]);
						$("#operator2").val(words[8]);
					} else {
						$("#time2").val(getDate());
					}

					var checkboxs = document.getElementsByName("typeCheckbox");

					for (var i = 0; i < checkboxs.length; i++) {
						if (checkboxs[i].value == "${payload.type}") {
							checkboxs[i].checked = true;
							break;
						}
					}

					var data = ${model.lineChart.jsonString};
					graphMetricChartForApp(document
							.getElementById('${model.lineChart.id}'), data, datePair);

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
				</select> 返回码 <select id="code" style="width: 120px;"><option value=''>All</option>
				</select> 网络类型 <select id="network" style="width: 80px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.networks}" varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select>
				</th>
			</tr>
			<tr>
				<th align=left>版本 <select id="version" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.versions}" varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 连接类型 <select id="connectionType" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.connectionTypes}"
							varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 平台 <select id="platform" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.platforms}"
							varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 地区 <select id="city" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.cities}" varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 运营商 <select id="operator" style="width: 100px;">
						<option value=''>All</option>
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
						<option value=''>All</option>
				</select> 网络类型 <select id="network2" style="width: 80px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.networks}" varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select>
				</th>
			</tr>
			<tr>
				<th align=left>版本 <select id="version2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.versions}" varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 连接类型 <select id="connectionType2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.connectionTypes}"
							varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 平台 <select id="platform2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.platforms}"
							varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 地区 <select id="city2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.cities}" varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select> 运营商 <select id="operator2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.operators}"
							varStatus="status">
							<option value='${item.id}'>${item.name}</option>
						</c:forEach>
				</select>
				</th>
			</tr>
		</table>

		<div class="btn-group" data-toggle="buttons">
			<label class="btn btn-info"><input type="radio"
				name="typeCheckbox" value="request">请求数
			</label> <label class="btn btn-info"> <input type="radio"
				name="typeCheckbox" value="success">成功率
			</label> <label class="btn btn-info">  <input type="radio"
				name="typeCheckbox" value="delay">成功延时
			</label>
		</div>

		<div style="margin: 0 auto; width: 95%;">
			<div id="${model.lineChart.id}"></div>
		</div>
		 <div class="report">
<table class="table table-striped table-bordered table-condensed">
	<tr class="text-success">
		<th>网络类型</th>
		<th>版本</th>
		<th>连接类型</th>
		<th>平台</th>
		<th>地区</th>
		<th>运营商</th>
		<th>成功率(%)</th>
		<th>总请求数</th>
		<th>成功平均延迟(ms)</th>
		<th>平均发包数</th>
		<th>平均回包数</th>
	</tr>
	<c:forEach var="item" items="${model.appDatas}" varStatus="status">
		<tr class="${status.index  mod 2==1 ? 'even' : 'odd'} right">
		<c:choose>
			<c:when test="${empty item.network && status.index == 0}">
			<td><button class="btn btn-small btn-info" onclick="query('network');">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<td>${item.network}</td>
			</c:otherwise>
		</c:choose>
		<c:choose>
			<c:when test="${empty item.appVersion && status.index == 0}">
			<td><button class="btn btn-small btn-info" onclick="query('app-version');">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<td>${item.appVersion}</td>
			</c:otherwise>
		</c:choose>
		<c:choose>
			<c:when test="${empty item.connectType && status.index == 0}">
			<td><button class="btn btn-small btn-info" onclick="query('connnect-type');">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<td>${item.connectType}</td>
			</c:otherwise>
		</c:choose>
		<c:choose>
			<c:when test="${empty item.platform && status.index == 0}">
			<td>
			<button class="btn btn-small btn-info" onclick="query('platform');">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<td>${item.platform}</td>
			</c:otherwise>
		</c:choose>
		<c:choose>
			<c:when test="${empty item.city && status.index == 0}">
			<td><button class="btn btn-small btn-info" onclick="query('city');">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<td>${item.city}</td>
			</c:otherwise>
		</c:choose>
		<c:choose>
			<c:when test="${empty item.operator && status.index == 0}">
			<td><button class="btn btn-small btn-info" onclick="query('operator');">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<td>${item.operator}</td>
			</c:otherwise>
		</c:choose>
		<td>${item.successRatio}</td>
		<td>${item.accessNumberSum}</td>
		<td>${item.responseTimeAvg}</td>
		<td>${item.requestPackageAvg}</td>
		<td>${item.responsePackageAvg}</td>
		</tr>
	</c:forEach>
</table>
</div>
</div>

		<table class="footer">
			<tr>
				<td>[ end ]</td>
			</tr>
		</table>
	</div>
</a:body>