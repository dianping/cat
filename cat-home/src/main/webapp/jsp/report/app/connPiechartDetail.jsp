<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<table>
			<tr>
				<th class="left">
				<div style="float:left;">
						&nbsp;开始
					<input type="text" id="time" style="width:150px;"/>
						结束
						<input type="text" id="time2" style="width:60px;"/></div>
		        &nbsp;项目<select id="group" style="width: 200px;"></select>
		         命令字 <select id="command" style="width: 200px;">
						
				</select> 返回码 <select id="code" style="width: 100px;"><option value=''>All</option>
				</select> 网络类型 <select id="network" style="width: 80px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.networks}" varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select>
				</th>
			</tr>
			<tr>
				<th align=left>&nbsp;版本 <select id="app-version" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.versions}" varStatus="status">
							<option value='${item.value.id}'>${item.value.name}</option>
						</c:forEach>
				</select> 连接类型 <select id="connnect-type" style="width: 100px;">
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
				</select> 饼图展开 <select id="piechartSelect" style="width: 100px;">
						<option value='code'>返回码</option>
						<option value='network'>网络类型</option>
						<option value='app-version'>版本</option>
						<option value='connnect-type'>连接类型</option>
						<option value='platform'>平台</option>
						<option value='city'>地区</option>
						<option value='operator'>运营商</option>
				</select>
				
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
	<c:forEach var="item" items="${model.pieChartDetailInfos}" varStatus="status">
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
		<td>${w:format(item.successRatio,'#0.000%')}</td>
		
		</tr>
	</c:forEach>
	</tbody>
</table>
