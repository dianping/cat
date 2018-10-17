<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.applog.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.applog.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.applog.Model" scope="request" />
<a:mobile>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/assets/css/select2.css"/>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/assets/css/chosen.css"/>
	
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<script src="${model.webapp}/assets/js/select2.min.js"></script>
	<script src="${model.webapp}/assets/js/chosen.jquery.min.js"></script>
		<div class="report">
		<table class="table ">
		<tr>
			<td colspan="2">
				<div class="input-group" style="float:left;">
	              <span class="input-group-addon">开始</span>
	              <input type="text" id="time" style="width:130px"/>
	            </div>
				<div class="input-group" style="float:left;width:60px">
	              <span class="input-group-addon">结束</span>
        	      <input type="text" id="time2" style="width:60px;"/></div>
        	    <div class="input-group" style="float:left;width:60px">
	              <span class="input-group-addon">平台</span>  
				<select id="platform" style="width: 100px; height:33px">
					<option value="1">Android</option>
					<option value="2">IOS</option>
				</select></div>
				  <div class="input-group" style="float:left;width:30px">
	              <span class="input-group-addon">App</span>  
				<select id="appName" style="width: 120px; height:33px">
						<c:forEach var="appName" items="${model.appLogDisplayInfo.appNames}">
							<option value="${appName.id}">${appName.value}</option>
						</c:forEach>
				</select></div>
				    <div class="input-group" style="float:left;">
					<span class="input-group-addon">UnionId</span>
					<input type="text"  id="unionId" />
	            </div>
					&nbsp;&nbsp;&nbsp;<input class="btn btn-primary btn-sm "
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" /></td>
			<td>
				<div class="nav-search nav" id="nav-search">
				&nbsp;[ <a href="${model.baseUri}?op=appLog&appLogQuery.day=${payload.appLogQuery.day}&appLogQuery.appName=${payload.appLogQuery.appName}&appLogQuery.unionId=${payload.appLogQuery.unionId}&appLogQuery.query=${payload.appLogQuery.query}&appLogQuery.step=-1&appLogQuery.platform=${payload.appLogQuery.platform}">-1d</a> ]&nbsp;
				&nbsp;[ <a href="${model.baseUri}?op=appLog&appLogQuery.day=${payload.appLogQuery.day}&appLogQuery.appName=${payload.appLogQuery.appName}&appLogQuery.unionId=${payload.appLogQuery.unionId}&appLogQuery.query=${payload.appLogQuery.query}&appLogQuery.step=1&appLogQuery.platform=${payload.appLogQuery.platform}">+1d</a> ]&nbsp;
				&nbsp;[ <a href="${model.baseUri}?op=appLog&appLogQuery.appName=${payload.appLogQuery.appName}&appLogQuery.unionId=${payload.appLogQuery.unionId}&appLogQuery.query=${payload.appLogQuery.query}&appLogQuery.platform=${payload.appLogQuery.platform}">now</a> ]&nbsp;
				</div>
			</td>
			</tr>
			<tr><td width="100px;">APP版本</td><td>
				<div>
				<label class="btn btn-info btn-sm">
    				<input type="checkbox" id="appVersionAll" onclick="clickAll('${model.appLogDisplayInfo.fieldsInfo.appVersions}', 'appVersion')" unchecked>All
  				</label><c:forEach var="item" items="${model.appLogDisplayInfo.fieldsInfo.appVersions}" varStatus="status"><label class="btn btn-info btn-sm"><input type="checkbox" id="appVersion_${item}" value="${item}" onclick="clickMe('${model.appLogDisplayInfo.fieldsInfo.appVersions}', 'appVersion')" unchecked>${item}</label></c:forEach>
				</div>
				</td></tr>
			<tr><td width="60px;">平台版本</td><td><div><label class="btn btn-info btn-sm">
    				<input type="checkbox" id="platformVersionAll" onclick="clickAll('${model.appLogDisplayInfo.fieldsInfo.platVersions}', 'platformVersion')" unchecked>All
  				</label><c:forEach var="item" items="${model.appLogDisplayInfo.fieldsInfo.platVersions}" varStatus="status"><label class="btn btn-info btn-sm"><input type="checkbox" id="platformVersion_${item}" value="${item}" onclick="clickMe('${model.appLogDisplayInfo.fieldsInfo.platVersions}', 'platformVersion')" unchecked>${item}</label></c:forEach>
				</div>
				</td></tr>
			<tr><td width="60px;"> 级别</td><td><div>
				<label class="btn btn-info btn-sm">
    				<input type="checkbox" id="levelAll" onclick="clickAll('${model.appLogDisplayInfo.fieldsInfo.levels}', 'level')"  unchecked>All
  				</label><c:forEach var="item" items="${model.appLogDisplayInfo.fieldsInfo.levels}" varStatus="status"><label class="btn btn-info btn-sm"><input type="checkbox" id="level_${item}" value="${item}" onclick="clickMe('${model.appLogDisplayInfo.fieldsInfo.levels}', 'level')" unchecked>${item}</label></c:forEach>
				</div>
				</td></tr>
			<tr><td width="60px;">设备</td><td>
				<select multiple="true"	class="chosen-select tag-input-style" id="device_select" name="devices"  data-placeholder="Choose devices...">
				<option id="device_all" value="device_all">ALL</option>
				<c:forEach var="item" items="${model.appLogDisplayInfo.fieldsInfo.devices}">
					<option id="${item}" value="${item}">${item}</option>
				</c:forEach>
				</select>
				</td></tr>
	</table>
	</div>

