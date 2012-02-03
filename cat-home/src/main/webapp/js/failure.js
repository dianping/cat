function generateTable(wrap, json) {
	var table = $('<table />').addClass('report-table'), caption = $('<caption />'), thead = $('<thead />'), thr = $(
			'<tr />').appendTo(thead), threads = json.threads.threads, ths = [ "Minute" ]
			.concat(threads);
	if(threads==null){
		threads =[];
	}
	/*caption.html(
			"From " + json.startTime + " To " + json.endTime
					+ " Failure Report Domain:" + json.domain).appendTo(table);*/

	thead.appendTo(table);

	ths.forEach(function(th) {
		var temp = 	$('<th />').html(th);
		if(temp!="")
			temp.appendTo(thr);
	});

	/*$('<tr />').append(
			$('<td />').attr('colspan', threads.length + 1).html(
					"machines:" + json.machines.machines.join(','))).appendTo(
			table);*/
	for ( var key in json.segments) {
		var seg = json.segments[key];
		var tr = $('<tr />'), tds = [];
		$('<td />').html(seg.id.substring(11)).appendTo(tr);
		threads.forEach(function() {
			var td = $('<td />');
			td.appendTo(tr);
			tds.push(td);
		});
		if (seg.entries == null) {
			seg.entries = [];
		}
			seg.entries
					.forEach(function(entry) {
						var index = threads.indexOf(entry.threadId), td = tds[index], type = entry.type, anchor = $(
								'<a />').attr("href",
								"m/" + entry.path).addClass(type).html(entry.text);

						if (td.html()) {
							td.append($('<br />'));
						}

						if (!type) {
							type = "Other";
						}
						td.append(anchor);
					});
			tr.appendTo(table);
		
	}
	$(wrap).append(table);
}

generateTable('#failureTable', jsonData);

var currentDomain = $("#currentDomain").val();
$("#domain-"+currentDomain).css("background", "#FFCC00");

var currentIp = $("#currentIp").val();
$("a[data-id='ip-"+currentIp+"']").css("background", "#FFCC00");

