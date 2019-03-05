<%@ page contentType="text/html; charset=utf-8"%>
<style>
.form-control {
  height: 30px;
}
</style>
<table>
			<tr>
				<th>
				<div class="input-group" style="float:left;">
	              <span class="input-group-addon">开始</span>
	              <input type="text" id="time" style="width:130px"/>
	            </div>
				<div class="input-group" style="float:left;width:60px">
	              <span class="input-group-addon">结束</span>
        	      <input type="text" id="endTime" style="width:60px;"/></div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">App</span>
					<select id="appId" style="width: 100px;">
						<c:forEach var="item" items="${model.apps}" varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
        	    </div>
				<div class="input-group" style="float:left;width:350px">
					<span class="input-group-addon">命令字</span>
		            <form id="wrap_search" style="margin-bottom:0px;">
						<span class="input-icon" style="width:350px;">
							<input type="text" placeholder="" class="search-input search-input form-control ui-autocomplete-input" id="command" autocomplete="on" data=""/>
							<i class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
					</form>
	            </div>
	             <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">来源</span>
					<select id="source" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.sources}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">返回码</span>
					<select id="code" style="width:120px"><option value=''>All</option></select>
	            </div>
				</th>
				</tr>
			<tr>
				<th align=left>
			 	<div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">网络类型</span>
					<select id="network">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.networks}" varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
				<div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">版本</span>
					<select id="version" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.versions}" varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">连接类型</span>
					<select id="connectionType" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.connectionTypes}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">平台</span>
					<select id="platform" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.platforms}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">地区</span>
					<select id="city" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.cities}" varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">运营商</span>
					<select id="operator" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.operators}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <input class="btn btn-primary btn-sm"
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" /> <input class="btn btn-primary btn-sm" id="checkbox"
					onclick="check()" type="checkbox" /> <label for="checkbox"
					style="display: -webkit-inline-box">选择对比</label>
				</th>
			</tr>
		</table>
		<table id="history" style="display: none">
				<tr>
				<th>
				<div class="input-group" style="float:left;">
	              <span class="input-group-addon">开始</span>
	              <input type="text" id="time2" style="width:130px"/>
	            </div>
	            <div class="input-group" style="float:left;width:60px">
	              <span class="input-group-addon">结束</span>
        	      <input type="text" id="endTime2" style="width:60px;"/></div>
        	    <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">App</span>
					<select id="appId2" style="width: 100px;">
						<c:forEach var="item" items="${model.apps}" varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
        	    </div>
				<div class="input-group" style="float:left;width:350px">
					<span class="input-group-addon">命令字</span>
		            <form id="wrap_search2" style="margin-bottom:0px;">
						<span class="input-icon" style="width:350px;">
							<input type="text" placeholder="input domain for search" class="search-input search-input form-control ui-autocomplete-input" id="command2" autocomplete="on" data=""/>
							<i class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
					</form>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">来源</span>
					<select id="source2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.sources}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">返回码</span>
					<select id="code2" style="width:120px"><option value=''>All</option></select>
	            </div>
				</th>
				</tr>
				<tr>
			<th align=left>
			 	<div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">网络类型</span>
					<select id="network2">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.networks}" varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
				<div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">版本</span>
					<select id="version2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.versions}" varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">连接类型</span>
					<select id="connectionType2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.connectionTypes}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">平台</span>
					<select id="platform2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.platforms}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">地区</span>
					<select id="city2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.cities}" varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">运营商</span>
					<select id="operator2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.operators}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            </th>
			</tr>
		</table>

		<div>&nbsp;
			<label class="btn btn-info btn-sm"><input type="radio"
				name="typeCheckbox" value="request">请求数
			</label><label class="btn btn-info btn-sm"> <input type="radio"
				name="typeCheckbox" value="success">网络成功率</label><label class="btn btn-info btn-sm">
			<input type="radio" name="typeCheckbox" value="businessSuccess">业务成功率
			</label><label class="btn btn-info btn-sm">  <input type="radio"
				name="typeCheckbox" value="delay">成功延时
			</label>
		</div>

		<div style="float: left; width: 100%;">
			<div id="${model.lineChart.id}"></div>
		</div>
		<br/>
