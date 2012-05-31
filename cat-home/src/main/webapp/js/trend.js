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