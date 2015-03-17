<%@ page contentType="text/html; charset=utf-8"%>
<table width="100%">
			<tr>
				<th>
		        <div style="float:left;">
						&nbsp;日期
					<input type="text" id="time" style="width:110px;"/>
					</div>
					&nbsp;项目<select id="domains" style="width: 200px;"></select>
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
				<th align=left>&nbsp;版本 <select id="version" style="width: 100px;">
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
				</select> <input class="btn btn-primary btn-sm"
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" /> <input class="btn btn-primary btn-sm" id="checkbox"
					onclick="check()" type="checkbox" /> <label for="checkbox"
					style="display: -webkit-inline-box">选择对比</label>
				</th>
			</tr>
		</table>
		<table id="history" style="display: none">
			<tr>
				<th align=left>
				<div style="float:left;">
						&nbsp;日期
					<input type="text" id="time2" style="width:110px;"/>
					</div>
						&nbsp;项目<select id="domains2" style="width: 200px;"></select>
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
				<th align=left>&nbsp;版本 <select id="version2" style="width: 100px;">
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

		<div>&nbsp;
			<label class="btn btn-info btn-sm"><input type="radio"
				name="typeCheckbox" value="request">请求数
			</label><label class="btn btn-info btn-sm"> <input type="radio"
				name="typeCheckbox" value="success">成功率
			</label><label class="btn btn-info btn-sm">  <input type="radio"
				name="typeCheckbox" value="delay">成功延时
			</label>
		</div>

		<div style="float: left; width: 100%;">
			<div id="${model.lineChart.id}"></div>
		</div>
		<br/>
<table id="web_content" class="table table-striped table-condensed table-bordered table-hover">
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
	<c:forEach var="item" items="${model.appDataDetailInfos}" varStatus="status">
		<tr class="right">
		<c:set var="networkCode" value="${item.network eq '-1' ? '' : item.network}"/>
		<c:set var="appVersionCode" value="${item.appVersion eq '-1' ? '' : item.appVersion}"/>
		<c:set var="channelCode" value="${item.connectType eq '-1' ? '' : item.connectType}"/>
		<c:set var="platformCode" value="${item.platform eq '-1' ? '' : item.platform}"/>
		<c:set var="cityCode" value="${item.city eq '-1' ? '' : item.city}"/>
		<c:set var="operatorCode" value="${item.operator eq '-1' ? '' : item.operator}"/>
		<c:set var="network" value="${model.networks[networkCode].name}"/>
		<c:set var="appVersion" value="${model.versions[appVersionCode].name}"/>
		<c:set var="channel" value="${model.connectionTypes[channelCode].name}"/>
		<c:set var="platform" value="${model.platforms[platformCode].name}"/>
		<c:set var="city" value="${model.cities[cityCode].name}"/>
		<c:set var="operator" value="${model.operators[operatorCode].name}"/>
		
		<c:choose>
			<c:when test="${empty networkCode}">
				<td><button class="btn btn-xs btn-info" onclick="query('network', '${networkCode}','${appVersionCode}','${channelCode}','${platformCode}','${cityCode}','${operatorCode}');">展开⬇</button></td>
			</c:when>
			<c:otherwise>
				<c:choose>
				<c:when test="${empty network}">
					<td class="text-danger">Unknown [${networkCode}]</td>				
				</c:when>
				<c:otherwise>
				<td>${network}</td>
				</c:otherwise>
				</c:choose>
			</c:otherwise>
		</c:choose>
		
		<c:choose>
			<c:when test="${empty appVersionCode}">
				<td><button class="btn btn-xs btn-info" onclick="query('app-version', '${networkCode}','${appVersionCode}','${channelCode}','${platformCode}','${cityCode}','${operatorCode}');">展开⬇</button></td>
			</c:when>
			<c:otherwise>
				<c:choose>
				<c:when test="${empty appVersion}">
					<td class="text-danger">Unknown [${appVersionCode}]</td>
				</c:when>
				<c:otherwise>
				<td>${appVersion}</td>
				</c:otherwise>
				</c:choose>
			</c:otherwise>
		</c:choose>
		
		<c:choose>
		<c:when test="${empty channelCode}">
			<td><button class="btn btn-xs btn-info" onclick="query('connnect-type', '${networkCode}','${appVersionCode}','${channelCode}','${platformCode}','${cityCode}','${operatorCode}');">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<c:choose>
			<c:when test="${empty channel}">
				<td class="text-danger">Unknown [${channelCode}]</td>
			</c:when>
			<c:otherwise>
			<td>${channel}</td>
			</c:otherwise>
			</c:choose>
			</c:otherwise>
		</c:choose>
		
		<c:choose>
		<c:when test="${empty platformCode}">
			<td><button class="btn btn-xs btn-info" onclick="query('platform', '${networkCode}','${appVersionCode}','${channelCode}','${platformCode}','${cityCode}','${operatorCode}');">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<c:choose>
			<c:when test="${empty platform}">
				<td class="text-danger">Unknown [${platformCode}]</td>
			</c:when>
			<c:otherwise>
			<td>${platform}</td>
			</c:otherwise>
			</c:choose>
			</c:otherwise>
		</c:choose>
		
		<c:choose>
		<c:when test="${empty cityCode}">
				<td><button class="btn btn-xs btn-info" onclick="query('city', '${networkCode}','${appVersionCode}','${channelCode}','${platformCode}','${cityCode}','${operatorCode}');">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<c:choose>
			<c:when test="${empty city}">
				<td class="text-danger">Unknown [${cityCode}]</td>
			</c:when>
			<c:otherwise>
			<td>${city}</td>
			</c:otherwise>
			</c:choose>
			</c:otherwise>
		</c:choose>
		
		<c:choose>
		<c:when test="${empty operatorCode}">
				<td><button class="btn btn-xs btn-info" onclick="query('operator', '${networkCode}','${appVersionCode}','${channelCode}','${platformCode}','${cityCode}','${operatorCode}');">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<c:choose>
			<c:when test="${empty operator}">
				<td class="text-danger">Unknown [${operatorCode}]</td>
			</c:when>
			<c:otherwise>
			<td>${operator}</td>
			</c:otherwise>
			</c:choose>
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
