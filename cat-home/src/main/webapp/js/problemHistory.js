function historyGraphLineChart(cell,response){
	cell.style.display = 'block';
	cell.parentNode.style.display = 'block';
	cell.innerHTML = response;
	
	var data = $('#errorTrendMeta',cell).text();
	graphLineChart($('#errorTrend',cell)[0],eval('('+data+')'));
}


