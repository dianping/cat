function showGraphs(anchor, id, domain, type, name) {
	var cell = document.getElementById(id);
	var text = anchor.innerHTML;
	
	if (text == '[:: show ::]') {
		anchor.innerHTML = '[:: hide ::]';
		
		$.ajax({
			type: "get",
			url: "?op=graphs&domain="+domain+"&type="+type+"&name="+name,
			success : function(data, textStatus) {
				cell.innerHTML = data;
			}
		});

		cell.parentNode.style.display = 'block';
	} else {
		anchor.innerHTML = '[:: show ::]';
		cell.parentNode.style.display = 'none';
	}
	
	return false;
}