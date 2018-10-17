$(document).delegate('.state_graph_link', 'click', function(e){
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
					
					var data = $('#trendMeta',cell).text();
					graphLineChart($('#trendGraph',cell)[0],eval('('+data+')'));
					
					data = $('#distributionMeta', cell).text();
					
					if (data != null && data.length > 0) {
						graphPieChart($('#distributionChart', cell)[0], eval('(' + data + ')'));
					}
				}
			});
		}
	} else {
		anchor.innerHTML = '[:: show ::]';
		cell.style.display = 'none';		
		cell.parentNode.style.display = 'none';
	}	
})