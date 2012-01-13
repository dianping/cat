var jsObject = eval(jsonDate);
var threadArray = new Array();
threadArray.push('Minute');

for (i = 0; i < jsObject.threads.threads.length; i++) {
	threadArray.push(jsObject.threads.threads[i]);
}

var colModelArray = new Array();

for (i = 0; i < threadArray.length; i++) {
	var object = {
		"name" : threadArray[i],
		"index" : threadArray[i],
		"sorttype" : "string"
	};
	colModelArray.push(object);
}

$(function() {
	$("#failureTable").jqGrid( {
		datatype : "local",
		colNames : threadArray,
		colModel : colModelArray,
		viewrecords : true,
		caption :"From "+jsObject.startTime+" To "+jsObject.endTime + "   Failure Report " +" Domain:" + jsObject.domain,
		height : 500,
		loadComplete : function() {
			var grid = $("#failureTable");
			var ids = grid.getDataIDs();
			for ( var i = 0; i < ids.length; i++) {
				grid.setRowData(ids[i], false, {
					height : 25
				});
			}
			grid.setGridHeight('auto');
		}
	}).navGrid('#pager2', {
		edit : false,
		add : false,
		del : false
	});

	jQuery("#failureTable").jqGrid('setGridWidth', '90%');
	for (var i = 0; i < jsObject.segments.length; i++) {
		var segment = jsObject.segments[i];
		var threadResult = creatNewArray(threadArray.length);
		threadResult[0] = jsObject.segments[i].id;
		for ( var j = 0; j < segment.entries.length; j++) {
			var entry = segment.entries[j];
			var threadId = entry.threadId;
			var type = entry.type;
			var messageId = entry.messageId;
			var text = entry.text;

			var index = getIndex(threadId, threadArray);
			var url = getUrl(type, text, messageId);
			if (threadResult[index] == "") {
				threadResult[index] = threadResult[index] + url;
			} else {
				threadResult[index] = threadResult[index] + '</br>' + url;
			}
		}
		var minuteData = {};
		for ( var m = 0; m < threadArray.length; m++) {
			minuteData[threadArray[m]] = threadResult[m];
		}
		jQuery("#failureTable").jqGrid('addRowData', m + 1, minuteData);
	}
});

function creatNewArray(length) {
	var array = new Array();
	for ( var i = 0; i < length; i++) {
		array.push("");
	}
	return array;
}

function getIndex(object, array) {
	for ( var i = 0; i < array.length; i++) {
		if (array[i] == object)
			return i;
	}
}

function getUrl(type, text, messageId) {
	if (type == 'RuntimeException') {
		return '<a target=\'_blank\' style=\'background:red;\' href=\'www.dianping.com/messageId='
				+ messageId + '\'>' + text + '</a>';
	} else if (type == 'Exception') {
		return '<a target=\'_blank\' style=\'background:#FFFF00;\' href=\'www.dianping.com/messageId='
				+ messageId + '\'>' + text + '</a>';
	} else if (type == 'Error') {
		return '<a target=\'_blank\' style=\'background:#FF00FF;\' href=\'www.dianping.com/messageId='
				+ messageId + '\'>' + text + '</a>';
	} else {
		return '<a target=\'_blank\' style=\'background:#CC99FF;\' href=\'www.dianping.com/messageId='
				+ messageId + '\'>' + text + '</a>';
	}
}