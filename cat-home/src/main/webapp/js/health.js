$(document).delegate('.health_history_graph_link', 'click', function(e){
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
					cell.style.display = 'table-cell';
					cell.parentNode.style.display = 'table-cell';
					cell.innerHTML = response;
					
					var data = $('#trendMeta',cell).text();
					var type=$('#reportType',cell).text();
					if(type.trim()=='day'){
						graphLineChart($('#trendGraph',cell)[0],eval('('+data+')'),60*60*1000);
					}else{
						graphLineChart($('#trendGraph',cell)[0],eval('('+data+')'),60*60*1000*24);
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