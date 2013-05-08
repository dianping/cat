$(document).ready(function() {
	$('#ckall').click(function() {
		if ($('#ckall').is(':checked')) {
			$(".table input[type='checkbox']").prop('checked', true);
		} else {
			$(".table input[type='checkbox']").prop('checked', false);
		}
	});
	
	$("#btnSuspend").click(function(){
		var checkbox = $(".table input[type='checkbox']:checked");
		var id = "";
		for(var i = 0 ; i < checkbox.length ; i++){
			id = id + $(":nth-child(2)",$(checkbox[i]).closest('tr')).html() + "-";
		}
		url = window.location.href;
		index = url.indexOf("&suspend");
		if(index != -1){
			window.location.href = url.substring(0,index) + "&suspend=-1&ids=" + id;
		}else{
			window.location.href = url + "&suspend=-1&ids=" + id;
		}
		loaction.reload();
	});
	
	$("#btnResume").click(function(){
		var checkbox = $(".table input[type='checkbox']:checked");
		var id = "";
		for(var i = 0 ; i < checkbox.length ; i++){
			id = id + $(":nth-child(2)",$(checkbox[i]).closest('tr')).html() + "-";
		}
		url = window.location.href;
		index = url.indexOf("&suspend");
		if(index != -1){
			window.location.href = url.substring(0,index) + "&suspend=1&ids=" + id;
		}else{
			window.location.href = url + "&suspend=1&ids=" + id;
		}
		loaction.reload();
	});
	
});