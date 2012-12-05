function onStartDateChange(){
	var start = $("#startDate").val();
	var type = $("#id_dateType").val();
	
	var startdate=new Date(start);
	var nowDayOfWeek = startdate.getDay();             
	var nowDay = startdate.getDate();              
	var nowMonth = startdate.getMonth()+1;             
	var nowYear = startdate.getYear();             
	nowYear += (nowYear < 2000) ? 1900 : 0;  
	
	if(type=='day'){
		start=start.split("-");
		var dD = new Date(start[0],start[1],start[2]);   
		dD.setDate(dD.getDate()+1);
		var endString = dD.getFullYear() + "-" + dD.getMonth() + "-" + dD.getDate();
		$( "#endDate" ).val(formatDate(endString));
	}else if(type=='week'){
		if(nowDayOfWeek==0){
			var weekStartDate = new Date(nowYear, nowMonth,nowDay-6);
			var weekEndDate = new Date(nowYear, nowMonth, nowDay);
			var startString=weekStartDate.getFullYear() + "-" + weekStartDate.getMonth() + "-" + weekStartDate.getDate();
			var endString=weekEndDate.getFullYear() + "-" + weekEndDate.getMonth() + "-" + weekEndDate.getDate();
			$( "#startDate" ).val(formatDate(startString));
			$( "#endDate" ).val(formatDate(endString));
		}else{
			var weekStartDate = new Date(nowYear, nowMonth,nowDay - nowDayOfWeek+1);
			var weekEndDate = new Date(nowYear, nowMonth, nowDay + (7- nowDayOfWeek));
			var startString=weekStartDate.getFullYear() + "-" + weekStartDate.getMonth() + "-" + weekStartDate.getDate();
			var endString=weekEndDate.getFullYear() + "-" + weekEndDate.getMonth() + "-" + weekEndDate.getDate();
			$( "#startDate" ).val(formatDate(startString));
			$( "#endDate" ).val(formatDate(endString));
		}
	}else if(type=='month'){
		var monthStartDate = new Date(nowYear, nowMonth,1);
		var monthEndDate = new Date(nowYear, nowMonth+1,1);
		var startString=monthStartDate.getFullYear() + "-" + monthStartDate.getMonth() + "-" + monthStartDate.getDate();
		var endString=monthEndDate.getFullYear() + "-" + monthEndDate.getMonth() + "-" + monthEndDate.getDate();
		$( "#startDate" ).val(formatDate(startString));
		$( "#endDate" ).val(formatDate(endString));
	}
}


function formatDate(date) {   
	date=date.split("-");
	var date = new Date(date[0],date[1],date[2]);
    var myyear = date.getFullYear();     
    var mymonth = date.getMonth();     
    var myweekday = date.getDate();      
         
    if(mymonth < 10){     
        mymonth = "0" + mymonth;     
    }      
    if(myweekday < 10){     
        myweekday = "0" + myweekday;     
    }     
    return (myyear+"-"+mymonth + "-" + myweekday);      
}   
    

function onStartDateChange(){
	var start = $("#datepicker").val();
	var type = $("#id_dateType").val();
	
	var startdate=new Date(start);
	var nowDayOfWeek = startdate.getDay();             
	var nowDay = startdate.getDate();              
	var nowMonth = startdate.getMonth()+1;             
	var nowYear = startdate.getYear();             
	nowYear += (nowYear < 2000) ? 1900 : 0;  

	if(type=='day'){
		start=start.split("-");
		var dD = new Date(start[0],start[1],start[2]);   
		dD.setDate(dD.getDate()+1);
		var endString = dD.getFullYear() + "-" + dD.getMonth() + "-" + dD.getDate();
		$( "#endDate" ).val(formatDate(endString));
	}else if(type=='week'){
		if(nowDayOfWeek==0){
			var weekStartDate = new Date(nowYear, nowMonth,nowDay-6);
			var weekEndDate = new Date(nowYear, nowMonth, nowDay);
			var startString=weekStartDate.getFullYear() + "-" + weekStartDate.getMonth() + "-" + weekStartDate.getDate();
			var endString=weekEndDate.getFullYear() + "-" + weekEndDate.getMonth() + "-" + weekEndDate.getDate();
			$( "#datepicker" ).val(formatDate(startString));
			$( "#endDate" ).val(formatDate(endString));
		}else{
			var weekStartDate = new Date(nowYear, nowMonth,nowDay - nowDayOfWeek+1);
			var weekEndDate = new Date(nowYear, nowMonth, nowDay + (7- nowDayOfWeek));
			var startString=weekStartDate.getFullYear() + "-" + weekStartDate.getMonth() + "-" + weekStartDate.getDate();
			var endString=weekEndDate.getFullYear() + "-" + weekEndDate.getMonth() + "-" + weekEndDate.getDate();
			$( "#datepicker" ).val(formatDate(startString));
			$( "#endDate" ).val(formatDate(endString));
		}
	}else if(type=='month'){
		var monthStartDate = new Date(nowYear, nowMonth,1);
		var monthEndDate = new Date(nowYear, nowMonth+1,1);
		var startString=monthStartDate.getFullYear() + "-" + monthStartDate.getMonth() + "-" + monthStartDate.getDate();
		var endString=monthEndDate.getFullYear() + "-" + monthEndDate.getMonth() + "-" + monthEndDate.getDate();
		$( "#datepicker" ).val(formatDate(startString));
		$( "#endDate" ).val(formatDate(endString));
	}
}



$( "#startDate" ).datepicker({ changeMonth: true,changeYear: true,dateFormat: "yy-mm-dd"});

$("#domain").val(domain) ;
$("#reportType").val(type) ;


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


function formatDate(date) {   
	date=date.split("-");
	var date = new Date(date[0],date[1],date[2]);
    var myyear = date.getFullYear();     
    var mymonth = date.getMonth();     
    var myweekday = date.getDate();      
         
    if(mymonth < 10){     
        mymonth = "0" + mymonth;     
    }      
    if(myweekday < 10){     
        myweekday = "0" + myweekday;     
    }     
    return (myyear+"-"+mymonth + "-" + myweekday);      
} 
