(function() {

	var container = document.getElementById('container'), start = (new Date)
			.getTime(), data, graph, offset, i;

	// Draw a sine curve at time t
	function animate(t) {

		data = [];
		offset = 2 * Math.PI * (t - start) / 10000;

		// Sample the sine function
		for (i = 0; i < 4 * Math.PI; i += 0.2) {
			data.push( [ i, Math.sin(i - offset) ]);
		}

		// Draw Graph
		graph = Flotr.draw(container, [ data ], {
			yaxis : {
				max : 2,
				min : -2
			}
		});

		// Animate
		setTimeout(function() {
			animate((new Date).getTime());
		}, 50);
	}

	animate(start);
})();