<table id="web_content" class="table table-striped table-condensed table-bordered table-hover">
	<thead>
	<tr class="text-success">
		<th class="right text-success">类别</th>
		<th class="right text-success">成功率(%)</th>
		<th class="right text-success">总请求数</th>
		<th class="right text-success">成功平均延迟(ms)</th>
		<th class="right text-success">平均发包(B)</th>
		<th class="right text-success">平均回包(B)</th>
	</tr></thead>
	<tbody>
		<c:forEach var="item" items="${model.comparisonAppDetails}">
		<tr class="right">
			<td>${item.key}</td>
			<td>${w:format(item.value.successRatio,'#0.000')}%</td>
			<td>${w:format(item.value.accessNumberSum,'#,###,###,###,##0')}</td>
			<td>${w:format(item.value.responseTimeAvg,'###,##0.000')}</td>
			<td>${w:format(item.value.requestPackageAvg,'#,###,###,###,##0')}</td>
			<td>${w:format(item.value.responsePackageAvg,'#,###,###,###,##0')}</td>
		</tr>
		</c:forEach>
	</tbody>
</table>
<h5 class="center text-success"><strong>点击展开，进行OLAP查询</strong></h5>
<table id="comparison_content" class="table table-striped table-condensed table-bordered table-hover">
	<thead>
	<tr>
		<th class="right text-success">网络类型</th>
		<th class="right text-success">版本</th>
		<th class="right text-success">连接类型</th>
		<th class="right text-success">平台</th>
		<th class="right text-success">地区</th>
		<th class="right text-success">运营商</th>
		<th class="right text-success">来源</th>
		<th class="right"><a href="javascript:queryGroupBy('success');">成功率</a>(%)</th>
		<th class="right"><a href="javascript:queryGroupBy('request');">总请求数</a></th>
		<th class="right"><a href="javascript:queryGroupBy('delay');">成功平均延迟</a>(ms)</th>
		<th class="right"><a href="javascript:queryGroupBy('requestPackage');">平均发包</a>(B)</th>
		<th class="right"><a href="javascript:queryGroupBy('responsePackage');">平均回包</a>(B)</th>
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
		<c:set var="sourceCode" value="${item.source eq '-1' ? '' : item.source}"/>
		
		<c:set var="network" value="${model.networks[networkCode].value}"/>
		<c:set var="appVersion" value="${model.versions[appVersionCode].value}"/>
		<c:set var="channel" value="${model.connectionTypes[channelCode].value}"/>
		<c:set var="platform" value="${model.platforms[platformCode].value}"/>
		<c:set var="city" value="${model.cities[cityCode].value}"/>
		<c:set var="operator" value="${model.operators[operatorCode].value}"/>
		<c:set var="source" value="${model.sources[sourceCode].value}"/>
		
		<c:choose>
			<c:when test="${empty networkCode}">
				<td><button class="btn btn-xs btn-info" onclick="query('network', '${networkCode}','${appVersionCode}','${channelCode}','${platformCode}','${cityCode}','${operatorCode}','${sourceCode}');">展开⬇</button></td>
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
				<td><button class="btn btn-xs btn-info" onclick="query('app-version', '${networkCode}','${appVersionCode}','${channelCode}','${platformCode}','${cityCode}','${operatorCode}','${sourceCode}');">展开⬇</button></td>
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
			<td><button class="btn btn-xs btn-info" onclick="query('connect-type', '${networkCode}','${appVersionCode}','${channelCode}','${platformCode}','${cityCode}','${operatorCode}','${sourceCode}');">展开⬇</button></td>
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
			<td><button class="btn btn-xs btn-info" onclick="query('platform', '${networkCode}','${appVersionCode}','${channelCode}','${platformCode}','${cityCode}','${operatorCode}','${sourceCode}');">展开⬇</button></td>
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
				<td><button class="btn btn-xs btn-info" onclick="query('city', '${networkCode}','${appVersionCode}','${channelCode}','${platformCode}','${cityCode}','${operatorCode}','${sourceCode}');">展开⬇</button></td>
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
				<td><button class="btn btn-xs btn-info" onclick="query('operator', '${networkCode}','${appVersionCode}','${channelCode}','${platformCode}','${cityCode}','${operatorCode}','${sourceCode}');">展开⬇</button></td>
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
		
		<c:choose>
			<c:when test="${empty sourceCode}">
				<td><button class="btn btn-xs btn-info" onclick="query('source', '${networkCode}','${appVersionCode}','${channelCode}','${platformCode}','${cityCode}','${operatorCode}','${sourceCode}');">展开⬇</button></td>
			</c:when>
			<c:otherwise>
			<c:choose>
			<c:when test="${empty source}">
				<td class="text-danger">Unknown [${sourceCode}]</td>
			</c:when>
			<c:otherwise>
			<td>${source}</td>
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
