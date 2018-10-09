<%@ page contentType="text/html; charset=utf-8"%>
	<table>
		<tr>
			<th align=left>
					<div class="input-group" style="float:left;">
						<span class="input-group-addon">日期</span>
					<input type="text" id="time" style="width:110px;"/>
					</div>
					<div class="input-group" style="float:left;">
					<span class="input-group-addon">页面</span>
					<select id="page" style="width: 240px;">
					<c:forEach var="item" items="${model.appSpeedDisplayInfo.pages}" varStatus="status">
							<option value='${item}'>${item}</option>
					</c:forEach>
					</select></div>
					<div class="input-group" style="float:left;">
					<span class="input-group-addon">阶段</span>
					 <select id="step" style="width: 240px;">
					</select> <span class="input-group-addon">网络类型</span>
					 <select id="network" style="width: 80px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.networks}" varStatus="status">
						<option value='${item.value.id}'>${item.value.value}</option>
					</c:forEach>
			</select></div>
			</th>
		</tr>
		<tr>
			<th align=left>
			 <div class="input-group" style="float:left;">
					<span class="input-group-addon">版本</span><select id="version" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.versions}" varStatus="status">
						<option value='${item.value.id}'>${item.value.value}</option>
					</c:forEach>
			</select>  <span class="input-group-addon">平台</span><select id="platform" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.platforms}"
						varStatus="status">
						<option value='${item.value.id}'>${item.value.value}</option>
					</c:forEach>
			</select>  <span class="input-group-addon">地区</span><select id="city" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.cities}" varStatus="status">
						<option value='${item.value.id}'>${item.value.value}</option>
					</c:forEach>
			</select>  <span class="input-group-addon">运营商</span><select id="operator" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.operators}"
						varStatus="status">
						<option value='${item.value.id}'>${item.value.value}</option>
					</c:forEach>
			</select> <input class="btn btn-primary btn-sm"
				value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
				type="submit" /> <input class="btn btn-primary" id="checkbox"
				onclick="check()" type="checkbox" /> <label for="checkbox"
				style="display: -webkit-inline-box">选择对比</label></div>
			</th>
		</tr>
	</table>
	<table id="history" style="display: none">
		<tr>
			<th align=left>
				<div class="input-group" style="float:left;">
						<span class="input-group-addon">开始</span>
					<input type="text" id="time2" style="width:110px;"/>
				 <span class="input-group-addon">页面</span> <select id="page2" style="width: 240px;">
					<c:forEach var="item" items="${model.appSpeedDisplayInfo.pages}" varStatus="status">
							<option value='${item}'>${item}</option>
					</c:forEach>
					</select> 
					 <span class="input-group-addon">阶段</span><select id="step2" style="width: 240px;">
					</select> 
					<span class="input-group-addon">网络类型</span> <select id="network2" style="width: 80px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.networks}" varStatus="status">
						<option value='${item.value.id}'>${item.value.value}</option>
					</c:forEach>
			</select></div>
			</th>
		</tr>
		<tr>
			<th align=left>
				<div class="input-group" style="float:left;">
				<span class="input-group-addon">版本</span> <select id="version2" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.versions}" varStatus="status">
						<option value='${item.value.id}'>${item.value.value}</option>
					</c:forEach>
			</select> <span class="input-group-addon">平台</span> <select id="platform2" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.platforms}"
						varStatus="status">
						<option value='${item.value.id}'>${item.value.value}</option>
					</c:forEach>
			</select> <span class="input-group-addon">地区</span> <select id="city2" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.cities}" varStatus="status">
						<option value='${item.value.id}'>${item.value.value}</option>
					</c:forEach>
			</select> <span class="input-group-addon">运营商</span> <select id="operator2" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.operators}"
						varStatus="status">
						<option value='${item.value.id}'>${item.value.value}</option>
					</c:forEach>
			</select></div>
			</th>
		</tr>
	</table>

	<div style="float: left; width: 100%;">
		<div id="${model.appSpeedDisplayInfo.lineChart.id}"></div>
	</div>
