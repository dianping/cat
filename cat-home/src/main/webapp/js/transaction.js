function showGraphs(anchor, id, domain, type, name) {
	var cell = document.getElementById(id);
	var text = anchor.innerHTML;
	
	if (text == '[:: show ::]') {
		anchor.innerHTML = '[:: hide ::]';

		if (cell.nodeName == 'IMG') { // <img src='...'/>
			cell.src = "?op=graphs&domain="+domain+"&type="+type+"&name="+name;
		} else { // <div>...</div>
			$.ajax({
				type: "get",
				url: "?op=graphs&domain="+domain+"&type="+type+"&name="+name,
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
