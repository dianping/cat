function hourlyGraphLineChart(cell,data){
	cell.innerHTML = data;
}

function historyGraphLineChart(cell,response){
	cell.style.display = 'block';
	cell.parentNode.style.display = 'block';
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
}

function selectByName(date, domain, ip, type) {
	var queryname = $("#queryname").val();
	window.location.href = "?domain=" + domain + "&type=" + type + "&date="
			+ date + "&queryname=" + queryname + "&ip=" + ip;
}

