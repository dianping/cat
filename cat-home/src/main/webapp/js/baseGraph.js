var pieChartParse = function(data) {
	var res = [];
	var index = 0;

	data.items.forEach(function(item) {
		var temp = [];
		temp[0] = item.title;
		temp[1] = item.number;
		res[index] = temp;
		index++;
	})
	return res;
}

function graphPieChart(container, data) {
	$(container).highcharts({
		chart : {
			plotBackgroundColor : null,
			plotBorderWidth : null,
			plotShadow : false
		},
		title : {
			text : ''
		},
		credits : {
			enabled : false
		},
		tooltip : {
			pointFormat : '{series.name}: <b>{point.percentage:.1f}%</b>'
		},
		plotOptions : {
			pie : {
				allowPointSelect : true,
				cursor : 'pointer',
				dataLabels : {
					enabled : true,
					color : '#000000',
					connectorColor : '#000000',
					format : '<b>{point.name}</b>: {point.percentage:.1f} %'
				},
				showInLegend : true
			}
		},
		series : [ {
			type : 'pie',
			name : 'share',
			data : pieChartParse(data)
		} ]
	});
}

function parseLineData(data) {
	var res = [];
	data.subTitles.forEach(function(title, i) {
		var series = {}
		series.name = title;
		series.data = [];
		var start = new Date(Date.parse(data.start));
		var startLong = start.getTime();
		data.values[i].forEach(function(value, j) {
			var time = start.getTime() + j * data.step;
			var item = [];
			item[0] = time;
			item[1] = value;
			series.data[j] = item;
		});
		res.push(series);
	});
	return res;
}

function parseMetricLineData(data) {
	var res = [];
	data.subTitles.forEach(function(title, i) {
		var series = {}
		series.name = title;
		series.data = [];
		var map = data.datas[i];
		var j = 0;

		for ( var key in map) {
			var item = [];
			item[0] = Number(key);
			item[1] = map[key];
			series.data[j] = item;
			j++;
		}
		res.push(series);
	});
	return res;
}

function graphMetricChart(container, data) {
	Highcharts.setOptions({
		global : {
			useUTC : false
		}
	});
	var ylabelMin = data.minYlable;
	var _data = parseMetricLineData(data);
	$(container).highcharts(
			{
				chart : {
					type : 'spline'
				},
				title : {
					text : data.htmlTitle,
					useHTML: true
				},
				xAxis : {
					type : 'datetime',
					dateTimeLabelFormats : {
						second : '%H:%M:%S',
						minute : '%H:%M',
						hour : '%H:%M',
						day : '%m-%d',
						week : '%Y-%m-%d',
						month : '%m-%d',
						year : '%Y-%m'
					}
				},
				yAxis : {
					min : ylabelMin
				},
				credits : {
					enabled : false
				},
				plotOptions : {
					spline : {
						lineWidth : 2,
						states : {
							hover : {
								lineWidth : 2
							}
						},
						marker : {
							enabled : false
						}
					}
				},
				legend : {
					maxHeight : 82
				},
				tooltip : {
					allowPointSelect : false,
					formatter : function() {
						return '<b>'
								+ this.series.name
								+ '</b><br/>'
								+ Highcharts.dateFormat('%Y-%m-%d %H:%M',
										this.x) + ' ' + Number(this.y).toFixed(1);
					}
				},
				series : _data
			});
}
function graphLineChart(container, data) {
	Highcharts.setOptions({
		global : {
			useUTC : false
		}
	});
	var _data = parseLineData(data);
	$(container).highcharts(
			{
				chart : {
					type : 'spline'
				},
				title : {
					text : data.title,
					useHTML: true
				},
				xAxis : {
					type : 'datetime',
					dateTimeLabelFormats : {
						second : '%H:%M:%S',
						minute : '%H:%M',
						hour : '%H:%M',
						day : '%m-%d',
						week : '%Y-%m-%d',
						month : '%m-%d',
						year : '%Y-%m'
					}
				},
				yAxis : {
					min : 0.0
				},
				credits : {
					enabled : false
				},
				plotOptions : {
					spline : {
						lineWidth : 2,
						states : {
							hover : {
								lineWidth : 2
							}
						},
						marker : {
							enabled : false
						}
					}
				},
				legend : {
					maxHeight : 82
				},
				tooltip : {
					allowPointSelect : false,
					formatter : function() {
						return '<b>'
								+ this.series.name
								+ '</b><br/>'
								+ Highcharts.dateFormat('%Y-%m-%d %H:%M',
										this.x) + ': ' + this.y;
					}
				},
				series : _data
			});
}

$(document).delegate(
		'.graph_link',
		'click',
		function(e) {
			var anchor = this, el = $(anchor), id = Number(el
					.attr('data-status')) || 0;

			if (e.ctrlKey || e.metaKey) {
				return true;
			} else {
				e.preventDefault();
			}

			var cell = document.getElementById(id);
			var text = el.html();

			if (text == '[:: show ::]') {
				anchor.innerHTML = '[:: hide ::]';

				if (cell.nodeName == 'IMG') { // <img src='...'/>
					cell.src = anchor.href;
				} else { // <div>...</div>
					$.ajax({
						type : "get",
						url : anchor.href,
						success : function(data, textStatus) {
							hourlyGraphLineChart(cell, data);
						}
					});
				}
				cell.style.display = 'block';
				cell.parentNode.style.display = 'block';
			} else {
				anchor.innerHTML = '[:: show ::]';
				cell.style.display = 'none';
				cell.parentNode.style.display = 'none';
			}
		});

$(document).delegate(
		'.history_graph_link',
		'click',
		function(e) {
			var anchor = this, el = $(anchor), id = Number(el
					.attr('data-status')) || 0;

			if (e.ctrlKey || e.metaKey) {
				return true;
			} else {
				e.preventDefault();
			}

			var cell = document.getElementById(id);
			var text = el.html();

			if (text == '[:: show ::]') {
				anchor.innerHTML = '[:: hide ::]';

				if (cell.nodeName == 'IMG') { // <img src='...'/>
					cell.src = anchor.href;
				} else { // <div>...</div>
					$.ajax({
						type : "get",
						url : anchor.href,
						success : function(response, textStatus) {
							historyGraphLineChart(cell, response);
						}
					});
				}
			} else {
				anchor.innerHTML = '[:: show ::]';
				cell.style.display = 'none';
				cell.parentNode.style.display = 'none';
			}
		});
$(document).delegate('.problem_status_graph_link', 'click', function(e) {
	var anchor = this, el = $(anchor), id = el.attr('data-status');

	console.log("id: " + id)
	if (e.ctrlKey || e.metaKey) {
		return true;
	} else {
		e.preventDefault();
	}

	var cell = document.getElementById(id);
	var text = el.html();

	if (text == '[:: show ::]') {
		anchor.innerHTML = '[:: hide ::]';

		if (cell.nodeName == 'IMG') { // <img src='...'/>
			cell.src = anchor.href;
		} else { // <div>...</div>
			$.ajax({
				type : "get",
				url : anchor.href,
				success : function(response, textStatus) {
					historyGraphLineChart(cell, response);
				}
			});
		}
	} else {
		anchor.innerHTML = '[:: show ::]';
		cell.style.display = 'none';
		cell.parentNode.style.display = 'none';
	}
});
