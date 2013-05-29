function historyGraphLineChart(cell,response){
	cell.style.display = 'block';
	cell.parentNode.style.display = 'block';
	cell.innerHTML = response;
	
	var data = $('#trendMeta',cell).text();
	var type=$('#reportType',cell).text();
	if(type.trim()=='day'){
		graphLineChart($('#trendGraph',cell)[0],eval('('+data+')'),60*60*1000);
	}else{
		graphLineChart($('#trendGraph',cell)[0],eval('('+data+')'),60*60*1000*24);
	}
}
