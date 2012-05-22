(function basic_axis(container) {
	var d1 = new Array;
	var xlabel = data.xlabel;
	var allValue = data.values;
	var length = xlabel.length;
	var graphNumber = allValue.length;
	for(var i = 0;i<graphNumber;i++){
		d1[i] = [];
	}
	
	for ( var i = 0; i <= length; i += 1) {
		for ( var j = 0; j < graphNumber; j++) {
			d1[j].push([ xlabel[i], allValue[j][i] ]);
		}
	}

	var flag = 1;
	var dataContent = new Array;
	for ( var j = 0; j < graphNumber; j++) {
		var showData = {};
		showData.data = d1[j];
		showData.label= data.subTitles[j];
		//showData.lines={show:true};
		//showData.points ={show:true};
		dataContent[j] = showData;
	}

	console.log(dataContent);
	function ticksFn(n) {
		return '' + n + '';
	}

	graph = Flotr.draw(container, dataContent, {
		xaxis : {
			noTicks : length, // Display 7 ticks.
			tickFormatter : ticksFn, // Displays tick values between
										// brackets.
			// min : 1, // Part of the series is not displayed.
			// max : 7.5 // Part of the series is not displayed.
		},
		yaxis : {
			ticks : data.m_ylable, // Set Y-Axis ticks
			// max : 40 // Maximum value along Y-Axis
		},
		grid : {
			verticalLines : false,
			backgroundColor : {
				colors : [ [ 0, '#fff' ], [ 1, '#ccc' ] ],
				start : 'top',
				end : 'bottom'
			}
		},
		legend : {
			position : 'nw'
		},
		title : data.title,
		subtitle : 'This is a subtitle'
	});
})(document.getElementById("testGraph"));