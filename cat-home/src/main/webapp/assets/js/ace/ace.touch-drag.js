/**
 <b>Custom drag event for touch devices</b> used in scrollbars.
 For better touch event handling and extra options a more advanced solution such as <u>Hammer.js</u> is recommended.
*/

//based on but not dependent on jQuery mobile
/*
* jQuery Mobile v1.3.2
* http://jquerymobile.com
*
* Copyright 2010, 2013 jQuery Foundation, Inc. and other contributors
* Released under the MIT license.
* http://jquery.org/license
*
*/
ace.add_touch_drag = function($) {
	if(!ace.vars['touch']) return;

	var touchStartEvent = "touchstart MSPointerDown pointerdown",// : "mousedown",
			touchStopEvent  =  "touchend touchcancel MSPointerUp MSPointerCancel pointerup pointercancel",// : "mouseup",
			touchMoveEvent  =  "touchmove MSPointerMove MSPointerHover pointermove";// : "mousemove";


	$.event.special.ace_drag = {
		setup: function() {
			var min_threshold = 0;
		
			var $this = $(this);
			$this.on(touchStartEvent, function(event) {		
				var data = event.originalEvent.touches ?
					event.originalEvent.touches[ 0 ] :
					event,
					start = {
						//time: Date.now(),
						coords: [ data.pageX, data.pageY ],
						origin: $(event.target)
					},
					stop;
					//start.origin.trigger({'type' : 'ace_dragStart', 'start':(start || [-1,-1])});
					
					var direction = false, dx = 0, dy = 0;

				function moveHandler(event) {
					if (!start) {
						return;
					}
					var data = event.originalEvent.touches ?
							event.originalEvent.touches[ 0 ] :
							event;
					stop = {
						coords: [ data.pageX, data.pageY ]
					};
					
					// prevent scrolling
					//if ( Math.abs(start.coords[1] - stop.coords[1]) > 0 || Math.abs(start.coords[0] - stop.coords[01]) > 0 ) {
						//event.preventDefault();
					//}


					if (start && stop) {
						dx = 0;
						dy = 0;

						direction = 
							(
							 Math.abs(dy = start.coords[ 1 ] - stop.coords[ 1 ]) > min_threshold
								&& 
							 Math.abs(dx = start.coords[ 0 ] - stop.coords[ 0 ]) <= Math.abs(dy)
							)
							? 
							(dy > 0 ? 'up' : 'down')
							:
							(
							 Math.abs(dx = start.coords[ 0 ] - stop.coords[ 0 ]) > min_threshold
								&& 
							 Math.abs( dy ) <= Math.abs(dx)
							)
							?
							(dx > 0 ? 'left' : 'right')
							:
							false;
							

							if( direction !== false ) {
							 var retval = {cancel: false}
							 start.origin.trigger({
								'type': 'ace_drag',
								//'start': start.coords,
								//'stop': stop.coords,
								'direction': direction,
								'dx': dx,
								'dy': dy,
								'retval': retval
							 })

		 					  // prevent document scrolling unless retval.cancel == true
							  if( retval.cancel == false ) event.preventDefault();
							}
					}
					start.coords[0] = stop.coords[0];
					start.coords[1] = stop.coords[1];
				}

				$this
				.on(touchMoveEvent, moveHandler)
				.one(touchStopEvent, function(event) {
					$this.off(touchMoveEvent, moveHandler);
					//start.origin.trigger({'type' : 'ace_dragEnd', 'stop':(stop || [-1,-1])});
					
					start = stop = undefined;
				
				});
			});
		}
	}
}