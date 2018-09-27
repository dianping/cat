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

	var data = $('#responseTrendMeta', cell).text();
	graphLineChart($('#responseTrend', cell)[0], eval('(' + data
			+ ')'));

	data = $('#hitTrendMeta', cell).text();
	graphLineChart($('#hitTrend', cell)[0], eval('(' + data
			+ ')'));

	data = $('#errorTrendMeta', cell).text();
	graphLineChart($('#errorTrend', cell)[0], eval('(' + data
			+ ')'));
	
	data = $('#distributionChartMeta', cell).text();
	
	if (data != null && data.length > 0) {
		graphPieChart($('#distributionChart', cell)[0], eval('(' + data + ')'));
	}
}

function selectByName(date, domain, ip, type) {
	var queryname = $("#queryname").val();
	window.location.href = "?domain=" + domain + "&type=" + type + "&date="
			+ date + "&queryname=" + queryname + "&ip=" + ip;
}

function selectGroupByName(date, domain, ip, type) {
	var queryname = $("#queryname").val();
	window.location.href = "?op=groupReport&domain=" + domain + "&type=" + type + "&date="
			+ date + "&queryname=" + queryname + "&ip=" + ip;
}



