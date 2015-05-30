<%@ page contentType="text/html; charset=utf-8"%>
<style>
.form-control {
  height: 30px;
}
</style>
<table width="100%">
			<tr>
				<th>
				<div class="input-group" style="float:left;width:120px">
	              <span class="input-group-addon">日期</span>
	              <input type="text" id="time" style="width:100px"/>
	            </div>
				<div class="input-group" style="float:left;width:250px">
					<span class="input-group-addon">链接</span>
		            <form id="wrap_search" style="margin-bottom:0px;">
						<span class="input-icon" style="width:250px;">
							<input type="text" placeholder="" class="search-input search-input form-control ui-autocomplete-input" id="command" autocomplete="on" data=""/>
							<i class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
					</form>
	            </div>
	            <div class="input-group" style="float:left;width:100px">
	              	<span class="input-group-addon">返回码</span>
					<select id="code" style="width:100px">
						<option value=''>All</option>
						<c:forEach var="code" items="${model.codes}">
							<option value="${code.value.id}">${code.value.name}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">地区</span>
					<select id="city" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.cities}" varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">运营商</span>
					<select id="operator" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.operators}"
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
				<div class="input-group" style="float:left;width:120px">
	              <span class="input-group-addon">日期</span>
	              <input type="text" id="time2" style="width:100px"/>
	            </div>
				<div class="input-group" style="float:left;width:250px">
					<span class="input-group-addon">链接</span>
		            <form id="wrap_search2" style="margin-bottom:0px;">
						<span class="input-icon" style="width:250px;">
							<input type="text" placeholder="input domain for search" class="search-input search-input form-control ui-autocomplete-input" id="command2" autocomplete="on" data=""/>
							<i class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
					</form>
	            </div>
	            <div class="input-group" style="float:left;width:100px">
	              	<span class="input-group-addon">返回码</span>
					<select id="code2" style="width:100px">
						<option value=''>All</option>
						<c:forEach var="code" items="${model.codes}">
							<option value="${code.value.id}">${code.value.name}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">地区</span>
					<select id="city2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.cities}" varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">运营商</span>
					<select id="operator2" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.operators}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
					</select>
	            </div>
	            </th>
			</tr>
		</table>
		<p/>
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
<%-- <table id="web_content" class="table table-striped table-condensed table-bordered table-hover">
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
</table> --%>