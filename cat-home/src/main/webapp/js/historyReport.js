function onStartDateChange(){
	var start = $("#startDate").val();
	var type = $("#id_dateType").val();
	if(type=='day'){
		$( "#endDate" ).val("123");
	}else if(type=='week'){
		$( "#endDate" ).val("1234");
	}else if(type=='month'){
		$( "#endDate" ).val("12345");
	}
}

$( "#startDate" ).datepicker({ changeMonth: true,changeYear: true,dateFormat: "yy-mm-dd"});
$( "#endDate" ).datepicker({ changeMonth: true,changeYear: true,dateFormat: "yy-mm-dd"});

function showSummarizedReport(){

	var start = $("#startDate").val();
	var end = $("#endDate").val();
	var type = $("#id_dateType").val();
	if(start==null||start.length==0||end==null||end.length==0){
		alert('please select the start time');
	}
	var domain=$("#domain").val();
	var reportType=$("#reportType").val();
	var domain=$("#domain").val();
	
	window.location.href="?domain="+domain+"&startDate="+start+"&endDate="+end+"&op="+reportType;

}