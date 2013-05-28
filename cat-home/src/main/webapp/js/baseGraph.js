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
                    	percent:0.85
                },
                y:{
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
            }

 }
function graphLineChart(container, data) {
	var _data = lineChartParse(data);
	new Venus.SvgChart(container, _data, lineChartOptions);
}