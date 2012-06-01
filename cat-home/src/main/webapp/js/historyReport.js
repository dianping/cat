function onStartDateChange(){
	var start = $("#startDate").val();
	var type = $("#id_dateType").val();
	if(type=='day'){
		$( "#endDate" ).val(start);
	}else if(type=='week'){
		var day = new Date(Date.parse(start.replace(/-/g, '/'))); //将日期值格式化
		$( "#endDate" ).val(day.getDay());
	}else if(type=='month'){
		var end="";
		$( "#endDate" ).val("12345");
	}
}

$( "#startDate" ).datepicker({ changeMonth: true,changeYear: true,dateFormat: "yy-mm-dd"});
$( "#endDate" ).datepicker({ changeMonth: true,changeYear: true,dateFormat: "yy-mm-dd"});

console.log(domain);
console.log(type);
$("#domain").val(domain) ;
$("#reportType").val(type) ;

function longTimeChange(domain,ip){
	var longtime=$("#p_longUrl").val();
	var start = $("#startDate").val();
	var end = $("#endDate").val();
	window.location.href="?op=problem&domain="+domain+"&ip="+ip+"&startDate="+start+"&endDate="+end+"&threshold="+longtime;
}

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