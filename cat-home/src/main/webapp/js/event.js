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

		if (cell.nodeName == 'IMG') { // <img src='...'/>
			cell.src=anchor.href;
		} else { // <div>...</div>
			$.ajax({
				type: "get",
				url: anchor.href,
				success : function(data, textStatus) {
					cell.innerHTML = data;
				}
			});
		}
		cell.style.display = 'block';
		cell.parentNode.style.display = 'block';
	} else {
		anchor.innerHTML = '[:: show ::]';
		cell.style.display = 'none';		
		cell.parentNode.style.display = 'none';
	}	
});
