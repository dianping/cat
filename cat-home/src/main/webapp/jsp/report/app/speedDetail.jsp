<%@ page contentType="text/html; charset=utf-8"%>
<table >
			<tr>
				<th align=left>
					<div id="datetimepicker1" class="input-append  date" style="float:left;">
			           时间<input id="time" name="time"  size="13" class="{required:true,date:true}"
			              data-format="yyyy-MM-dd" type="text"></input> <span class="add-on"  >
			              <i class="ace-icon fa fa-calendar"></i>
			           </span>
			        </div>
						&nbsp;&nbsp;页面 <select id="page" style="width: 240px;">
						<c:forEach var="item" items="${model.pages}" varStatus="status">
								<option value='${item}'>${item}</option>
						</c:forEach>
						</select> 
						阶段 <select id="step" style="width: 240px;">
						</select> 
						 网络类型 <select id="network" style="width: 80px;">
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
					type="submit" /> <input class="btn btn-primary" id="checkbox"
					onclick="check()" type="checkbox" /> <label for="checkbox"
					style="display: -webkit-inline-box">选择对比</label>
				</th>
			</tr>
		</table>
		<table id="history" style="display: none">
			<tr>
				<th align=left>
					<div id="datetimepicker2" class="input-append  date" style="float:left;">
			           时间<input id="time2" name="time2"  size="13" class="{required:true,date:true}"
			              data-format="yyyy-MM-dd" type="text"></input> <span class="add-on"  >
			              <i class="ace-icon fa fa-calendar"></i>
			           </span>
			        </div>
					 &nbsp;&nbsp;页面 <select id="page2" style="width: 240px;">
						<c:forEach var="item" items="${model.pages}" varStatus="status">
								<option value='${item}'>${item}</option>
						</c:forEach>
						</select> 
						阶段 <select id="step2" style="width: 240px;">
						</select> 
						网络类型 <select id="network2" style="width: 80px;">
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

		<div style="float: left; width: 100%;">
			<div id="${model.appSpeedDisplayInfo.lineChart.id}"></div>
		</div>
		<br/>
<table id="web_content" class="table table-striped table-bordered table-condensed table-hover">
	<thead>
	<tr>
	<c:choose>
		<c:when test="${fn:length(model.appSpeedDisplayInfo.appSpeedSummarys) gt 1}">
		<td colspan="4">
		</c:when>
		<c:otherwise>
		<td colspan="9">
		</c:otherwise>
	</c:choose>
	<span class="text-success">计算规则：</span>下面数据是从一天中288个5分钟点数据求和再平均的结果
	</td>
	</tr>
	<tr class="text-success">
		<th style="text-align: center">时间</th>
		<th style="text-align: center">访问次数</th>
		<th style="text-align: center">慢用户比例</th>
		<th style="text-align: center">延时(ms)</th>
		<c:if test="${fn:length(model.appSpeedDisplayInfo.appSpeedSummarys) gt 1}">
			<th style="text-align: center">对比时间</th>
			<th style="text-align: center">对比访问次数</th>
			<th style="text-align: center">对比慢用户比例</th>
			<th style="text-align: center">对比延时(ms)</th>
			<th style="text-align: center">变化比例</th>
		</c:if>
	</tr>
	</thead>
	<tbody>
	<c:set var="summarys" value="${model.appSpeedSummarys}" />
		<c:forEach var="entry" items="${summarys['当前值']}" >
		<tr class="${status.index  mod 2==1 ? 'even' : 'odd'} right">
	 		<td style="text-align: center">${entry.value.dayTime}</td>
			<td>${w:format(entry.value.accessNumberSum,'#,###,###,###,##0')}</td>
			<td>${w:format(entry.value.slowRatio,'#0.000')}%</td>
			<td>${w:format(entry.value.responseTimeAvg,'#,###,###,###,##0')}</td>
			<c:if test="${fn:length(model.appSpeedDisplayInfo.appSpeedSummarys) gt 1}">
				<c:set var="response" value="${summarys['对比值'][entry.value.minuteOrder].responseTimeAvg}" />
				<c:set var="ratio" value="${(entry.value.responseTimeAvg - response) / response * 100}" />
				<td style="text-align: center">${summarys['对比值'][entry.value.minuteOrder].dayTime}</td>
				<td>${w:format(summarys['对比值'][entry.value.minuteOrder].accessNumberSum,'#,###,###,###,##0')}</td>
				<td>${w:format(summarys['对比值'][entry.value.minuteOrder].slowRatio,'#0.000')}%</td>
				<td>${w:format(summarys['对比值'][entry.value.minuteOrder].responseTimeAvg,'#,###,###,###,##0')}</td>
				<c:choose>
				<c:when test="${ratio < 0 }">
				<td class="text-success">${w:format(ratio,'#0.000')}%</td>
				</c:when>
				<c:otherwise>
				<td class="text-error">${w:format(ratio,'#0.000')}%</td>
				</c:otherwise>
				</c:choose>
			</c:if>
		</tr>
		</c:forEach>
	</tbody>
