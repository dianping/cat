<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.app.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.app.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.app.Model" scope="request"/>

<a:body>
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useCss value="${res.css.local['bootstrap-datetimepicker.min.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
	<script type="text/javascript">
		function check(){
			var value = document.getElementById("checkbox").checked;
			
			if(value==true){
				$('#history').slideDown();
			}else{
				$('#history').slideUp();
			}
		};
	</script>
	<div class="report">
		<table>
			<tr>
				<th >
				时间
				<div id="datetimepicker1" class="input-append date"
					style="margin-bottom: 0px;">
					<input id="startTime" name="startTime"
						style="height: 30px; width: 150px;"
						data-format="yyyy-MM-dd hh:mm" type="text"></input> <span
						class="add-on"> <i data-time-icon="icon-time"
						data-date-icon="icon-calendar"> </i>
					</span>
				</div>
				业务线
				<select style="width: 80px;"></select>
				命令字
				<select style="width: 250px;"></select>
				返回码
				<select style="width: 80px;"></select>
				网络类型
				<select style="width: 80px;"></select>
				</th>
			</tr>
			<tr>
				<th align=left>
				版本
				<select  style="width: 100px;"></select>
				渠道
				<select  style="width: 100px;"></select>
				平台
				<select  style="width: 100px;"></select>
				地区
				<select  style="width: 100px;"></select>
				运营商
				<select  style="width: 100px;"></select>
				<input class="btn btn-primary "
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;"
					onclick="query()" type="submit"/>
				<input class="btn btn-primary" id="checkbox"
					onclick="check()" type="checkbox"/> <label for="checkbox" style="display:-webkit-inline-box">选择历史对比</label> 
				</th>
			</tr>
		</table>
		<table id="history" style="display:none">
			<tr>
				<th align=left>
				时间
				<div id="datetimepicker1" class="input-append date"
					style="margin-bottom: 0px;">
					<input id="startTime" name="startTime"
						style="height: 30px; width: 150px;"
						data-format="yyyy-MM-dd hh:mm" type="text"></input> <span
						class="add-on"> <i data-time-icon="icon-time"
						data-date-icon="icon-calendar"> </i>
					</span>
				</div>
				业务线
				<select style="width: 80px;"></select>
				命令字
				<select style="width: 250px;"></select>
				返回码
				<select style="width: 80px;"></select>
				网络类型
				<select style="width: 80px;"></select>
				</th>
			</tr>
			<tr>
				<th align=left>
				版本
				<select  style="width: 100px;"></select>
				渠道
				<select  style="width: 100px;"></select>
				平台
				<select  style="width: 100px;"></select>
				地区
				<select  style="width: 100px;"></select>
				运营商
				<select  style="width: 100px;"></select>

				</th>
			</tr>
		</table>

		<table class="footer">
			<tr>
				<td>[ end ]</td>
			</tr>
		</table>
	</div>
</a:body>