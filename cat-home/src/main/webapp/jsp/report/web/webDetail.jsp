<%@ page contentType="text/html; charset=utf-8"%>
<div class="report">
		<table>
			<tr>
				<th class="left">
				<div id="datetimepicker1" class="input-append date"
						style="margin-bottom: 0px;">
						<input id="startTime" name="time" style="height: 30px; width: 150px;"
							data-format="yyyy-MM-dd hh:mm" type="text"></input> <span
							class="add-on"> <i data-time-icon="icon-time"
							data-date-icon="icon-calendar"> </i>
						</span></div>结束
					<div id="datetimepicker2" class="input-append date"
						style="margin-bottom: 0px;">
						<input id="endTime" name="time2" style="height: 30px; width: 70px;"
							data-format="hh:mm" type="text"></input> <span
							class="add-on"> <i data-time-icon="icon-time"
							data-date-icon="icon-calendar"> </i>
						</span>
					</div>
				组&nbsp;<select style="width: 100px;" name="group" id="group">
				</select> URL <select style="width: 500px;" name="url" id="url"></select>
				省份 <select style="width: 100px;" name="province" id="province">
				</select> 城市 <select style="width: 100px;" name="city" id="city">
				</select> 
				</th>
				</tr>
				<tr>
				<th class="left">
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
				<input class="btn btn-primary "
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
					<div id="datetimepicker3" class="input-append date"
						style="margin-bottom: 0px;">
						<input id="startTime2" name="time" style="height: 30px; width: 150px;"
							data-format="yyyy-MM-dd hh:mm" type="text"></input> <span
							class="add-on"> <i data-time-icon="icon-time"
							data-date-icon="icon-calendar"> </i>
						</span></div>结束
					<div id="datetimepicker4" class="input-append date"
						style="margin-bottom: 0px;">
						<input id="endTime2" name="time2" style="height: 30px; width: 70px;"
							data-format="hh:mm" type="text"></input> <span
							class="add-on"> <i data-time-icon="icon-time"
							data-date-icon="icon-calendar"> </i>
						</span>
					</div>
				组&nbsp;<select style="width: 100px;" name="group2" id="group2">
				</select> URL <select style="width: 500px;" name="url2" id="url2"></select>
				省份 <select style="width: 100px;" name="province2" id="province2">
				</select> 城市 <select style="width: 100px;" name="city2" id="city2">
				</select>
				</th></tr> 
				<tr>
				<th class="left">
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
					<div style="float: left;">
						<div id="${item.id}" style="float: left; width: 95%;"></div>
					</div>
				</c:forEach></div>
				<div>
				<c:forEach var="item" items="${model.pieCharts}" varStatus="status">
					<div style="float: left;">
						<h5 class="text-center">${item.title}</h5>
						<div id="${item.title}" style="width:600px; height:450px;"></div>
					</div>
				</c:forEach></div>
			</c:when>
			<c:otherwise>
				<div class="row-fluid">
					<div class="span6">
						<div id="lineChart" style="float: left; width: 100%;"></div>
					</div>
					<div class="span6">
						<div id="pieChart" style="width:550px; height:400px;"></div>
					</div>
				</div>
			</c:otherwise>
		</c:choose>

		<table class="footer">
			<tr>
				<td>[ end ]</td>
			</tr>
		</table>
	</div>