</table>

<table id="web_content" class="table table-striped table-bordered table-condensed table-hover">
	<thead>
	<tr>
	<c:choose>
		<c:when test="${fn:length(model.appSpeedDisplayInfo.appSpeedSummarys) gt 1}">
		<td colspan="4">
		</c:when>
		<c:otherwise>
		<td colspan="9">
		</c:otherwise>
	</c:choose>
	<span class="text-success">计算规则：</span>下面数据是这个测速点5分钟内所有数据求和再平均的结果
	</td>
	</tr>
	<tr class="text-success">
		<th style="text-align: center">时间</th>
		<th style="text-align: center">访问次数</th>
		<th style="text-align: center">慢用户比例</th>
		<th style="text-align: center">延时(ms)</th>
		<c:if test="${fn:length(model.appSpeedDisplayInfo.appSpeedDetails) gt 1}">
			<th style="text-align: center">对比时间</th>
			<th style="text-align: center">对比访问次数</th>
			<th style="text-align: center">对比慢用户比例</th>
			<th style="text-align: center">对比延时(ms)</th>
			<th style="text-align: center">变化比例</th>
		</c:if>
	</tr></thead>
	<tbody id="details">
		<c:set var="details" value="${model.appSpeedDetails}" />
		<c:forEach var="entry" items="${details['当前值']}" >
		<tr class="${status.index  mod 2==1 ? 'even' : 'odd'} right" >
	 		<td style="text-align: center">${entry.value.dateTime}</td>
			<td>${w:format(entry.value.accessNumberSum,'#,###,###,###,##0')}</td>
			<td>${w:format(entry.value.slowRatio,'#0.000')}%</td>
			<td>${w:format(entry.value.responseTimeAvg,'#,###,###,###,##0')}</td>
			<c:if test="${fn:length(model.appSpeedDisplayInfo.appSpeedDetails) gt 1}">
				<c:set var="response" value="${details['对比值'][entry.value.minuteOrder].responseTimeAvg}" />
				<c:set var="ratio" value="${(entry.value.responseTimeAvg - response) / response * 100}" />
				<td style="text-align: center">${details['对比值'][entry.value.minuteOrder].dateTime}</td>
				<td>${w:format(details['对比值'][entry.value.minuteOrder].accessNumberSum,'#,###,###,###,##0')}</td>
				<td>${w:format(details['对比值'][entry.value.minuteOrder].slowRatio,'#0.000')}%</td>
				<td>${w:format(details['对比值'][entry.value.minuteOrder].responseTimeAvg,'#,###,###,###,##0')}</td>
				<c:choose>
				<c:when test="${ratio < 0 }">
				<td class="text-success">${w:format(ratio,'#0.000')}%</td>
				</c:when>
				<c:otherwise>
				<td class="text-error">${w:format(ratio,'#0.000')}%</td>
				</c:otherwise>
				</c:choose>
			</c:if>
		</tr>
		</c:forEach>
	</tbody>
</table>
