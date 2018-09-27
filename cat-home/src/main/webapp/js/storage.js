$(document).delegate('.storage_graph_link', 'click', function(e){
	var anchor = this,
		el = $(anchor),
		id = el.attr('data-status');
	
	if(e.ctrlKey || e.metaKey){
		return true;
	}else{
		e.preventDefault();
	}
	
	var cell = document.getElementById(id);
	var text = el.html();
	
	if (text == '[:: show ::]') {
		anchor.innerHTML = '[:: hide ::]';

		if (cell.nodeName == 'IMG') { // <img src='...'/>
			cell.src=anchor.href;
		} else { 
			$.ajax({
				type: "get",
				url: anchor.href,
				success : function(response, textStatus) {
					cell.style.display = 'block';
					cell.parentNode.style.display = 'table-cell';
					cell.innerHTML = response;
					
					var data = $('#countTrendMeta',cell).text();
					graphLineChart($('#countTrend',cell)[0],eval('('+data+')'));
					
					data = $('#longTrendMeta',cell).text();
					graphLineChart($('#longTrend',cell)[0],eval('('+data+')'));
					
					data = $('#avgTrendMeta',cell).text();
					graphLineChart($('#avgTrend',cell)[0],eval('('+data+')'));
					
					data = $('#errorTrendMeta',cell).text();
					graphLineChart($('#errorTrend',cell)[0],eval('('+data+')'));
					
					data = $('#piechartMeta',cell).text();
					graphPieChart($('#piechart',cell)[0],eval('('+data+')'));
				}
			});
		}
	} else {
		anchor.innerHTML = '[:: show ::]';
		cell.style.display = 'none';		
		cell.parentNode.style.display = 'none';
	}	
})