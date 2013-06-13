$(document).delegate('.graph_link', 'click', function(e){
	var anchor = this,
		el = $(anchor),
		id = Number(el.attr('data-status')) || 0;
	
	if(e.ctrlKey || e.metaKey){
		return true;
	}else{
		e.preventDefault();
	}
	
	var cell = document.getElementById(id);
	var text = el.html();
	
	if (text == '[:: show ::]') {
		anchor.innerHTML = '[:: hide ::]';

		if (cell.nodeName == 'IMG') { // <img src='...'/>
			cell.src=anchor.href;
		} else { // <div>...</div>
			$.ajax({
				type: "get",
				url: anchor.href,
				success : function(data, textStatus) {
					cell.innerHTML = data;
				}
			});
		}
		cell.style.display = 'block';
		cell.parentNode.style.display = 'block';
	} else {
		anchor.innerHTML = '[:: show ::]';
		cell.style.display = 'none';		
		cell.parentNode.style.display = 'none';
	}	
});

$(document).delegate('.history_graph_link', 'click', function(e){
	var anchor = this,
		el = $(anchor),
		id = Number(el.attr('data-status')) || 0;
	
	if(e.ctrlKey || e.metaKey){
		return true;
	}else{
		e.preventDefault();
	}
	
	var cell = document.getElementById(id);
	var text = el.html();
	
	if (text == '[:: show ::]') {
		anchor.innerHTML = '[:: hide ::]';

		if (cell.nodeName == 'IMG') { // <img src='...'/>
			cell.src=anchor.href;
		} else { // <div>...</div>
			$.ajax({
				type: "get",
				url: anchor.href,
				success : function(response, textStatus) {
					cell.style.display = 'block';
					cell.parentNode.style.display = 'block';
					cell.innerHTML = response;

					var hitData = $('#hitTrendMeta',cell).text();
					graph($('#hitTrend',cell)[0],eval('('+hitData+')'));
					var failureData = $('#failureTrendMeta',cell).text();
					graph($('#failureTrend',cell)[0],eval('('+failureData+')'));
				}
			});
		}
	} else {
		anchor.innerHTML = '[:: show ::]';
		cell.style.display = 'none';		
		cell.parentNode.style.display = 'none';
	}	
});


function graphPieChart(container,data){
	 var showData=[];
	 
	 for(i = 0; i < data.items.length; i++){
		 var dataItem = [];
		 var graphItem ={};
		
		 dataItem.push([i+1,data.items[i].number]);
		 graphItem.data=dataItem;
		 graphItem.label=data.items[i].title;
		 showData.push(graphItem);
	 }

	 var graph = Flotr.draw(container,showData, {
	    HtmlText : true,
	    grid : {
	      verticalLines : false,
	      horizontalLines : false
	    },
	    xaxis : { showLabels : false },
	    yaxis : { showLabels : false },
	    pie : {
	      show : true, 
	      position : 'ne',
	      explode : 6
	    },
	    mouse : { track : true },
	    legend : {
	      position : 'ne',
	      backgroundColor : '#D2E8FF'
	    }
	  });
}



