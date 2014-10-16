<%@ page contentType="text/html; charset=utf-8"%>
<table >
			<tr>
				<th align=left>时间
					<div id="datetimepicker1" class="input-append date"
						style="margin-bottom: 0px;">
						<input id="time" name="time" style="height: 30px; width: 150px;"
							data-format="yyyy-MM-dd" type="text"></input> <span
							class="add-on"> <i data-time-icon="icon-time"
							data-date-icon="icon-calendar"> </i>
						</span>
					</div> 
						项目<select id="domains" style="width: 100px;"></select>
						命令字 <select id="command" style="width: 240px;">
						</select> 
				返回码 <select id="code" style="width: 120px;"><option value=''>All</option>
				</select> 网络类型 <select id="network" style="width: 80px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.networks}" varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select>
				</th>
			</tr>
			<tr>
				<th align=left>版本 <select id="version" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.versions}" varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select> 连接类型 <select id="connectionType" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.connectionTypes}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select> 平台 <select id="platform" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.platforms}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select> 地区 <select id="city" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.cities}" varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select> 运营商 <select id="operator" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.operators}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select> <input class="btn btn-primary "
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" /> <input class="btn btn-primary" id="checkbox"
					onclick="check()" type="checkbox" /> <label for="checkbox"
					style="display: -webkit-inline-box">选择对比</label>
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
					</div> 
						项目<select id="domains2" style="width: 100px;"></select>
						命令字 <select id="command2" style="width: 240px;">
						</select> 
						 返回码 <select id="code2" style="width: 120px;">
						<option value=''>All</option>
				</select> 网络类型 <select id="network2" style="width: 80px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.networks}" varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select>
				</th>
			</tr>
			<tr>
				<th align=left>版本 <select id="version2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.versions}" varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select> 连接类型 <select id="connectionType2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.connectionTypes}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select> 平台 <select id="platform2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.platforms}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select> 地区 <select id="city2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.cities}" varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select> 运营商 <select id="operator2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.operators}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
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

		<div style="float: left; width: 100%;">
			<div id="${model.lineChart.id}"></div>
		</div>
		<br/>
