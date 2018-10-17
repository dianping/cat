<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<style>
.form-control {
  height: 30px;
}
</style>
<table>
	<tr>
			<th>
				<div class="input-group" style="float:left;width:110px">
	              <span class="input-group-addon">开始</span>
	              <input type="text" id="time"/>
	            </div>
				<div class="input-group" style="float:left;width:60px">
	              <span class="input-group-addon">结束</span>
        	      <input type="text" id="time2" style="width:60px;"/></div>
	            </div>
				<div class="input-group" style="float:left;width:200px">
					<span class="input-group-addon">命令字</span>
		            <form id="wrap_search" style="margin-bottom:0px;">
						<span class="input-icon" style="width:200px;">
							<input type="text" placeholder="" class="search-input search-input form-control ui-autocomplete-input" id="command" autocomplete="on" data=""/>
							<i class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
					</form>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">返回码</span>
					<select id="code" style="width:120px"><option value=''>All</option></select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">网络类型</span>
					<select id="network">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.networks}" varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
				</select>
	            </div>
			</th>
			</tr>
			<tr>
				<th align=left>
				<div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">版本</span>
					<select id="app-version" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.versions}" varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">连接类型</span>
					<select id="connect-type" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.cipConnectionTypes}"
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
	             <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">饼图展开</span>
					<select id="piechartSelect" style="width: 100px;">
						<option value='code'>返回码</option>
						<option value='network'>网络类型</option>
						<option value='app-version'>版本</option>
						<option value='connect-type'>连接类型</option>
						<option value='platform'>平台</option>
						<option value='city'>地区</option>
						<option value='operator'>运营商</option>
				</select>
	            </div>
	            <input class="btn btn-primary btn-sm"
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" />
				</th>
			</tr>
		</table>
		<h5 class="text-center">请求量分布</h5>
		<div id="piechart"></div>
		<br/>
	<table id="web_content" class="table table-striped table-condensed">
		<thead><tr class="text-success">
		<c:if test="${payload.groupByField.name eq 'code'}">
		<th width="20%" colspan="2">返回码 (默认设置无法编辑)</th>
		</c:if>
		<th>类别</th>
		<th>请求总数</th>
		<th>百分比</th>
		
	</tr></thead>
	<tbody>
	<c:forEach var="item" items="${model.connDisplayInfo.pieChartDetailInfo.requestSortedItems}" varStatus="status">
		<tr>
		<c:if test="${payload.groupByField.name eq 'code'}">
			<c:choose>
			<c:when test="${model.codes[item.id] != null}">
				<td width="5%">${item.id}</td><td><a  class="btn btn-xs" href="/cat/s/config?op=appCodeUpdate&id=${model.commandId}&code=${item.id}">编辑</a></td>  
			</c:when>
			<c:otherwise>
				<td colspan="2">${item.id}</td>  
			</c:otherwise>
			</c:choose>
		</c:if>
		<td>${item.title}</td>
		<td>${w:format(item.requestSum,'#,###,###,###,##0')}</td>
		<td>${w:format(item.ratio,'#0.000%')}</td>
		
		</tr>
	</c:forEach>
	</tbody>
</table>
