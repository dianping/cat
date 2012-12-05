var isCtrl = false;

function showGraphs(anchor,target) {
	if (isCtrl) return true;
	
	var cell = document.getElementById(target);
	var text = anchor.innerHTML;
	
	if (text == '[:: show ::]') {
		anchor.innerHTML = '[:: hide ::]';

		if (cell.nodeName == 'IMG') { // <img src='...'/>
			cell.src = "?op=graphs&id="+id;
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
	
	return false;
}

$(document).keydown(function(e) {
    if(e.ctrlKey || e.metaKey) isCtrl = true;
}).keyup(function(e) {
    isCtrl = false;
});


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