<div class="tabbable">
 <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" style="height: 50px;">
    <li class="active"><a href="#report" data-toggle="tab"><strong>详细日志</strong></a></li>
  </ul>
  
  <div class="tab-content">
 <div class="tab-pane active" id="report">
	<table class="table table-hover table-striped table-condensed">
	<tr>
		<th width="40%">Msg</th>
		<th width="5%">Count</th>
		<th width="30%">SampleLinks</th>
	</tr>
	<c:forEach var="error" items="${model.appLogDisplayInfo.msgs}" varStatus="typeIndex">
	<tr>
		<td>
		<a href="" class="app_graph_link" data-status="${typeIndex.index}" msg="${error.msg}">[:: show ::]</a>
		${error.msg}</td>
		<td  class="right">${w:format(error.count,'#,###,###,###,##0')}&nbsp;</td>
		<td >
			<c:forEach var="id" items="${error.ids}" varStatus="linkIndex">
				<a href="/cat/r/applog?op=appLogDetail&id=${id}">${linkIndex.first?'L':(linkIndex.last?'g':'o')}</a>
			</c:forEach>
		</td>
	</tr>
	<tr></tr>
	<tr class="graphs"><td colspan="5" style="display:none"><div id="${typeIndex.index}" style="display:none"></div></td></tr>
	</c:forEach>
</table></div>
</div>
</div>
</a:mobile>

