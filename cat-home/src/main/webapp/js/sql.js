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