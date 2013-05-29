function hourlyGraphLineChart(cell,data){
	cell.style.display = 'block';
	cell.parentNode.style.display = 'block';
	cell.innerHTML = response;
	
	var data = $('#trendMeta',cell).text();
	graphLineChart($('#trendGraph',cell)[0],eval('('+data+')'));
}