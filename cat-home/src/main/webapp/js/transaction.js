function renderTable(container, data) {
	var table = $('<table />').addClass('report-table'), cols = "Type,Total Count,Fail Count,Failure%,Sample Link,Min/Max/Avg/Std(ms)"
			.split(','), tr, th, td;

	function makeRow(row) {
		var tr = $('<tr />');
		[  "<a href='?domain="+domain+"&type="+row.id+"'>"+row.id+"</a>"  , row.totalCount, row.failCount, row.failPercent,
				'<a href="\m\\' + row.successMessageUrl + '" >success</a>',
				row.min + "/" + row.max + "/" + row.avg + "/" + row.std ]
				.forEach(function(e) {
					$('<td />').html(e).appendTo(tr);
				});
		tr.appendTo(table);
	}

	// thead
	tr = $('<tr />').appendTo(table);
	cols.forEach(function(e) {
		var th = $('<th />').html(e);
		th.appendTo(tr);
	});

	for ( var i in data) {
		makeRow(data[i]);
	}
	$(container).empty().append(table);
}

if (nowtype!=null && nowtype.length>0) {
	renderTable("#transactionTable", data["types"][nowtype]["names"]);
} else {
	renderTable("#transactionTable", data["types"]);
}
