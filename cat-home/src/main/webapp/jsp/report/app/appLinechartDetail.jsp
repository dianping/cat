<%@ page contentType="text/html; charset=utf-8"%>
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
	<c:forEach var="item" items="${model.appDataSpreadInfos}" varStatus="status">
		<tr class="${status.index  mod 2==1 ? 'even' : 'odd'} right" name>
		<c:set var="networkCode" value="${item.network}"/>
		<c:set var="appVersionCode" value="${item.appVersion}"/>
		<c:set var="channelCode" value="${item.connectType}"/>
		<c:set var="platformCode" value="${item.platform}"/>
		<c:set var="cityCode" value="${item.city}"/>
		<c:set var="operatorCode" value="${item.operator}"/>
		<c:set var="network" value="${model.networks[networkCode].name}"/>
		<c:set var="appVersion" value="${model.versions[appVersionCode].name}"/>
		<c:set var="channel" value="${model.connectionTypes[connectTypeCode].name}"/>
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
			<button class="btn btn-small btn-info" onclick="query('paltform', ${networkCode},${appVersionCode},${channelCode},${platformCode},${cityCode},${operatorCode});">展开⬇</button></td>
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
		<td>${item.successRatio}</td>
		<td>${item.accessNumberSum}</td>
		<td>${item.responseTimeAvg}</td>
		<td>${item.requestPackageAvg}</td>
		<td>${item.responsePackageAvg}</td>
		</tr>
	</c:forEach>
</table>