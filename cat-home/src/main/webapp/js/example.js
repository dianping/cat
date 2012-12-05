//(function() {
//
//	var container = document.getElementById('container'), start = (new Date)
//			.getTime(), data, graph, offset, i;
//
//	// Draw a sine curve at time t
//	function animate(t) {
//
//		data = [];
//		offset = 2 * Math.PI * (t - start) / 10000;
//
//		// Sample the sine function
//		for (i = 0; i < 4 * Math.PI; i += 0.2) {
//			data.push( [ i, Math.sin(i - offset) ]);
//		}
//
//		// Draw Graph
//		graph = Flotr.draw(container, [ data ], {
//			yaxis : {
//				max : 2,
//				min : -2
//			}
//		});
//
//		// Animate
//		setTimeout(function() {
//			animate((new Date).getTime());
//		}, 50);
//	}
//
//	animate(start);
//})();

(function basic_axis(container) {

  var
    d1 = [],
    d2 = [],
    d3 = [],
    d4 = [],
    d5 = [],                        // Data
    ticks = [[ 0, "Lower"], 10, 20, 30, [40, "Upper"]], // Ticks for the Y-Axis
    graph;
        
  for(var i = 0; i <= 10; i += 0.1){
    d1.push([i, 4 + Math.pow(i,1.5)]);
    d2.push([i, Math.pow(i,3)]);
    d3.push([i, i*5+3*Math.sin(i*4)]);
    d4.push([i, i]);
    if( i.toFixed(1)%1 == 0 ){
      d5.push([i, 2*i]);
    }
  }
        
  d3[30][1] = null;
  d3[31][1] = null;

  function ticksFn (n) { return '('+n+')'; }

  graph = Flotr.draw(container, [ 
      { data : d1, label : 'y = 4 + x^(1.5)', lines : { fill : true } }, 
      { data : d2, label : 'y = x^3'}, 
      { data : d3, label : 'y = 5x + 3sin(4x)'}, 
      { data : d4, label : 'y = x'},
      { data : d5, label : 'y = 2x', lines : { show : true }, points : { show : true } }
    ], {
      xaxis : {
        noTicks : 7,              // Display 7 ticks.
        tickFormatter : ticksFn,  // Displays tick values between brackets.
        min : 1,                  // Part of the series is not displayed.
        max : 7.5                 // Part of the series is not displayed.
      },
      yaxis : {
        ticks : ticks,            // Set Y-Axis ticks
        max : 40                  // Maximum value along Y-Axis
      },
      grid : {
        verticalLines : false,
        backgroundColor : {
          colors : [[0,'#fff'], [1,'#ccc']],
          start : 'top',
          end : 'bottom'
        }
      },
      legend : {
        position : 'nw'
      },
      title : 'Basic Axis example',
      subtitle : 'This is a subtitle'
  });
})(document.getElementById("container"));