function disksGraph(size, diskHistoryGraph) {
	if (size > 0) {
		// this.parentNode.parentNode.rowIndex
		var graphID = document.getElementById('graph');
		var memoryGraph = document.getElementById('memoryGraph');
		var memroyIndex = memoryGraph.rowIndex;
		var diskInfoHead = graphID.insertRow(memroyIndex + 1);
		diskInfoHead.innerHTML = '';
		for (len = 0; len < size / 3; len++) {
			var id = "diskGraph" + len;
			var graphRow = graphID.insertRow(memroyIndex + 2 + len);
			graphRow.setAttribute("id", id);
		}
		for (i = 0; i < size; i++) {
			var index = Math.floor(i / 3);
			var id = "diskGraph" + index;
			var graphData = diskHistoryGraph[i];
			var graphCell = document.getElementById(id).insertCell();
			var div = document.createElement("div");
			graphCell.appendChild(div);
			div.setAttribute("class", "graph");
			graphLineChart(div, graphData);
		}
	}
}

function buildExtensionGraph(size, extensionGraphs) {
	if (size > 0) {
		var graphID = document.getElementById('graph');
		var extensionGraph = document.getElementById('extensionGraph');
		var extensionIndex = extensionGraph.rowIndex;
		var extensionHead = graphID.insertRow(extensionIndex + 1);
		extensionHead.innerHTML = '';
		for (len = 0; len < size / 3; len++) {
			var id = "extensionGraph" + len;
			var graphRow = graphID.insertRow(extensionIndex + 2 + len);
			graphRow.setAttribute("id", id);
		}
		for (i = 0; i < size; i++) {
			var index = Math.floor(i / 3);
			var id = "extensionGraph" + index;
			var graphData = extensionGraphs[i];
			var graphCell = document.getElementById(id).insertCell();
			var div = document.createElement("div");
			graphCell.appendChild(div);
			div.setAttribute("class", "graph");
			graphLineChart(div, graphData);
		}
	}
}

$(document).delegate('.heartbeat_graph_link', 'click', function(e){
	var anchor = this;
	var el = $(anchor);
	var div = el.attr('data-status');
	var cell = document.getElementById(div);
	cell.src=anchor.href;
	cell.style.display="block";
	return false;
});
