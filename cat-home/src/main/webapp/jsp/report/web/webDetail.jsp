<%@ page contentType="text/html; charset=utf-8"%>
		<table width="100%">
			<tr>
				<th class="left">
					<div style="float:left;">
						&nbsp;开始
					<input type="text" id="startTime" style="width:150px;"/>
						结束
						<input type="text" id="endTime" style="width:60px;"/></div>
				&nbsp;组&nbsp;<select style="width: 100px;" name="group" id="group">
				</select> URL <select style="width: 600px;" name="url" id="url"></select>
				</th>
				</tr>
				<tr>
				<th class="left">
				&nbsp;省份 <select style="width: 100px;" name="province" id="province">
				</select> 城市 <select style="width: 100px;" name="city" id="city">
				</select> 
				运营商 <select style="width: 100px;" name="channel" id="channel">
						<option value="">ALL</option>
						<option value="中国电信">中国电信</option>
						<option value="中国移动">中国移动</option>
						<option value="中国联通">中国联通</option>
						<option value="中国铁通">中国铁通</option>
						<option value="其他">其他</option>
						<option value="国外其他">国外其他</option>
				</select>
				查询类型 <select style="width: 100px;" name="type" id="type">
						<option value="info">访问情况</option>
						<option value="httpStatus">Http返回码</option>
						<option value="errorCode">错误码</option>
				</select>
				<input class="btn btn-sm btn-primary "
					value="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
					onclick="query()" type="submit">&nbsp;<input class="btn btn-primary" id="checkbox"
					onclick="check()" type="checkbox" /> <label for="checkbox" id="checkboxLabel"
					style="display: -webkit-inline-box">选择对比</label>
				</th>
			</tr>
		</table>
		<table id="history" style="display: none">
			<tr>
				<th class="left">
				<div style="float:left;">
						&nbsp;开始
					<input type="text" id="startTime2" style="width:150px;"/>
						结束
						<input type="text" id="endTime2" style="width:60px;"/>
				</div>
				&nbsp;组&nbsp;<select style="width: 100px;" name="group2" id="group2">
				</select> URL <select style="width: 600px;" name="url2" id="url2"></select>
				</th></tr> 
				<tr>
				<th class="left">
				&nbsp;省份 <select style="width: 100px;" name="province2" id="province2">
				</select> 城市 <select style="width: 100px;" name="city2" id="city2">
				</select>
				运营商 <select style="width: 100px;" name="channel2" id="channel2">
						<option value="">ALL</option>
						<option value="中国电信">中国电信</option>
						<option value="中国移动">中国移动</option>
						<option value="中国联通">中国联通</option>
						<option value="中国铁通">中国铁通</option>
						<option value="其他">其他</option>
						<option value="国外其他">国外其他</option>
				</select>
				</th>
			</tr>
		</table>
		<c:choose>
			<c:when test="${payload.type eq 'info'}">
				<div>
				<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
					<div style="float: left;width: 100%;">
						<div id="${item.id}"></div>
					</div>
				</c:forEach></div>
				<div>
				<c:forEach var="item" items="${model.pieCharts}" varStatus="status">
					<div style="float: left;">
						<h5 class="text-center">${item.title}</h5>
						<div id="${item.title}" style="width:500px; height:450px;"></div>
					</div>
				</c:forEach></div>
			</c:when>
			<c:otherwise>
			<div style="float: left; width: 100%;">
					<div id="lineChart"></div>
					<div id="pieChart"></div>
			</div>
			</c:otherwise>
		</c:choose>