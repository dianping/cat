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
		"width": (i==0?"50":"200") // can width be "auto" for thread column?
	};
	
	colModelArray.push(object);
}

$(function() {
	$("#failureTable").jqGrid(
			{
				datatype : "local",
				colNames : threadArray,
				colModel : colModelArray,
				caption : "From " + jsObject.startTime + " To "
						+ jsObject.endTime + "   Failure Report " + " Domain:"
						+ jsObject.domain,
				height : "100%",
				autowidth: true,
				loadComplete : function() {
					$("#failureTable").setGridHeight('auto');
				}
			}).navGrid('#pager2', {edit:false,add:false,del:false});

	$("#failureTable").jqGrid('setGridWidth', '100%');
	
	var segments = jsObject.segments;
	for (var key in segments) {
		var segment = segments[key];
		var threadResult = creatNewArray(threadArray.length);
		
		threadResult[0] = segment.id.substring(11);
		for ( var j = 0; j < segment.entries.length; j++) {
			var entry = segment.entries[j];
			var threadId = entry.threadId;
			var type = entry.type;
			var path = entry.path;
			var text = entry.text;
			var index = getIndex(threadId, threadArray);
			var url = getUrl(type, text, path);
			
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
		$("#failureTable").jqGrid('addRowData', m + 1, minuteData);
		
        
        $(function(){
            $(window).resize(function(){  
                  $("#failureTable").setGridWidth($(window).width()*0.99);
            });
        });
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

// should all legend be provided by data model?
function getUrl(type, text, path) {
	if (type == 'RuntimeException') {
		return '<a style=\'background:red;\' href=\'m/' + path + '\'>' + text + '</a>';
	} else if (type == 'Exception') {
		return '<a style=\'background:#FFFF00;\' href=\'m/' + path + '\'>' + text + '</a>';
	} else if (type == 'Error') {
		return '<a style=\'background:#FF00FF;\' href=\'m/' + path + '\'>' + text + '</a>';
	} else {
		return '<a style=\'background:#CC99FF;\' href=\'m/' + path + '\'>' + text + '</a>';
	}
}