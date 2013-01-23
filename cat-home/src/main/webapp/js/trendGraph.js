function graphReal(container, data, step) {
	var dataLength = data.values.length;
	var real = data.values[0], size = data.size;
	var start = new Date(data.start).getTime(), options, graph, i, x, o;

	// [ d1, d2, d3 ]
	var allDataArray = [];

	function _rebuild_xy(line_data) {
		var _arr = [];
		for (i = 0; i < size; i++) {
			x = start + (i * step);
			_arr.push([ x, line_data[i] ]);
		}
		;
		return _arr;
	}
	;

	for (j = 0; j < dataLength; j++) {
		allDataArray.push(_rebuild_xy(data.values[j]));
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

		// if (opts != null && opts.xaxis != null) {
		// o.title = " From " + formatDate(new Date(opts.xaxis.min)) + " To "
		// + formatDate(new Date(opts.xaxis.max));
		// } else {
		// }
		// Return a new graph.
		return Flotr.draw(container, allDataArray, o);
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

// default is five minutes
function graph(container, data) {
	var step = data.step;
	if (step == 0) {
		//deafult is five minute
		graphReal(container, data, 5 * 60 * 1000);
	} else {
		graphReal(container, data, step);
	}
}

function graphPieChart(container, data) {
	var showData = [];

	for (i = 0; i < data.items.length; i++) {
		var dataItem = [];
		var graphItem = {};

		dataItem.push([ i + 1, data.items[i].number ]);
		graphItem.data = dataItem;
		graphItem.label = data.items[i].title;
		showData.push(graphItem);
	}

	var graph = Flotr.draw(container, showData, {
		HtmlText : true,
		grid : {
			verticalLines : false,
			horizontalLines : false
		},
		xaxis : {
			showLabels : false
		},
		yaxis : {
			showLabels : false
		},
		pie : {
			show : true,
			position : 'ne',
			explode : 6
		},
		mouse : {
			track : true
		},
		legend : {
			position : 'ne',
			backgroundColor : '#D2E8FF'
		}
	});
}