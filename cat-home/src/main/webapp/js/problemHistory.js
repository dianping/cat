$(document).delegate('.history_graph_link', 'click', function(e){
	var anchor = this,
		el = $(anchor),
		id = Number(el.attr('data-status')) || 0;
	
	console.log("id: " + id)
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
		} else { // <div>...</div>
			$.ajax({
				type: "get",
				url: anchor.href,
				success : function(response, textStatus) {
					cell.style.display = 'block';
					cell.parentNode.style.display = 'block';
					cell.innerHTML = response;
					
					var data = $('#errorTrendMeta',cell).text();
					graph($('#errorTrend',cell)[0],eval('('+data+')'));
				}
			});
		}
	} else {
		anchor.innerHTML = '[:: show ::]';
		cell.style.display = 'none';		
		cell.parentNode.style.display = 'none';
	}	
});

$(document).delegate('.problem_status_graph_link', 'click', function(e){
	var anchor = this,
		el = $(anchor),
		id = el.attr('data-status');
	
	console.log("id: " + id)
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
		} else { // <div>...</div>
			$.ajax({
				type: "get",
				url: anchor.href,
				success : function(response, textStatus) {
					cell.style.display = 'block';
					cell.parentNode.style.display = 'block';
					cell.innerHTML = response;
					
					var data = $('#errorTrendMeta',cell).text();
					graph($('#errorTrend',cell)[0],eval('('+data+')'));
				}
			});
		}
	} else {
		anchor.innerHTML = '[:: show ::]';
		cell.style.display = 'none';		
		cell.parentNode.style.display = 'none';
	}	
});