<h5 class="center text-success"><strong>计算规则：所选天中288个5分钟点数据求和再平均的结果</strong></h5>
<table id="web_content" class="table table-striped table-condensed table-bordered table-hover">
	<thead>
	<tr>
		<th class="right text-success"  width="10%">日期</th>
		<th class="right text-success" width="10%">访问次数</th>
		<th class="right text-success" width="10%">慢用户比例</th>
		<th class="right text-success" width="10%">延时(ms)</th>
		<c:if test="${fn:length(model.appSpeedDisplayInfo.appSpeedSummarys) gt 1}">
			<th class="right text-success" width="10%">对比日期</th>
			<th class="right text-success" width="10%">对比访问次数</th>
			<th class="right text-success" width="10%">对比慢用户比例</th>
			<th class="right text-success" width="10%">对比延时(ms)</th>
			<th class="right text-success" width="10%">变化比例</th>
		</c:if>
	</tr>
	</thead>
	<tbody>
	<c:set var="summarys" value="${model.appSpeedDisplayInfo.appSpeedSummarys}" />
		<c:forEach var="entry" items="${summarys['当前值']}" >
		<tr class="right">
	 		<td class="right">${entry.value.dayTime}</td>
			<td>${w:format(entry.value.accessNumberSum,'#,###,###,###,##0')}</td>
			<td>${w:format(entry.value.slowRatio,'#0.000')}%</td>
			<td>${w:format(entry.value.responseTimeAvg,'#,###,###,###,##0')}</td>
			<c:if test="${fn:length(model.appSpeedDisplayInfo.appSpeedSummarys) gt 1}">
				<c:set var="response" value="${summarys['对比值'][entry.value.minuteOrder].responseTimeAvg}" />
				<c:set var="ratio" value="${(entry.value.responseTimeAvg - response) / response * 100}" />
				<td class="right">${summarys['对比值'][entry.value.minuteOrder].dayTime}</td>
				<td>${w:format(summarys['对比值'][entry.value.minuteOrder].accessNumberSum,'#,###,###,###,##0')}</td>
				<td>${w:format(summarys['对比值'][entry.value.minuteOrder].slowRatio,'#0.000')}%</td>
				<td>${w:format(summarys['对比值'][entry.value.minuteOrder].responseTimeAvg,'#,###,###,###,##0')}</td>
				<c:choose>
				<c:when test="${ratio < 0 }">
				<td class="text-success">${w:format(ratio,'#0.000')}%</td>
				</c:when>
				<c:otherwise>
				<td class="text-danger">${w:format(ratio,'#0.000')}%</td>
				</c:otherwise>
				</c:choose>
			</c:if>
		</tr>
		</c:forEach>
	</tbody>
</table>
<h5 class="center text-success"><strong>计算规则：这个测速点5分钟内所有数据求和再平均的结果</strong></h5>
<table id="web_content" class="table table-striped table-condensed table-bordered table-hover">
	<thead>
	<tr>
		<th class="right text-success" width="10%">时间</th>
		<th class="right text-success" width="10%">访问次数</th>
		<th class="right text-success" width="10%">慢用户比例</th>
		<th class="right text-success" width="10%">延时(ms)</th>
		<c:if test="${fn:length(model.appSpeedDisplayInfo.appSpeedDetails) gt 1}">
			<th class="right text-success" width="10%">对比时间</th>
			<th class="right text-success" width="10%">对比访问次数</th>
			<th class="right text-success" width="10%">对比慢用户比例</th>
			<th class="right text-success" width="10%">对比延时(ms)</th>
			<th class="right text-success" width="10%">变化比例</th>
		</c:if>
	</tr></thead>
	<tbody id="details">
		<c:set var="details" value="${model.appSpeedDisplayInfo.appSpeedDetails}" />
		<c:forEach var="entry" items="${details['当前值']}" >
		<tr class="right" >
	 		<td class="right" width="10%">${entry.value.dateTime}</td>
			<td>${w:format(entry.value.accessNumberSum,'#,###,###,###,##0')}</td>
			<td>${w:format(entry.value.slowRatio,'#0.000')}%</td>
			<td>${w:format(entry.value.responseTimeAvg,'#,###,###,###,##0')}</td>
			<c:if test="${fn:length(model.appSpeedDisplayInfo.appSpeedDetails) gt 1}">
				<c:set var="response" value="${details['对比值'][entry.value.minuteOrder].responseTimeAvg}" />
				<c:set var="ratio" value="${(entry.value.responseTimeAvg - response) / response * 100}" />
				<td class="right">${details['对比值'][entry.value.minuteOrder].dateTime}</td>
				<td>${w:format(details['对比值'][entry.value.minuteOrder].accessNumberSum,'#,###,###,###,##0')}</td>
				<td>${w:format(details['对比值'][entry.value.minuteOrder].slowRatio,'#0.000')}%</td>
				<td>${w:format(details['对比值'][entry.value.minuteOrder].responseTimeAvg,'#,###,###,###,##0')}</td>
				<c:choose>
				<c:when test="${ratio < 0 }">
				<td class="text-success">${w:format(ratio,'#0.000')}%</td>
				</c:when>
				<c:otherwise>
				<td class="text-danger">${w:format(ratio,'#0.000')}%</td>
				</c:otherwise>
				</c:choose>
			</c:if>
		</tr>
		</c:forEach>
	</tbody>
</table>
