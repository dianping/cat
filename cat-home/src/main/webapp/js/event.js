function hourlyGraphLineChart(cell,data){
	cell.style.display = 'block';
	cell.parentNode.style.display = 'table-cell';
	cell.innerHTML = data;
	
	data = $('#distributionChartMeta', cell).text();
	
	if (data != null && data.length > 0) {
		graphPieChart($('#distributionChart', cell)[0], eval('(' + data + ')'));
	}
}

function historyGraphLineChart(cell,response){
	cell.style.display = 'block';
	cell.parentNode.style.display = 'table-cell';
	cell.innerHTML = response;

	var hitData = $('#hitTrendMeta',cell).text();
	graphLineChart($('#hitTrend',cell)[0],eval('('+hitData+')'));
	var failureData = $('#failureTrendMeta',cell).text();
	graphLineChart($('#failureTrend',cell)[0],eval('('+failureData+')'));
	
	data = $('#distributionChartMeta', cell).text();
	
	if (data != null && data.length > 0) {
		graphPieChart($('#distributionChart', cell)[0], eval('(' + data + ')'));
	}
}