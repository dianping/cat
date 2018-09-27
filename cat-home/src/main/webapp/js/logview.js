function show(anchor, id) {
	var cell = document.getElementById(id);
	var text = anchor.innerHTML;
	
	if (!anchor.originalHTML) {
		anchor.originalHTML = anchor.innerHTML;
	}
	
	if (text != '[:: hide ::]') {
		anchor.innerHTML = '[:: hide ::]';

		$.ajax({
			type : "get",
			url : anchor.href + "?header=no&waterfall=false&map=true",
			success : function(data, textStatus) {
				cell.innerHTML = data;
			}
		});

		cell.style.display = 'block';
	} else {
		anchor.innerHTML = anchor.originalHTML;
		cell.style.display = 'none';
	}

	return false;
}

function popup(id) {
	var url = window.location.href;
	var p2 = url.indexOf("?");
	var p1 = url.lastIndexOf("/", p2);
	var newUrl = url.substring(0, p1 + 1) + id + url.substring(p2);

	window.open(newUrl, id);
}