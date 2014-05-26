var appendHostname = function(obj) {
	$('table.machines a').each(function() {
		var ip = $(this).text();
		if (ip != "All") {
			$(this).text(ip + " " + obj[ip]);
		}
	});
};