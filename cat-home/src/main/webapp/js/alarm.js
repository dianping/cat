function sub(anchor) {
	var text = anchor.innerHTML;
	$.ajax({
		type : "get",
		url : anchor.href,
		success : function(data, textStatus) {
			if (data.trim() == 'Success') {
				if (text == '订阅') {
					var oldUrl = anchor.href;
					var newUrl = oldUrl.replace("subState=0","subState=1");
					anchor.href=newUrl;
					anchor.innerHTML = "取消";
				} else {
					var oldUrl = anchor.href;
					var newUrl = oldUrl.replace("subState=1","subState=0");
					anchor.href=newUrl;
					anchor.innerHTML = "订阅";
				}
			}else{
				console.log("ERROR when request");
			}
		}
	});
	return false;
}


