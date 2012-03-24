function show(anchor) {
	alert(anchor)
	$.ajax({
		type: "get",
		url: anchor.href,
		success : function(data, textStatus) {
			alert(data);
		}
	});
	
}