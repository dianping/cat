var lineChartParse = function(data){
		var res = [];
		data.subTitles.forEach(function(title,i){
			var series = {}
			series.name = title;
			series.data = {};
			data.values[i].forEach(function(value,j){
				var time = Date.parse(data.start);
				var t = new Date(+time + j*data.step);
				series.data[t.getFullYear()+"-"+(t.getMonth()+1)+"-"+t.getDate()+" "+t.getHours()+":"+t.getMinutes()] = value;
			});
			res.push(series);
	
		})
		return res;
}
var pieChartOption = {
        pie:{
            animation:true, //[true, false, 'simultaneous'],
            hollow:10,
            stroke:{
                'stroke-width':1,
                'stroke':'#dfdfdf'
            },
            radius:100

        },
        legend:{}
    };

var pieChartParse = function(data){
	var res = {};
	data.items.forEach(function(item){
			res[item.title]= item.number;
	})
	return res;
}

function graphPieChart(container, data){
	 new Venus.SvgChart(container, pieChartParse(data), pieChartOption);
}

var lineChartOptions = {
    axis:{
        x:{
            type:"datetime",
            percent:0.8
        },
        y:{
        	title:"10分钟"
        }
    },
    line:{
        smooth:true,
        dotRadius:4,
        dotSelect:true,
        area:false
    },
    grid:{
        enableRow:true,
        enableColumn:true
    },
    legend:{
    },
    tooltip:function(obj){
    	return obj.x+" "+obj.y.toFixed(0);
    }
    
 }
function graphLineChart(container, data) {
	var _data = lineChartParse(data);
	console.log(_data);
	new Venus.SvgChart(container, _data, lineChartOptions);
}

$(document).delegate(
		'.graph_link',
		'click',
		function(e) {
			var anchor = this, el = $(anchor), id = Number(el
					.attr('data-status')) || 0;

			if (e.ctrlKey || e.metaKey) {
				return true;
			} else {
				e.preventDefault();
			}

			var cell = document.getElementById(id);
			var text = el.html();

			if (text == '[:: show ::]') {
				anchor.innerHTML = '[:: hide ::]';

				if (cell.nodeName == 'IMG') { // <img src='...'/>
					cell.src = anchor.href;
				} else { // <div>...</div>
					$.ajax({
						type : "get",
						url : anchor.href,
						success : function(data, textStatus) {
							hourlyGraphLineChart(cell,data);
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

$(document).delegate(
		'.history_graph_link',
		'click',
		function(e) {
			var anchor = this, el = $(anchor), id = Number(el
					.attr('data-status')) || 0;

			if (e.ctrlKey || e.metaKey) {
				return true;
			} else {
				e.preventDefault();
			}

			var cell = document.getElementById(id);
			var text = el.html();

			if (text == '[:: show ::]') {
				anchor.innerHTML = '[:: hide ::]';

				if (cell.nodeName == 'IMG') { // <img src='...'/>
					cell.src = anchor.href;
				} else { // <div>...</div>
					$.ajax({
						type : "get",
						url : anchor.href,
						success : function(response, textStatus) {
							historyGraphLineChart(cell,response);
						}
					});
				}
			} else {
				anchor.innerHTML = '[:: show ::]';
				cell.style.display = 'none';
				cell.parentNode.style.display = 'none';
			}
		});
$(document).delegate('.problem_status_graph_link', 'click', function(e){
	var anchor = this,
		el = $(anchor),
		id = el.attr('data-status');
	
	console.log("id: " + id)
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
					historyGraphLineChart(cell,response);
				}
			});
		}
	} else {
		anchor.innerHTML = '[:: show ::]';
		cell.style.display = 'none';		
		cell.parentNode.style.display = 'none';
	}	
});

