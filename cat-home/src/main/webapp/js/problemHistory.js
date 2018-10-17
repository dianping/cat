function historyGraphLineChart(cell, response) {
	cell.style.display = 'block';
	cell.parentNode.style.display = 'table-cell';
	cell.innerHTML = response;

	var data = $('#errorTrendMeta', cell).text();
	graphLineChart($('#errorTrend', cell)[0], eval('(' + data + ')'));

	data = $('#distributionChartMeta', cell).text();

	if (data != null && data.length > 0) {
		graphPieChart($('#distributionChart', cell)[0], eval('(' + data + ')'));
	}
}
