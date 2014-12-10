<%@ page contentType="text/html; charset=utf-8"%>
<div class="report">
		<table width="100%">
			<tr>
				<th class="left">
				<div id="datetimepicker1" class="input-append  date" style="margin-bottom: 0px;float:left;">
		           开始<input id="startTime" name="time"  size="16" class="{required:true,date:true}"
		              data-format="yyyy-MM-dd HH:mm" type="text"></input> <span class="add-on">
		              <i class="ace-icon fa fa-calendar"></i>
		           </span>
		        </div>
		        <div id="datetimepicker2" class="input-append  date" style="margin-bottom: 0px;float:left;">
		           开始<input id="endTime" name="time2"  size="8" class="{required:true,date:true}"
		              data-format="HH:mm" type="text"></input> <span class="add-on">
		              <i class="ace-icon fa fa-calendar"></i>
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
					<div id="datetimepicker3" class="input-append  date" style="margin-bottom: 0px;float:left;">
		           开始<input id="startTime2" name="time"  size="16" class="{required:true,date:true}"
		              data-format="yyyy-MM-dd HH:mm" type="text"></input> <span class="add-on">
		              <i class="ace-icon fa fa-calendar"></i>
		           </span>
		        </div>
		        <div id="datetimepicker4" class="input-append  date" style="margin-bottom: 0px;float:left;">
		           开始<input id="endTime2" name="time2"  size="8" class="{required:true,date:true}"
		              data-format="HH:mm" type="text"></input> <span class="add-on">
		              <i class="ace-icon fa fa-calendar"></i>
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
						<div id="${item.id}" style="float: left; width: 98%;"></div>
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
					<div id="lineChart" style="float: left; width: 98%;"></div>
					<div id="pieChart" style="float: left; width: 98%;"></div>
			</c:otherwise>
		</c:choose>
	</div>