<script type="text/javascript">
	function query(){
 		window.location.href = "?op=appLog" + getQueryParams();
	}
	
	function getQueryParams() {
		var time = $("#time").val();
		var times = time.split(" ");
		var period = times[0];
		var start = converTimeFormat(times[1]);
		var end = converTimeFormat($("#time2").val());
		var unionId = $("#unionId").val();
		var appName = $("#appName").val();
		var platform = $("#platform").val();
		
 		var appVersion = queryField('${model.appLogDisplayInfo.fieldsInfo.appVersions}','appVersion');
		var platVersion = queryField('${model.appLogDisplayInfo.fieldsInfo.platVersions}','platformVersion');
		var level = queryField('${model.appLogDisplayInfo.fieldsInfo.levels}','level');
		var device = queryDevice();
		var split = ";";
		var query = appVersion + split + platVersion + split + level + split + device;
		
 		return "&appLogQuery.day=" + period + "&appLogQuery.startTime=" + start + "&appLogQuery.endTime=" + end
			 + "&appLogQuery.appName=" + appName + "&appLogQuery.platform=" + platform + "&appLogQuery.unionId=" + unionId + "&appLogQuery.query=" + query;
	}
	
	function queryDevice() {
		var device = "";
		$('.search-choice').each(function(){
			var o = $(this).children("span").eq(0).html();
			if (o == 'ALL') {
				device = "";
				return;
			} else {
				device += o + ":";
			}
		});
		return device;
	}
	
	$("#appName")
	  .change(function () {
			var time = $("#time").val();
			var times = time.split(" ");
			var period = times[0];
			var start = converTimeFormat(times[1]);
			var end = converTimeFormat($("#time2").val());
			var unionId = $("#unionId").val();
			var appName = $("#appName").val();
			var platform = $("#platform").val();
			
	 		window.location.href = "?op=appLog&appLogQuery.day=" + period + "&appLogQuery.startTime=" + start + "&appLogQuery.endTime=" + end
			 + "&appLogQuery.appName=" + appName + "&appLogQuery.platform=" + platform + "&appLogQuery.unionId=" + unionId ;

	  })
	  
	$(document).ready(
		function() {
			$('#App_report').addClass("active open");
			$('#appLog').addClass('active');
			$('#time').datetimepicker({
				format:'Y-m-d H:i',
				step:30,
				maxDate:0
			});
			$('#time2').datetimepicker({
				datepicker:false,
				format:'H:i',
				step:30,
				maxDate:0
			});
			
			var startTime = '${payload.appLogQuery.startTime}';
			if (startTime == null || startTime.length == 0) {
				$("#time").val(getDate());
			} else {
				$("#time").val('${payload.appLogQuery.day} ' + startTime);
			}
			
			var endTime = '${payload.appLogQuery.endTime}';
			if (endTime == null || endTime.length == 0){
				$("#time2").val(getTime());
			}else{
				$("#time2").val(endTime);
			}
			
			var platform = '${payload.appLogQuery.platform}';
			if (platform != null && platform.length != 0) {
				$("#platform").val(platform);
			}	
			
			var appName = '${payload.appLogQuery.appName}';
			if (appName != null && appName.length != 0) {
				$("#appName").val(appName);
			}
			
			var unionId = '${payload.appLogQuery.unionId}';
			if (unionId != null && unionId.length != 0) {
				$("#unionId").val(unionId);
			}
			
			var fields = "${payload.appLogQuery.query}".split(";");
			docReady(fields[0], '${model.appLogDisplayInfo.fieldsInfo.appVersions}','appVersion');
			docReady(fields[1], '${model.appLogDisplayInfo.fieldsInfo.platVersions}','platformVersion');
			docReady(fields[2], '${model.appLogDisplayInfo.fieldsInfo.levels}','level');
			
			$("#device_select").select({
				placeholder : "选择执行任务的设备",
				allowClear : true
			});
			
			if(typeof fields[3] == "undefined" || fields[3].length == 0){
				$('#device_all').attr("selected", "true");
			}else{
				urls = fields[3].split(":");
				for(var i=0; i<urls.length; i++) {
					var deviceid = urls[i];
					if(deviceid != 'null' && deviceid.length != 0) {
						document.getElementById(deviceid).selected = true;
					}
				}
			}
			
			$('.chosen-select').chosen({
				allow_single_deselect : true
			});
			//resize the chosen on window resize
			$(window).off('resize.chosen').on('resize.chosen', function() {
				$('.chosen-select').each(function() {
					var $this = $(this);
					$this.next().css({
						'width' : '800px'
					});
				})
			}).trigger('resize.chosen');
			
		});
	
	$(document).delegate(
			'.app_graph_link',
			'click',
			function(e) {
				var anchor = this, el = $(anchor), id = Number(el
						.attr('data-status')) || 0;

				if (e.ctrlKey || e.metaKey) {
					return true;
				} else {
					e.preventDefault();
				}

				var cell = document.getElementById(id);
				var text = el.html();
				anchor.href =  "?op=appLogGraph" + getQueryParams() + "&appLogQuery.category=" + encodeURIComponent(el.attr('msg')) ;
				
				if (text == '[:: show ::]') {
					anchor.innerHTML = '[:: hide ::]';

					if (cell.nodeName == 'IMG') { // <img src='...'/>
						cell.src = anchor.href;
					} else { // <div>...</div>
						$.ajax({
							type : "get",
							url : anchor.href,
							success : function(response, textStatus) {
								cell.style.display = 'block';
								cell.parentNode.style.display = 'table-cell';
								cell.innerHTML = response;
								
								var data = $('#appVersionsMeta', cell).text();
								graphPieChartWithName($('#appVersions', cell)[0], eval('(' + data + ')'),  'APP版本分布');
								
								data = $('#platformVersionsMeta', cell).text();
								graphPieChartWithName($('#platformVersions', cell)[0], eval('(' + data + ')'),  '平台版本分布');
								
								data = $('#devicesMeta', cell).text();
								graphPieChartWithName($('#devices', cell)[0], eval('(' + data + ')'),  '设备分布');
								
							}
						});
					}
				} else {
					anchor.innerHTML = '[:: show ::]';
					cell.style.display = 'none';
					cell.parentNode.style.display = 'none';
				}
			});
	
	function executeScript(html) {
	    var reg = /<script[^>]*>([^\x00]+)$/i;
	    var htmlBlock = html.split("<\/script>");
	    for (var i in htmlBlock) {
	        var blocks;
	        if (blocks = htmlBlock[i].match(reg)) 
	        {
	            var code = blocks[1].replace(/<!--/, '');
	            try {
	                eval(code) //执行脚本
	            } catch (e) {
	            }
	        }
	    }
	}
	
	function docReady(field, fields, prefix){
		var urls = [];
		
		if(typeof field == "undefined" || field.length == 0){
			document.getElementById(prefix + "All").checked = true;
			clickAll(fields, prefix);
		}else{
			urls = field.split(":");
			for(var i=0; i<urls.length; i++) {
				if(document.getElementById(prefix + "_" + urls[i]) != null) {
					document.getElementById(prefix + "_" + urls[i]).checked = true;
				}
			}
		}
	}
	
	function getDate() {
		var myDate = new Date();
		var myMonth = new Number(myDate.getMonth());
		var month = myMonth + 1;
		var day = myDate.getDate();
		
		if(month<10){
			month = '0' + month;
		}
		if(day<10){
			day = '0' + day;
		}
		
		var myHour = new Number(myDate.getHours());
		
		if(myHour < 10){
			myHour = '0' + myHour;
		}
		
		return myDate.getFullYear() + "-" + month + "-" + day + " " + myHour + ":00";
	}

	function getTime(){
		var myDate = new Date();
		var myHour = new Number(myDate.getHours());
		var myMinute = new Number(myDate.getMinutes());
		
		if(myHour < 10){
			myHour = '0' + myHour;
		}
		if(myMinute < 10){
			myMinute = '0' + myMinute;
		}
		return myHour + ":" + myMinute;
	}

	function converTimeFormat(time){
		var times = time.split(":");
		var hour = times[0];
		var minute = times[1];
		
		if(hour.length == 1){
			hour = "0" + hour;
		}
		if(minute.length == 1) {
			minute = "0" + minute;
		}
		return hour + ":" + minute;
	}
	
	function clickMe(fields, prefix) {
		var fs = [];
		if(fields != "[]") {
			fs = fields.replace(/[\[\]]/g,'').split(', ');
		}
		
		var num = 0;
		for( var i=0; i<fs.length; i++){
		 	var f = prefix + "_" + fs[i];
			if(document.getElementById(f).checked){
				num ++;
			}else{
				document.getElementById(prefix + "All").checked = false;
				break;
			} 
		}
		if(num > 0 && num == fs.length) {
			document.getElementById(prefix + "All").checked = true;
		}
	}
	
	function clickAll(fields, prefix) {
		var fs = [];
		if(fields.length > 0){
			fs = fields.replace(/[\[\]]/g,'').split(', ');
			for( var i=0; i<fs.length; i++){
			 	var f = prefix + "_" + fs[i];
			 	if(document.getElementById(f) != undefined) {
					document.getElementById(f).checked = document.getElementById(prefix + "All").checked;
			 	}
			}
		}
	}
	
	function queryField(fields, prefix){
		var fs = [];
		if(fields.length > 0) {
			fs = fields.replace(/[\[\]]/g,'').split(', ');
		}
		
		var url = '';
		var num = 0;
		if(document.getElementById(prefix + "All").checked == false && fs.length > 0) {
			for( var i=0; i<fs.length; i++){
			 	var f = prefix + "_" + fs[i];
				if(document.getElementById(f) != undefined 
						&& document.getElementById(f).checked){
					url += fs[i] + ":";
				} 
			}
			url = url.substring(0, url.length-1);
		}else{
			url = "";
		}
		return url;
	}
	
</script>

<style type="text/css">
	.row-fluid .span2 {
		width:10%;
	}
	.row-fluid .span10 {
		width:87%;
	}
	.report .btn-group {
		position: relative;
		display: inline-block;
		font-size: 0;
		white-space: normal;
		vertical-align: middle;
	}
	.chosen-container-multi .chosen-choices li.search-choice .search-choice-close {
		background:inherit;
	}
</style>