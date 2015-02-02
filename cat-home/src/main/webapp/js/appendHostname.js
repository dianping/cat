var appendHostname = function(obj) {
	$('table.machines a').each(function() {
		var ip = $(this).text();
		if (ip != "All" && ip.indexOf(".") >= 0) {
			if(obj[ip] !=null){
				$(this).text(ip + " (" + obj[ip]+")");
			}
		}
	});
};