function insertTr(){
	var innerHTML = "<tr>";
	innerHTML += '<td><select><option value="url">Current URL</option><option value="city">City</option>'
			+ '<option value="platform">Platform</option><option value="vistorType">VisitorType</option>'
			+ '</select></td>';
			
	innerHTML += '<td><select><option value="1">is equal to (case insens.)</option>'
		+ '<option value="2">is not equal to (case insens.)</option><option value="3">is equal to (case sens.)</option>'
		+ '<option value="4">is not equal to (case sens.)</option><option value="5">marches Regex (case insens.)</option>'
		+ '<option value="6">marches Regex (case sens.)</option><option value="7">contains</option>'
		+ '<option value="8">does not contains</option></select></td>';
	
	innerHTML += '<td><input class="input-large"></td>';
	innerHTML += '<td><a class="btn btn-link"><i class="icon-remove"></i></a></td>';
	innerHTML += '</tr>';
	
	return innerHTML;
}

function insertOperator(){
	var innerHTML = "<tr>";
	innerHTML += '<td colspan="1"></td>';
	innerHTML += '<td colspan="1"><a class="btn btn-primary">AND</a><a class="btn pull-right">OR</a></td>';
	innerHTML += '<td colspan="1"></td>';
	innerHTML += '<td colspan="1"></td>';
	innerHTML += '</tr>';
	
	return innerHTML;
}

function getConditionsHTML(){
	var conditions = "";
	
	var trs = $('#div2 table tr');
	var size = trs.size();
	
	for(var index = 0 ; index < size ; index++ ){
		var tr = trs.get(index);
		var selects = $('select',tr);
		var input = $('input',tr);
		if(index != (size - 1)){
			var secondTr = trs.get(++index);
			var op = $('.btn-primary',secondTr).text();
			conditions += "<p><strong>" + $(selects[0]).val() + " " + $('option:selected',selects[1]).text() + " " + $(input).val() + " <span class='label label-info'>" + op + "</span><strong></p>";
		}else{
			conditions += "<p><strong>" + $(selects[0]).val() + " " + $('option:selected',selects[1]).text() + " " + $(input).val() + "</strong></p> ";
		}
	}
	
	return conditions;
}

function getConditions(){
	var conditions = [];
	
	$('input[type="url"]').each(function(i){
		var name = $(this).attr("name");
		
		if(name && $(this).val()){
			var condition = getCondition("url","and",name,$(this).val(),i);
			conditions.push(condition);
		}
		
	});
	
	var trs = $('#div2 table tr');
	var size = trs.size();
	
	for(var index = 0 ; index < size ; index++ ){
		var tr = trs.get(index);
		var selects = $('select',tr);
		var input = $('input',tr);
		if(index != (size - 1)){
			var secondTr = trs.get(++index);
			var tmpOp = $('.btn-primary',secondTr).text();
			var op = "";
			if(tmpOp == "AND"){
				op = "and";
			}else if(tmpOp == "OR"){
				op = "or";
			}
			var condition = getCondition($(selects[0]).val(),op,$(selects[1]).val(),$(input).val(),3);
			conditions.push(condition);
		}else{
			var condition = getCondition($(selects[0]).val(),"and",$(selects[1]).val(),$(input).val(),3);
			conditions.push(condition);
		}
	}
	
	var condition = getCondition("percent","and",1,$(".add-on:eq(3)").text(),4);
	conditions.push(condition);
	
	return conditions;
}

function getCondition(name,operator,comparator,text,seq){
	var condition = {
		"name":name,
		"operator":operator,
		"comparator":comparator,
		"text":text,
		"seq":seq
	}
	
	return condition;
}
$(document).ready(function() {
	$('#ckall').click(function() {
		if ($('#ckall').is(':checked')) {
			$(".table input[type='checkbox']").prop(
					'checked', true);
		} else {
			$(".table input[type='checkbox']").prop(
					'checked', false);
		}
	});
	
	$("#btnSuspend").click(function() {
		var checkbox = $(".table input[type='checkbox']:checked");
		var id = "";
		for ( var i = 0; i < checkbox.length; i++) {
			id = id + $(":nth-child(2)",$(checkbox[i]).closest('tr')).html() + "-";
		}
		url = window.location.href;
		index = url.indexOf("&suspend");
		if (index != -1) {
			window.location.href = url
					.substring(0, index)
					+ "&suspend=-1&ids=" + id;
		} else {
			if (url.indexOf("?") == -1) {
				url = url + '?';
			}
			window.location.href = url
					+ "&suspend=-1&ids=" + id;
		}
		loaction.reload();
	});

	$("#btnResume").click(function() {
		var checkbox = $(".table input[type='checkbox']:checked");
		var id = "";
		for ( var i = 0; i < checkbox.length; i++) {
			id = id + $(":nth-child(2)",$(checkbox[i]).closest('tr')).html() + "-";
		}
		url = window.location.href;
		index = url.indexOf("&suspend");
		if (index != -1) {
			window.location.href = url
					.substring(0, index)
					+ "&suspend=1&ids=" + id;
		} else {
			if (url.indexOf("?") == -1) {
				url = url + '?';
			}
			window.location.href = url
					+ "&suspend=1&ids=" + id;
		}
		loaction.reload();
	});
	
	$('#addVistorCondition').click(function(){
		var tr = $(insertTr());
		$('a',tr).click(function(){
			var index = $(this).closest("tr").index();
			if(index > 0){
				$(this).closest("tr").prev().remove();
				$(this).closest("tr").remove();
			}else if(index == 0){
				$(this).closest("tr").next().remove();
				$(this).closest("tr").remove();
			}
		});
		
		if($('#div2 tbody tr:last').size() == 0){
			$('#div2 tbody').html(tr);
		}else{
			var innerHTML = $(insertOperator());
			$('a',innerHTML).click(function(){
				$('a',innerHTML).toggleClass("btn-primary");
			});
			$('#div2 tbody tr:last').after(innerHTML);
			$('#div2 tbody tr:last').after(tr);
		}
	});

});

(function(w) {
	var abtest = {
		"httpError" : function(xhr, textStatus, errorThrown) {
			// 去掉按钮disable
			abtest.alertError('抱歉啦', '抱歉，网络发生错误了，请刷新页面试试...');
		},
		"alertError" : function(title, errorMsg) {
			// 显示错误消息
			$('#errorMsgModal > div[class="modal-header"] > h3').text(title);
			$('#errorMsgModal > div[class="modal-body"] > p').text(errorMsg);
			$('#errorMsgModal').modal('show');
		}
	};
	w.abtest = abtest;
}(window || this));
