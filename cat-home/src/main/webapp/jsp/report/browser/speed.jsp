<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix='fmt' uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.browser.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.browser.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.browser.Model" scope="request" />

<a:web_body>

<script type="text/javascript">
function check() {
	var value = document.getElementById("checkbox").checked;

	if (value == true) {
		$('#history').slideDown();
		$("#page2").val($("#page").val());
		onPageChange2();
		$("#step2").val($("#step").val());
		$("#network2").val($("#network").val());
		$("#platform2").val($("#platform").val());
		$("#city2").val($("#city").val());
		$("#operator2").val($("#operator").val());
		$("#source2").val($("#source").val());
		$("#time2").val($("#time").val());
	} else {
		$('#history').slideUp();
	}
}

function getDate() {
	var myDate = new Date();
	var myMonth = new Number(myDate.getMonth());
	var month = myMonth + 1;
	var day = myDate.getDate();

	if (month < 10) {
		month = '0' + month;
	}
	if (day < 10) {
		day = '0' + day;
	}

	return myDate.getFullYear() + "-" + month + "-" + day;
}

function query() {
	var time = $("#time").val();
	var page = $("#page").val();
	var step = $("#step").val();
	var network = $("#network").val();
	var platform = $("#platform").val();
	var city = $("#city").val();
	var operator = $("#operator").val();
	var source = $("#source").val();
	var split = ";";
	var query1 = time + split + page + split + step + split + network
			 + split + platform + split + city + split
			+ operator + split + source;
	var query2 = "";
	var value = document.getElementById("checkbox").checked;

	if (value == true) {
		var time2 = $("#time2").val();
		var page2 = $("#page2").val();
		var step2 = $("#step2").val();
		var network2 = $("#network2").val();
		var platform2 = $("#platform2").val();
		var city2 = $("#city2").val();
		var operator2 = $("#operator2").val();
		var source2 = $("#source2").val();
		
		query2 = time2 + split + page2 + split + step2 + split
				+ network2 + split + platform2
				+ split + city2 + split + operator2 + split + source2;
	}

	window.location.href = "?op=speed&query1=" + query1 + "&query2=" + query2;
}
		
$(document).ready(
	function() {
		$('#Browser').addClass('active open');
		$('#web_speed').addClass('active');
		$('#time').datetimepicker({
			format:'Y-m-d',
			timepicker:false,
			maxDate:0
		});
		$('#time2').datetimepicker({
			format:'Y-m-d',
			timepicker:false,
			maxDate:0
		});

		buildPage();
		buildPage2();
		var query1 = '${payload.query1}';
		var query2 = '${payload.query2}';
		var words = query1.split(";");

		if (typeof (words[0]) != "undefined" && words[0].length == 0) {
			$("#time").val(getDate());
		} else {
			$("#time").val(words[0]);
		}
		if(typeof words[1] != "undefined"  && words[1].length > 0) {
			
			$("#page").val(words[1]);
		}
		onPageChange();
		if(typeof words[2] != "undefined"  && words[2].length > 0) {
			$("#step").val(words[2]);
		}
		$("#network").val(words[3]);
		$("#platform").val(words[4]);
		$("#city").val(words[5]);
		$("#operator").val(words[6]);
		$("#source").val(words[7]);
		
		var datePair = {};
		datePair["当前值"]=$("#time").val();

		if (query2 != null && query2 != '') {
			$('#history').slideDown();
			document.getElementById("checkbox").checked = true;
			var words = query2.split(";");

			if (typeof (words[0]) != "undefined"
				&& words[0].length == 0) {
				$("#time2").val(getDate());
			} else {
				$("#time2").val(words[0]);
			}
			
			datePair["对比值"]=$("#time2").val();
			if(typeof words[1] != "undefined"  && words[1].length > 0) {
				$("#page2").val(words[1]);
			}
			onPageChange2();
			if(typeof words[2] != "undefined" && words[2].length > 0) {
				$("#step2").val(words[2]);
			}
			$("#network2").val(words[3]);
			$("#platform2").val(words[4]);
			$("#city2").val(words[5]);
			$("#operator2").val(words[6]);
			$("#source2").val(words[7]);
		} else {
			$("#time2").val(getDate());
		}
		var data = ${model.webSpeedDisplayInfo.lineChart.jsonString};
		
		graphMetricChartForDay(document.getElementById('${model.webSpeedDisplayInfo.lineChart.id}'),
				data, datePair); 
});
		
function buildPage() {
	//custom autocomplete (category selection)
	$.widget( "custom.catcomplete", $.ui.autocomplete, {
		_renderMenu: function( ul, items ) {
			var that = this,
			currentCategory = "";
			$.each( items, function( index, item ) {
				that._renderItemData( ul, item );
			});
			ul.bind("click",function(){
				onPageChange();
			});
		}
	});
		
 	var data = [];
	<c:forEach var="speed" items="${model.speeds}">
		var item = {};
		item['label'] = '${speed.value.id}|${speed.key}';
		data.push(item);
	</c:forEach>
		
	$("#page").catcomplete({
		delay: 0,
		source: data
	}); 
}

function buildPage2() {
	//custom autocomplete (category selection)
	$.widget( "custom.catcomplete", $.ui.autocomplete, {
		_renderMenu: function( ul, items ) {
			var that = this,
			currentCategory = "";
			$.each( items, function( index, item ) {
				that._renderItemData( ul, item );
			});
			ul.bind("click",function(){
				onPageChange2();
			});
		}
	});
		
 	var data = [];
	<c:forEach var="speed" items="${model.speeds}">
		var item = {};
		item['label'] = '${speed.value.id}|${speed.key}';
		data.push(item);
	</c:forEach>
		
	$("#page2").catcomplete({
		delay: 0,
		source: data
	}); 
}

var page2Steps = ${model.page2StepsJson};

function onPageChange() {
    var	page = $("#page").val();
	var stepSelect = $("#step");
	var steps = page2Steps[page.split("|")[1]]['steps'];
	stepSelect.empty();
	
	for(var s in steps){
		var step = steps[s];
		if(step['title'] != undefined && step['title'].length > 0){
			stepSelect.append($("<option value='"+step['id']+"'>"+step['title']+"</option>"));
		}else{
			stepSelect.append($("<option value='"+step['id']+"'>"+step['step']+"</option>"));
		}
	} 
}

function onPageChange2() {
	var	page2 = $("#page2").val();
	var	stepSelect2 = $("#step2");
	
	if(page2 != "" && page2 != 'undefined') {
		var steps2 = page2Steps[page2.split("|")[1]]['steps'];
		stepSelect2.empty();
	
		for(var s in steps2){
			var step = steps2[s];
			
			if(step['title'] != undefined && step['title'].length > 0){
				stepSelect2.append($("<option value='"+step['id']+"'>"+step['title']+"</option>"));
			}else{
				stepSelect2.append($("<option value='"+step['id']+"'>"+step['step']+"</option>"));
			}
		}
	}
}
</script>

			<%@include file="speedDetail.jsp"%>
</a:web_body>

<style type="text/css">
.row-fluid .span2 {
	width: 10%;
}

.row-fluid .span10 {
	width: 87%;
}
</style>