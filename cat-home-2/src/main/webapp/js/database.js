$(document).delegate('.graph_link', 'click', function(e){
	var anchor = this,
		el = $(anchor),
		id = Number(el.attr('data-status')) || 0;
	
	if(e.ctrlKey || e.metaKey){
		return true;
	}else{
		e.preventDefault();
	}
	
	var cell = document.getElementById(id);
	var text = el.html();
	
	if (text == '[:: show ::]') {
		anchor.innerHTML = '[:: hide ::]';
		cell.style.display = '';
		cell.style.display = '';
	} else {
		anchor.innerHTML = '[:: show ::]';
		cell.style.display = 'none';		
		cell.style.display = 'none';
	}	
});



