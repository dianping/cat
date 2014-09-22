<%@ page contentType="text/html; charset=utf-8"%>
<table>
			<tr>
				<th align=left>开始
					<div id="datetimepicker1" class="input-append date"
						style="margin-bottom: 0px;">
						<input id="time" name="time" style="height: 30px; width: 150px;"
							data-format="yyyy-MM-dd hh:mm" type="text"></input> <span
							class="add-on"> <i data-time-icon="icon-time"
							data-date-icon="icon-calendar"> </i>
						</span></div>结束
					<div id="datetimepicker2" class="input-append date"
						style="margin-bottom: 0px;">
						<input id="time2" name="time2" style="height: 30px; width: 70px;"
							data-format="hh:mm" type="text"></input> <span
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
				<th align=left>版本 <select id="app-version" style="width: 100px;">
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
				
				 <input class="btn btn-primary "
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" />
				</th>
			</tr>
		</table>
		<h5 class="text-center">请求量分布</h5>
		<div id="piechart"></div>
		<br/>
<table id="web_content" class="table table-striped table-bordered table-condensed">
	<thead><tr class="text-success">
		<th>类别</th>
		<th>请求总数</th>
		<th>百分比</th>
	</tr></thead>
	<tbody>
	<c:forEach var="item" items="${model.pieChartDetailInfos}" varStatus="status">
		<tr>
		<td>${item.title}</td>
		<td></a>${w:format(item.requestSum,'#,###,###,###,##0')}</td>
		<td>${w:format(item.successRatio,'#0.000%')}</td>
		</tr>
	</c:forEach>
	</tbody>
</table>
