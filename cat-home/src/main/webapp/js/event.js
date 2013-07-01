function hourlyGraphLineChart(cell,data){
	cell.innerHTML = data;
}

function historyGraphLineChart(cell,response){
	cell.style.display = 'block';
	cell.parentNode.style.display = 'block';
	cell.innerHTML = response;

	var hitData = $('#hitTrendMeta',cell).text();
	graphLineChart($('#hitTrend',cell)[0],eval('('+hitData+')'));
	var failureData = $('#failureTrendMeta',cell).text();
	graphLineChart($('#failureTrend',cell)[0],eval('('+failureData+')'));
}