<table id="web_content" class="table table-striped table-bordered table-condensed table-hover">
	<thead><tr class="text-success">
		<th>网络类型</th>
		<th>版本</th>
		<th>连接类型</th>
		<th>平台</th>
		<th>地区</th>
		<th>运营商</th>
		<th><a href="javascript:queryGroupBy('success');">成功率</a>(%)</th>
		<th><a href="javascript:queryGroupBy('request');">总请求数</a></th>
		<th><a href="javascript:queryGroupBy('delay');">成功平均延迟</a>(ms)</th>
		<th><a href="javascript:queryGroupBy('requestPackage');">平均发包</a>(B)</th>
		<th><a href="javascript:queryGroupBy('responsePackage');">平均回包</a>(B)</th>
	</tr></thead>
	<tbody>
	<c:forEach var="item" items="${model.appDataSpreadInfos}" varStatus="status">
		<tr class="${status.index  mod 2==1 ? 'even' : 'odd'} right">
		<c:set var="networkCode" value="${item.network}"/>
		<c:set var="appVersionCode" value="${item.appVersion}"/>
		<c:set var="channelCode" value="${item.connectType}"/>
		<c:set var="platformCode" value="${item.platform}"/>
		<c:set var="cityCode" value="${item.city}"/>
		<c:set var="operatorCode" value="${item.operator}"/>
		<c:set var="network" value="${model.networks[networkCode].name}"/>
		<c:set var="appVersion" value="${model.versions[appVersionCode].name}"/>
		<c:set var="channel" value="${model.connectionTypes[channelCode].name}"/>
		<c:set var="platform" value="${model.platforms[platformCode].name}"/>
		<c:set var="city" value="${model.cities[cityCode].name}"/>
		<c:set var="operator" value="${model.operators[operatorCode].name}"/>
		
		<c:choose>
			<c:when test="${empty network}">
			<td><button class="btn btn-small btn-info" onclick="query('network', ${networkCode},${appVersionCode},${channelCode},${platformCode},${cityCode},${operatorCode});">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<td>${network}</td>
			</c:otherwise>
		</c:choose>
		
		<c:choose>
			<c:when test="${empty appVersion}">
			<td><button class="btn btn-small btn-info" onclick="query('app-version', ${networkCode},${appVersionCode},${channelCode},${platformCode},${cityCode},${operatorCode});">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<td>${appVersion}</td>
			</c:otherwise>
		</c:choose>
		
		<c:choose>
			<c:when test="${empty channel}">
			<td><button class="btn btn-small btn-info" onclick="query('connnect-type', ${networkCode},${appVersionCode},${channelCode},${platformCode},${cityCode},${operatorCode});">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<td>${channel}</td>
			</c:otherwise>
		</c:choose>
		
		<c:choose>
			<c:when test="${empty platform}">
			<td>
			<button class="btn btn-small btn-info" onclick="query('platform', ${networkCode},${appVersionCode},${channelCode},${platformCode},${cityCode},${operatorCode});">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<td>${platform}</td>
			</c:otherwise>
		</c:choose>
		
		<c:choose>
			<c:when test="${empty city}">
			<td><button class="btn btn-small btn-info" onclick="query('city', ${networkCode},${appVersionCode},${channelCode},${platformCode},${cityCode},${operatorCode});">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<td>${city}</td>
			</c:otherwise>
		</c:choose>
		
		<c:choose>
			<c:when test="${empty operator}">
			<td><button class="btn btn-small btn-info" onclick="query('operator', ${networkCode},${appVersionCode},${channelCode},${platformCode},${cityCode},${operatorCode});">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<td>${operator}</td>
			</c:otherwise>
		</c:choose>
 		<td>${w:format(item.successRatio,'#0.000')}%</td>
		<td>${w:format(item.accessNumberSum,'#,###,###,###,##0')}</td>
		<td>${w:format(item.responseTimeAvg,'###,##0.000')}</td>
		<td>${w:format(item.requestPackageAvg,'#,###,###,###,##0')}</td>
		<td>${w:format(item.responsePackageAvg,'#,###,###,###,##0')}</td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<script>
	var domainToCommandsJson = ${model.domainToCommandsJson};

	function changeDomain(domainId, commandId, domainInitVal, commandInitVal){
		if(domainInitVal == ""){
			domainInitVal = $("#"+domainId).val()
		}
		var commandSelect = $("#"+commandId);
		var commands = domainToCommandsJson[domainInitVal];
		
		$("#"+domainId).val(domainInitVal);
		commandSelect.empty();
		for(var cou in commands){
			var command = commands[cou];
			if(command['title'] != undefined){
				commandSelect.append($("<option value='"+command['id']+"'>"+command['title']+"</option>"));
			}else{
				commandSelect.append($("<option value='"+command['id']+"'>"+command['name']+"</option>"));
			}
		}
		if(commandInitVal != ''){
			commandSelect.val(commandInitVal);
		}
	}
	
	function changeDomainByChange(){
		if($(this).attr("id")=="domains"){
			var domain = $("#domains").val();
			var commandSelect = $("#command");
		}else{
			var domain = $("#domains2").val();
			var commandSelect = $("#command2");
		}
		var commands = domainToCommandsJson[domain];
		commandSelect.empty();
		
		for(var cou in commands){
			var command = commands[cou];
			if(command['title'] != undefined){
				commandSelect.append($("<option value='"+command['id']+"'>"+command['title']+"</option>"));
			}else{
				commandSelect.append($("<option value='"+command['id']+"'>"+command['name']+"</option>"));
			}
		}
	}
	
	function initDomain(domainSelectId, commandSelectId, domainInitVal, commandInitVal){
		var domainsSelect = $("#"+domainSelectId);
		for(var domain in domainToCommandsJson){
			domainsSelect.append($("<option value='"+domain+"'>"+domain+"</option>"))
		}
		changeDomain(domainSelectId, commandSelectId, domainInitVal, commandInitVal);
		domainsSelect.on('change', changeDomainByChange);
	}

	$(document).ready(function(){
		initDomain('domains', 'command', '${payload.domains}', '${payload.commandId}');
		initDomain('domains2', 'command2', '${payload.domains2}', '${payload.commandId2}');
	})
</script>