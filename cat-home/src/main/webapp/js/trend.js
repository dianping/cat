$(function() {
	$( "#datepicker" ).datepicker({ changeMonth: true,changeYear: true,dateFormat: "yy-mm-dd"});
});

function showSelfDefined(){
	var type=$("#id_selfDefined").val();
	if(type=='problem'){
		$('#status').html("status:");
	}else{
		$('#status').html("name:");
	}
}

function searchReport(domain,graphType){
	var dateType=$("#id_dateType").val();
	var queryDate=$("#datepicker").val();
	var selfQueryOption=$("#id_selfDefinedType").val();
	var queryIP=$("#ip").val(); 
	var queryType=$("#type").val(); 
	var queryName=$("#nameOrStatus").val(); 
	window.location.href="?domain="+domain+"&graphType="+graphType+"&dateType="+dateType+"&queryDate="+queryDate+"&selfQueryOption="
	+selfQueryOption+"&queryIP="+queryIP+"&queryType="+queryType+"&queryName="+queryName;
}

function formatDate(date) {   
    var myyear = date.getFullYear();     
    var mymonth = date.getMonth();     
    var myweekday = date.getDate();      
    var hour = date.getHours();
    var minute = date.getMinutes();
    var second=date.getSeconds();
    mymonth=mymonth<10?'0'+mymonth:mymonth;
    myweekday=myweekday<10?'0'+myweekday:myweekday;
    hour=hour<10?'0'+hour:hour;
    minute=minute<10?'0'+minute:minute;
    second=second<10?'0'+second:second;
    return (myyear+"-"+mymonth + "-" + myweekday+" "+hour+":"+minute+":"+second);      
}   

function maxDay(myDate)  
{   
    var ary = myDate.toArray();  
    var date1 = (new Date(ary[0],ary[1]+1,1));  
    var date2 = date1.dateAdd(1,'m',1);  
    var result = dateDiff(date1.Format('yyyy-MM-dd'),date2.Format('yyyy-MM-dd'));  
    return result;  
}  

function onStartDateChange(){
	var start = $("#datepicker").val();
	var type = $("#id_dateType").val();
	
	var startdate=new Date(start);
	var nowDayOfWeek = (startdate.getDay()-1+7)%7;             
	var nowDay = startdate.getDate();              
	var nowMonth = startdate.getMonth()+1;             
	var nowYear = startdate.getYear();             
	nowYear += (nowYear < 2000) ? 1900 : 0;  

	if(type=='day'){
		$( "#datepicker" ).val(formatDate(startdate));
		$( "#endDate" ).val(formatDate(new Date(startdate.getTime()+24*60*60*1000)));
	}else if(type=='week'){
		var startDateLong = startdate.getTime();
		var realStartTime = new Date( startdate.getTime() - nowDayOfWeek* 24*60*60*1000);
		var realEndTime = new Date(realStartTime.getTime() + 7* 24*60*60*1000);
		$( "#datepicker" ).val(formatDate(realStartTime));
		$( "#endDate" ).val(formatDate(realEndTime));
	}else if(type=='month'){
		var monthStartDate = new Date(nowYear, nowMonth,1);
		var monthEndDate = new Date(nowYear, nowMonth+1,1);
		$( "#datepicker" ).val(formatDate(monthStartDate));
		$( "#endDate" ).val(formatDate(monthEndDate));
	}
}

$("#id_selfDefinedType").val($("#hiddenSelfQueryOption").val()) ;
$("#id_dateType").val($("#hiddenDateType").val()) ;

//(function basic_time(container) {
//	 var real = data.values[0];
//	  var
//	    d1    = [],
//	    start = new Date("2012/05/14 00:00").getTime(),
//	    options,
//	    graph,
//	    i, x, o;
//
//	  for (i = 0; i < 192; i++) {
//	    x = start+(i*1000*3600);
//	    d1.push([x, real[i]]);
//	  }
//	        
//	  options = {
//	    xaxis : {
//	      mode : 'time', 
//	      labelsAngle : 15
//	    },
//	    selection : {
//	      mode : 'x'
//	    },
//	    HtmlText : false,
//	    title : 'Time'
//	  };
//	        
//	  // Draw graph with default options, overwriting with passed options
//	  function drawGraph (opts) {
//
//	    // Clone the options, so the 'options' variable always keeps intact.
//	    o = Flotr._.extend(Flotr._.clone(options), opts || {});
//
//	    if(opts!=null&&opts.xaxis!=null){
//	    console.log(opts.xaxis.min);
//	    console.log(new Date(opts.xaxis.min));
//	    o.title = "From"+new Date(opts.xaxis.min) +o.title;
//	    }
//	    // Return a new graph.
//	    return Flotr.draw(
//	      container,
//	      [ d1 ],
//	      o
//	    );
//	  }
//
//	  graph = drawGraph();      
//	        
//	  Flotr.EventAdapter.observe(container, 'flotr:select', function(area){
//	    // Draw selected area
//	    graph = drawGraph({
//	      xaxis : { min : area.x1, max : area.x2, mode : 'time', labelsAngle : 15 },
//	      yaxis : { min : area.y1, max : area.y2 },
//	      HtmlText : true
//	    });
//	  });
//	        
//	  // When graph is clicked, draw the graph with default area.
//	  Flotr.EventAdapter.observe(container, 'flotr:click', function () { graph = drawGraph(); });
//	})(document.getElementById("testGraph"));