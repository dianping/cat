<%@ page contentType="text/html; charset=utf-8"%>
<style>
.form-control {
  height: 30px;
}
</style>
<table width="100%">
			<tr>
				<th>
				<div class="input-group" style="float:left;width:130px">
	              <span class="input-group-addon">日期</span>
	              <input type="text" id="time" style="width:130px"/>
	            </div>
				<div class="input-group" style="float:left;width:350px">
					<span class="input-group-addon">链接</span>
		            <form id="wrap_search" style="margin-bottom:0px;">
						<span class="input-icon" style="width:350px;">
							<input type="text" placeholder="" class="search-input search-input form-control ui-autocomplete-input" id="command" autocomplete="on" data=""/>
							<i class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
					</form>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">返回码</span>
					<select id="codeStatus" style="width:120px">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.codes}">
							<option value="${item.value.id}">${item.value.name}</option>
						</c:forEach>
					</select>
	            </div>
	             <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">地区</span>
					<select id="city" style="width: 120px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.cities}" varStatus="status">
							<option value="${item.value.id}">${item.value.name}</option>
						</c:forEach>
					</select>
	            </div>
				</th>
			</tr>
			<tr>
				<th align=left>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">运营商</span>
					<select id="operator" style="width: 120px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.operators}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">网络类型</span>
					<select id="network" style="width: 120px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.networks}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
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
				<div class="input-group" style="float:left;width:130px">
	              <span class="input-group-addon">日期</span>
	              <input type="text" id="time2" style="width:130px"/>
	            </div>
				<div class="input-group" style="float:left;width:350px">
					<span class="input-group-addon">链接</span>
		            <form id="wrap_search2" style="margin-bottom:0px;">
						<span class="input-icon" style="width:350px;">
							<input type="text" placeholder="input domain for search" class="search-input search-input form-control ui-autocomplete-input" id="command2" autocomplete="on" data=""/>
							<i class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
					</form>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">返回码</span>
					<select id="codeStatus2" style="width:120px">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.codes}">
							<option value="${item.value.id}">${item.value.name}</option>
						</c:forEach>
					</select>
	            </div>
	             <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">地区</span>
					<select id="city2" style="width: 120px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.cities}" varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
					</select>
	            </div>
	            </th>
			</tr>
			<tr><th align=left>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">运营商</span>
					<select id="operator2" style="width: 120px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.operators}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
					</select>
	            </div>
	             <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">网络类型</span>
					<select id="network2" style="width: 120px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.networks}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
					</select>
	            </div>
			</th></tr>
		</table>
		<p/>
		<div>&nbsp;
			<label class="btn btn-info btn-sm"><input type="radio"
				name="typeCheckbox" value="request" checked>请求数
			</label><label class="btn btn-info btn-sm"> <input type="radio"
				name="typeCheckbox" value="success">成功率
			</label><label class="btn btn-info btn-sm">  <input type="radio"
				name="typeCheckbox" value="delay">成功延时
			</label>
		</div>

		<div style="float: left; width: 100%;">
			<div id="${model.ajaxDataDisplayInfo.lineChart.id}"></div>
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
		<c:forEach var="item" items="${model.ajaxDataDisplayInfo.comparisonAjaxDetails}">
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
		<th class="right text-success">地区</th>
		<th class="right text-success">运营商</th>
		<th class="right"><a href="javascript:queryGroupBy('success');">成功率</a>(%)</th>
		<th class="right"><a href="javascript:queryGroupBy('request');">总请求数</a></th>
		<th class="right"><a href="javascript:queryGroupBy('delay');">成功平均延迟</a>(ms)</th>
		<th class="right"><a href="javascript:queryGroupBy('requestPackage');">平均发包</a>(B)</th>
		<th class="right"><a href="javascript:queryGroupBy('responsePackage');">平均回包</a>(B)</th>
	</tr></thead>
	<tbody>
	<c:forEach var="item" items="${model.ajaxDataDisplayInfo.ajaxDataDetailInfos}" varStatus="status">
		<tr class="right">
		<c:set var="networkCode" value="${item.network eq '-1' ? '' : item.network}"/>
		<c:set var="cityCode" value="${item.city eq '-1' ? '' : item.city}"/>
		<c:set var="operatorCode" value="${item.operator eq '-1' ? '' : item.operator}"/>
		<c:set var="network" value="${model.networks[networkCode].name}"/>
		<c:set var="city" value="${model.cities[cityCode].name}"/>
		<c:set var="operator" value="${model.operators[operatorCode].name}"/>
		
		<c:choose>
			<c:when test="${empty networkCode}">
				<td><button class="btn btn-xs btn-info" onclick="query('network', '${networkCode}','${cityCode}','${operatorCode}');">展开⬇</button></td>
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
		<c:when test="${empty cityCode}">
				<td><button class="btn btn-xs btn-info" onclick="query('city', '${networkCode}', '${cityCode}','${operatorCode}');">展开⬇</button></td>
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
				<td><button class="btn btn-xs btn-info" onclick="query('operator', '${networkCode}', '${cityCode}','${operatorCode}');">展开⬇</button></td>
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