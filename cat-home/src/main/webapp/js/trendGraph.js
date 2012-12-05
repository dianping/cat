function graphReal(container, data, step) {
	var real = data.values[0];
	var d1 = [], start = new Date(data.start).getTime(), options, graph, i, x, o;
	
	for (i = 0; i < data.size; i++) {
		x = start + (i * step);
		d1.push([ x, real[i] ]);
	}
	options = {
		xaxis : {
			mode : 'time',
			timeMode : 'local',
			labelsAngle : 15
		},
		yaxis : {
			min : 0
		},
		selection : {
			mode : 'x'
		},
		HtmlText : false,
		title : data.titles

	};
	// Draw graph with default options, overwriting with passed options
	function drawGraph(opts) {

		// Clone the options, so the 'options' variable always keeps intact.
		o = Flotr._.extend(Flotr._.clone(options), opts || {});

		if (opts != null && opts.xaxis != null) {
			o.title = " From " + formatDate(new Date(opts.xaxis.min)) + " To "
					+ formatDate(new Date(opts.xaxis.max));
		} else {
		}
		// Return a new graph.
		return Flotr.draw(container, [ d1 ], o);
	}

	graph = drawGraph();

	Flotr.EventAdapter.observe(container, 'flotr:select', function(area) {
		// Draw selected area
		graph = drawGraph({
			xaxis : {
				min : area.x1,
				max : area.x2,
				mode : 'time',
				timeMode : 'local',
				labelsAngle : 15
			},
			yaxis : {
				min : area.y1,
				max : area.y2
			},
			HtmlText : true
		});
	});
	// When graph is clicked, draw the graph with default area.
	Flotr.EventAdapter.observe(container, 'flotr:click', function() {
		graph = drawGraph();
	});
}

//default is five minutes
function graph(container, data) {
	graphReal(container, data, 5 * 60 * 1000);
}

function graphPieChart(container,data){
	 var showData=[];
	 
	 for(i = 0; i < data.items.length; i++){
		 var dataItem = [];
		 var graphItem ={};
		
		 dataItem.push([i+1,data.items[i].number]);
		 graphItem.data=dataItem;
		 graphItem.label=data.items[i].title;
		 showData.push(graphItem);
	 }

	 var graph = Flotr.draw(container,showData, {
	    HtmlText : true,
	    grid : {
	      verticalLines : false,
	      horizontalLines : false
	    },
	    xaxis : { showLabels : false },
	    yaxis : { showLabels : false },
	    pie : {
	      show : true, 
	      position : 'ne',
	      explode : 6
	    },
	    mouse : { track : true },
	    legend : {
	      position : 'ne',
	      backgroundColor : '#D2E8FF'
	    }
	  });
}