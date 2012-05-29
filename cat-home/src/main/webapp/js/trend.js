//(function basic_axis(container) {
//	var d1 = new Array;
//	var xlabel = data.xlabel;
//	var allValue = data.values;
//	var length = xlabel.length;
//	var graphNumber = allValue.length;
//	for(var i = 0;i<graphNumber;i++){
//		d1[i] = [];
//	}
//	
//	for ( var i = 0; i <= length; i += 1) {
//		for ( var j = 0; j < graphNumber; j++) {
//			d1[j].push([ xlabel[i], allValue[j][i] ]);
//		}
//	}
//
//	var flag = 1;
//	var dataContent = new Array;
//	for ( var j = 0; j < graphNumber; j++) {
//		var showData = {};
//		showData.data = d1[j];
//		showData.label= data.subTitles[j];
//		//showData.lines={show:true};
//		//showData.points ={show:true};
//		dataContent[j] = showData;
//	}
//
//	console.log(dataContent);
//	function ticksFn(n) {
//		return '' + n + '';
//	}
//
//	graph = Flotr.draw(container, dataContent, {
//		xaxis : {
//			noTicks : length, // Display 7 ticks.
//			tickFormatter : ticksFn, // Displays tick values between
//										// brackets.
//			// min : 1, // Part of the series is not displayed.
//			// max : 7.5 // Part of the series is not displayed.
//		},
//		yaxis : {
//			ticks : data.m_ylable, // Set Y-Axis ticks
//			// max : 40 // Maximum value along Y-Axis
//		},
//		grid : {
//			verticalLines : false,
//			backgroundColor : {
//				colors : [ [ 0, '#fff' ], [ 1, '#ccc' ] ],
//				start : 'top',
//				end : 'bottom'
//			}
//		},
//		legend : {
//			position : 'nw'
//		},
//		title : data.title,
//		subtitle : 'This is a subtitle'
//	});
//})(document.getElementById("testGraph"));

(function basic_time(container) {
	 var real = data.values[0];
	  var
	    d1    = [],
	    start = new Date("2012/05/14 00:00").getTime(),
	    options,
	    graph,
	    i, x, o;

	  for (i = 0; i < 192; i++) {
	    x = start+(i*1000*3600);
	    d1.push([x, real[i]]);
	  }
	        
	  options = {
	    xaxis : {
	      mode : 'time', 
	      labelsAngle : 15
	    },
	    selection : {
	      mode : 'x'
	    },
	    HtmlText : false,
	    title : 'Time'
	  };
	        
	  // Draw graph with default options, overwriting with passed options
	  function drawGraph (opts) {

	    // Clone the options, so the 'options' variable always keeps intact.
	    o = Flotr._.extend(Flotr._.clone(options), opts || {});

	    if(opts!=null&&opts.xaxis!=null){
	    console.log(opts.xaxis.min);
	    console.log(new Date(opts.xaxis.min));
	    o.title = "From"+new Date(opts.xaxis.min) +o.title;
	    }
	    // Return a new graph.
	    return Flotr.draw(
	      container,
	      [ d1 ],
	      o
	    );
	  }

	  graph = drawGraph();      
	        
	  Flotr.EventAdapter.observe(container, 'flotr:select', function(area){
	    // Draw selected area
	    graph = drawGraph({
	      xaxis : { min : area.x1, max : area.x2, mode : 'time', labelsAngle : 15 },
	      yaxis : { min : area.y1, max : area.y2 },
	      HtmlText : true
	    });
	  });
	        
	  // When graph is clicked, draw the graph with default area.
	  Flotr.EventAdapter.observe(container, 'flotr:click', function () { graph = drawGraph(); });
	})(document.getElementById("testGraph"));