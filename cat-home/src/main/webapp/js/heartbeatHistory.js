function formatDate(date) {
	var myyear = date.getFullYear();
	var mymonth = date.getMonth();
	var myweekday = date.getDate();
	var hour = date.getHours();
	var minute = date.getMinutes();
	var second = date.getSeconds();

	if (mymonth < 10) {
		mymonth = "0" + mymonth;
	}
	if (myweekday < 10) {
		myweekday = "0" + myweekday;
	}
	hour = hour < 10 ? '0' + hour : hour;
	minute = minute < 10 ? '0' + minute : minute;
	second = second < 10 ? '0' + second : second;
	return (myyear + "-" + mymonth + "-" + myweekday + " " + hour + ":"
			+ minute + ":" + second);
}

function graph(container, data) {
	var hour = 1000 * 3600;
	var minute = 1000*60;
	var title=data.titles;
	var real = data.values[0];
	var d1 = [], start = new Date(data.start).getTime(), options, graph, i, x, o;

	for (i = 0; i < data.size; i++) {
		x = start + (i * minute);
		d1.push([ x, real[i] ]);
	}

	options = {
		xaxis : {
			mode : 'time',
			timeMode:'local',  
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
			console.log(opts.xaxis.min);
			console.log(new Date(opts.xaxis.min));
			console.log(formatDate(new Date(opts.xaxis.min)))
			console.log(opts.xaxis.max);
			console.log(new Date(opts.xaxis.max));
			console.log(formatDate(new Date(opts.xaxis.max)))
			o.title = data.titles + " From "
					+ formatDate(new Date(opts.xaxis.min)) + " To"
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
				timeMode:'local',  
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

function disksGraph(size,diskHistoryGraph){
	if(size>0){
		//this.parentNode.parentNode.rowIndex
		var graphID=document.getElementById('graph');
		var memoryGraph=document.getElementById('memoryGraph');
		var memroyIndex=memoryGraph.rowIndex;
		var diskInfoHead = graphID.insertRow(memroyIndex+1);
		diskInfoHead.innerHTML = '<th colspan="3">Disk Info</th>'; 
		for(len=0;len<size/3;len++){
			var id="diskGraph"+len;
			var graphRow=graphID.insertRow(memroyIndex+2+len);
			graphRow.setAttribute("id",id);
		}
		for(i=0;i<size;i++){
			var index=Math.floor(i/3);
			var id="diskGraph"+index;
			var graphData=diskHistoryGraph[i];
			var graphCell=document.getElementById(id).insertCell();
			var div = document.createElement("div");
			graphCell.appendChild(div);
			div.setAttribute("class","graph");
			graph(div,graphData);
		}
	}
}


